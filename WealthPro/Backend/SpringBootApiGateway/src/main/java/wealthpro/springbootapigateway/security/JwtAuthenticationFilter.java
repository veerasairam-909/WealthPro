package wealthpro.springbootapigateway.security;

import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import wealthpro.springbootapigateway.entities.AuditUsers;
import wealthpro.springbootapigateway.service.AuditService;
import wealthpro.springbootapigateway.service.AuthUserDetailsService;
import wealthpro.springbootapigateway.service.TokenBlocklistService;
import wealthpro.springbootapigateway.utility.JwtTokenUtil;

import reactor.core.publisher.Mono;

/**
 * JWT authentication filter for the gateway.
 *
 * Responsibilities:
 *  1. Extract the bearer token, validate signature & expiry.
 *  2. Load UserDetails, populate ReactiveSecurityContext.
 *  3. Write an audit entry.
 *  4. Inject X-Auth-Username / X-Auth-Roles / X-Auth-Client-Id headers into
 *     the forwarded request so downstream microservices can perform
 *     row-level ownership checks WITHOUT re-validating the JWT.
 *
 *  Downstream services MUST trust these headers, which is safe because
 *  the microservices are not exposed publicly — only the gateway can reach
 *  them (internal network / Eureka routing).
 */
public class JwtAuthenticationFilter implements WebFilter {

    public static final String HDR_USERNAME  = "X-Auth-Username";
    public static final String HDR_ROLES     = "X-Auth-Roles";
    public static final String HDR_CLIENT_ID = "X-Auth-Client-Id";

    private JwtTokenUtil jwtService;
    private AuthUserDetailsService service;
    private AuditService auditService;
    private TokenBlocklistService tokenBlocklistService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtService, AuthUserDetailsService service,
                                   AuditService auditService, TokenBlocklistService tokenBlocklistService) {
        this.jwtService = jwtService;
        this.service = service;
        this.auditService = auditService;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // Skip preflight requests and favicon
        if (CorsUtils.isPreFlightRequest(exchange.getRequest()) ||
                exchange.getRequest().getPath().value().equals("/favicon.ico")) {
            return chain.filter(exchange);
        }

        // Strip any spoofed auth headers on inbound requests — only this filter
        // is allowed to set them, and only after verifying the JWT.
        ServerHttpRequest sanitised = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove(HDR_USERNAME);
                    h.remove(HDR_ROLES);
                    h.remove(HDR_CLIENT_ID);
                })
                .build();
        ServerWebExchange sanitisedExchange = exchange.mutate().request(sanitised).build();

        String token = extractToken(sanitisedExchange.getRequest());

        if (token == null) {
            return chain.filter(sanitisedExchange);
        }

        // Reject tokens that have been logged out
        if (tokenBlocklistService.isBlocked(token)) {
            sanitisedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return sanitisedExchange.getResponse().setComplete();
        }

        final String usernameFromToken;
        final String rolesFromToken;
        final Long   clientIdFromToken;
        try {
            usernameFromToken = jwtService.getUsernameFromToken(token);
            rolesFromToken    = jwtService.getRolesFromToken(token);
            clientIdFromToken = jwtService.getClientIdFromToken(token);
        } catch (Exception e) {
            sanitisedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return sanitisedExchange.getResponse().setComplete();
        }

        // Safely resolve remote address (can be null behind a proxy/load balancer)
        String remoteAddress = "unknown";
        if (sanitisedExchange.getRequest().getRemoteAddress() != null) {
            remoteAddress = sanitisedExchange.getRequest().getRemoteAddress().getHostString()
                    + ":" + sanitisedExchange.getRequest().getRemoteAddress().getPort();
        }

        AuditUsers auditUser = new AuditUsers();
        auditUser.setUsername(usernameFromToken);
        auditUser.setMethod(sanitisedExchange.getRequest().getMethod().name());
        auditUser.setRemoteAddress(remoteAddress);
        auditUser.setPath(sanitisedExchange.getRequest().getPath().value());
        auditUser.setAction(sanitisedExchange.getRequest().getMethod().name());
        auditUser.setResource(sanitisedExchange.getRequest().getPath().value());
        auditUser.setTimestamp(LocalDateTime.now());

        // CRITICAL: switchIfEmpty must run BEFORE flatMap, not after.
        // If we put switchIfEmpty after flatMap, it incorrectly fires for any
        // controller returning Mono<Void> (e.g. DELETE) — because the inner Mono
        // is empty even though the request was handled successfully. Putting it
        // before flatMap makes it fire only when findByUsername itself is empty.
        return service.findByUsername(usernameFromToken)
                .switchIfEmpty(Mono.defer(() -> {
                    // Token references a user that no longer exists in DB
                    sanitisedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return sanitisedExchange.getResponse().setComplete().then(Mono.empty());
                }))
                .flatMap(userDetails -> {
                    auditUser.setRoles(userDetails.getAuthorities().toString());

                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        // Inject identity headers for downstream services
                        ServerHttpRequest enriched = sanitisedExchange.getRequest().mutate()
                                .header(HDR_USERNAME, usernameFromToken)
                                .header(HDR_ROLES,
                                        (rolesFromToken == null || rolesFromToken.isBlank())
                                            ? userDetails.getAuthorities().toString()
                                            : rolesFromToken)
                                .headers(h -> {
                                    if (clientIdFromToken != null) {
                                        h.set(HDR_CLIENT_ID, String.valueOf(clientIdFromToken));
                                    }
                                })
                                .build();
                        ServerWebExchange enrichedExchange = sanitisedExchange.mutate().request(enriched).build();

                        return auditService.saveUserAudit(auditUser)
                                .then(chain.filter(enrichedExchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)));
                    }

                    // Token signature/expiry invalid
                    sanitisedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return sanitisedExchange.getResponse().setComplete();
                });
    }

    private String extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
