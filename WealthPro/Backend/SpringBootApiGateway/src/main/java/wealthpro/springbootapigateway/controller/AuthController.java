package wealthpro.springbootapigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import wealthpro.springbootapigateway.client.WealthproClient;
import wealthpro.springbootapigateway.dto.AuthRequest;
import wealthpro.springbootapigateway.dto.AuthResponse;
import wealthpro.springbootapigateway.security.WealthProUserDetails;
import wealthpro.springbootapigateway.service.TokenBlocklistService;
import wealthpro.springbootapigateway.utility.JwtTokenUtil;

import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtService;

    @Autowired
    private WealthproClient wealthproClient;

    @Autowired
    private TokenBlocklistService tokenBlocklistService;

    @PostMapping("/auth/login")
    public Mono<AuthResponse> login(@RequestBody AuthRequest authRequest) {

        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        )
        .flatMap(auth -> {
            // AuthUserDetailsService returns WealthProUserDetails which carries
            // userId and roleName — no second DB call needed.
            UserDetails principal = (UserDetails) auth.getPrincipal();

            boolean isClient = principal.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()));

            if (!isClient) {
                // Extract userId and role directly from the authenticated principal
                Long   userId;
                String roleName;
                if (principal instanceof WealthProUserDetails wp) {
                    userId   = wp.getUserId();
                    roleName = wp.getRoleName();
                } else {
                    userId   = null;
                    roleName = "";
                }
                String token = jwtService.generateStaffToken(principal, userId);
                return Mono.just(new AuthResponse(token, userId, roleName));
            }

            // CLIENT users — resolve clientId from Wealthpro and embed in JWT.
            // Fallback: if lookup fails, issue a token without clientId.
            return wealthproClient.resolveClientIdByUsername(principal.getUsername())
                    .map(clientId -> {
                        String token = jwtService.generateClientToken(principal, clientId);
                        return new AuthResponse(token, null, "CLIENT");
                    })
                    .defaultIfEmpty(new AuthResponse(jwtService.generateToken(principal), null, "CLIENT"));
        })
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
        .onErrorResume(e -> {
            // Already a structured HTTP error — pass through unchanged
            if (e instanceof ResponseStatusException) return Mono.error(e);
            // Wrong username or password — return clean 401
            if (e instanceof org.springframework.security.core.AuthenticationException) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
            }
            // Anything else (DB error, mapping error, etc.) must NOT be hidden as
            // "Invalid credentials" — surface it as a 500 so it can be diagnosed.
            return Mono.error(new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Login failed due to a server error: " + e.getMessage()));
        });
    }

    @PostMapping("/auth/logout")
    public Mono<ResponseEntity<String>> logout(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlocklistService.block(token);
        }
        return Mono.just(ResponseEntity.ok("Logged out successfully"));
    }
}
