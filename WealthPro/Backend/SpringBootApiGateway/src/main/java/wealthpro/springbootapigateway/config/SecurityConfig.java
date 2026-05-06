package wealthpro.springbootapigateway.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import wealthpro.springbootapigateway.security.JwtAuthenticationFilter;
import wealthpro.springbootapigateway.service.AuditService;
import wealthpro.springbootapigateway.service.AuthUserDetailsService;
import wealthpro.springbootapigateway.service.TokenBlocklistService;
import wealthpro.springbootapigateway.utility.JwtTokenUtil;

/**
 * WealthPro API Gateway - Security Configuration
 *
 * Roles:
 *   CLIENT     - Client / Investor
 *   RM         - Relationship Manager
 *   DEALER     - Dealer / Trader
 *   COMPLIANCE - Compliance Analyst
 *   ADMIN      - Wealth Admin
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private JwtTokenUtil jwtService;

    @Autowired
    private AuthUserDetailsService service;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TokenBlocklistService tokenBlocklistService;

    @Value("#{'${cors.allowed-origins}'.split(',')}")
    private List<String> corsAllowedOrigins;

    private final ReactiveUserDetailsService mapReactiveUserDetailsService;

    public SecurityConfig(ReactiveUserDetailsService mapReactiveUserDetailsService) {
        this.mapReactiveUserDetailsService = mapReactiveUserDetailsService;
    }

    @Bean
    @Lazy
    public ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService uds) {
        UserDetailsRepositoryReactiveAuthenticationManager am =
                new UserDetailsRepositoryReactiveAuthenticationManager(uds);
        am.setPasswordEncoder(passwordEncoder());
        return am;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                             ReactiveAuthenticationManager am) {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, service, auditService, tokenBlocklistService);

        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges

                // ── Public endpoints ─────────────────────────────────────
                .pathMatchers("/auth/login").permitAll()
                .pathMatchers("/auth/logout").permitAll()

                // RM-led client onboarding — clients DO NOT self-register.
                // RM/ADMIN call /auth/register/client to create a CLIENT user.
                // The role is still hardcoded to CLIENT server-side regardless of payload.
                .pathMatchers("/auth/register/client")
                    .hasAnyRole("RM", "ADMIN")

                // ── User Management ─────────────────────────────────────
                .pathMatchers(HttpMethod.GET, "/auth/users").hasAnyRole("ADMIN", "COMPLIANCE", "RM")
                .pathMatchers("/auth/users/**").hasRole("ADMIN")
                .pathMatchers("/auth/audit/**").hasAnyRole("ADMIN", "COMPLIANCE")

                // ── Wealthpro: IAM & Client Onboarding / KYC ────────────
                .pathMatchers(HttpMethod.GET, "/api/clients/by-username/**")
                    .hasAnyRole("RM", "COMPLIANCE", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/clients/*")
                    .hasAnyRole("CLIENT", "RM", "COMPLIANCE", "ADMIN")

                .pathMatchers(HttpMethod.GET, "/api/clients/*/kyc")
                    .hasAnyRole("CLIENT", "RM", "COMPLIANCE", "ADMIN")
                .pathMatchers("/api/clients/*/kyc")
                    .hasAnyRole("RM", "COMPLIANCE", "ADMIN")
                .pathMatchers("/api/clients/kyc/**")
                    .hasAnyRole("RM", "COMPLIANCE", "ADMIN")

                .pathMatchers(HttpMethod.GET, "/api/clients/*/risk-profile")
                    .hasAnyRole("CLIENT", "RM", "COMPLIANCE", "ADMIN")
                .pathMatchers("/api/clients/*/risk-profile")
                    .hasAnyRole("RM", "COMPLIANCE", "ADMIN")

                .pathMatchers("/api/clients/**")
                    .hasAnyRole("RM", "COMPLIANCE", "ADMIN")

                .pathMatchers("/api/suitability-rules/**")
                    .hasAnyRole("COMPLIANCE", "ADMIN")

                .pathMatchers("/api/aml-flags/**")
                    .hasAnyRole("COMPLIANCE", "ADMIN", "RM")

                // ── Analytics: Performance, Risk, Compliance ─────────────
                .pathMatchers("/api/analytics/**")
                    .hasAnyRole("CLIENT", "RM", "DEALER", "COMPLIANCE", "ADMIN")

                .pathMatchers("/api/compliance-breaches/**")
                    .hasAnyRole("COMPLIANCE", "ADMIN")

                // ── Orders: Execution & Allocations ──────────────────────
                .pathMatchers("/api/orders/**")
                    .hasAnyRole("CLIENT", "DEALER", "RM", "COMPLIANCE", "ADMIN")

                // ── Product Catalog: Securities, Terms, Research ──────────
                .pathMatchers("/api/securities/**")
                    .hasAnyRole("CLIENT", "RM", "DEALER", "COMPLIANCE", "ADMIN")

                .pathMatchers("/api/product-terms/**")
                    .hasAnyRole("RM", "DEALER", "COMPLIANCE", "ADMIN")

                .pathMatchers("/api/research-notes/**")
                    .hasAnyRole("CLIENT", "RM", "DEALER", "COMPLIANCE", "ADMIN")

                // ── Goals Advisory: Goals, Models, Recommendations ────────
                .pathMatchers("/api/goals/**")
                    .hasAnyRole("CLIENT", "RM", "ADMIN")

                .pathMatchers("/api/model-portfolios/**")
                    .hasAnyRole("RM", "ADMIN")

                .pathMatchers("/api/recommendations/**")
                    .hasAnyRole("CLIENT", "RM", "COMPLIANCE", "ADMIN")

                // ── Review: Reviews & Statements ─────────────────────────
                .pathMatchers("/api/reviews/**")
                    .hasAnyRole("CLIENT", "RM", "COMPLIANCE", "ADMIN")

                .pathMatchers("/api/statements/**")
                    .hasAnyRole("CLIENT", "RM", "COMPLIANCE", "ADMIN")

                // ── PBOR: Accounts, Holdings, Cash, Corporate Actions ─────
                .pathMatchers("/api/accounts/**")
                    .hasAnyRole("CLIENT", "RM", "DEALER", "COMPLIANCE", "ADMIN")

                .pathMatchers("/api/holdings/**")
                    .hasAnyRole("CLIENT", "RM", "DEALER", "COMPLIANCE", "ADMIN")

                .pathMatchers("/api/cash-ledger/**")
                    .hasAnyRole("CLIENT", "RM", "DEALER", "COMPLIANCE", "ADMIN")

                .pathMatchers("/api/corporate-actions/**")
                    .hasAnyRole("RM", "ADMIN")

                // ── Notifications ─────────────────────────────────────────
                .pathMatchers("/api/notifications/**")
                    .hasAnyRole("CLIENT", "RM", "DEALER", "COMPLIANCE", "ADMIN")

                // ── Fallback & Actuator ───────────────────────────────────
                .pathMatchers("/fallback/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()

                .anyExchange().authenticated()
            )
            .authenticationManager(am)
            .addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsAllowedOrigins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(Arrays.asList("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength 12 -> 2^12 = 4096 rounds. Slower than default 10 but ~4x harder
        // to brute-force. Login latency on modern hardware is still well under 300ms.
        return new BCryptPasswordEncoder(12);
    }
}