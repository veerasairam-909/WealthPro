package wealthpro.springbootapigateway.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wealthpro.springbootapigateway.client.WealthproClient;
import wealthpro.springbootapigateway.dto.AuthRequest;
import wealthpro.springbootapigateway.dto.AuthResponse;
import wealthpro.springbootapigateway.security.WealthProUserDetails;
import wealthpro.springbootapigateway.service.TokenBlocklistService;
import wealthpro.springbootapigateway.utility.JwtTokenUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private ReactiveAuthenticationManager authenticationManager;

    @Mock
    private JwtTokenUtil jwtService;

    @Mock
    private WealthproClient wealthproClient;

    @Mock
    private TokenBlocklistService tokenBlocklistService;

    @InjectMocks
    private AuthController authController;

    // ─── helper to build WealthProUserDetails ─────────────────────────────────

    private WealthProUserDetails buildStaffPrincipal(String username, String role, Long userId) {
        return new WealthProUserDetails(
                username,
                "encoded_pass",
                List.of(new SimpleGrantedAuthority("ROLE_" + role)),
                userId,
                role
        );
    }

    private WealthProUserDetails buildClientPrincipal(String username) {
        return new WealthProUserDetails(
                username,
                "encoded_pass",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")),
                null,
                "CLIENT"
        );
    }

    // ─── login — staff user ───────────────────────────────────────────────────

    @Test
    void testLogin_StaffUser_ReturnsAuthResponseWithUserId() {
        AuthRequest request = new AuthRequest("rm_john", "password");
        WealthProUserDetails principal = buildStaffPrincipal("rm_john", "RM", 42L);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(authToken));
        when(jwtService.generateStaffToken(principal, 42L)).thenReturn("staff.jwt.token");

        Mono<AuthResponse> result = authController.login(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("staff.jwt.token", response.getToken());
                    assertEquals(42L, response.getUserId());
                    assertEquals("RM", response.getRole());
                })
                .verifyComplete();
    }

    @Test
    void testLogin_ComplianceUser_ReturnsCorrectRole() {
        AuthRequest request = new AuthRequest("comp1", "password");
        WealthProUserDetails principal = buildStaffPrincipal("comp1", "COMPLIANCE", 10L);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(authToken));
        when(jwtService.generateStaffToken(principal, 10L)).thenReturn("compliance.jwt.token");

        Mono<AuthResponse> result = authController.login(request);

        StepVerifier.create(result)
                .assertNext(response -> assertEquals("COMPLIANCE", response.getRole()))
                .verifyComplete();
    }

    // ─── login — client user ──────────────────────────────────────────────────

    @Test
    void testLogin_ClientUser_ResolvesClientIdAndReturnsToken() {
        AuthRequest request = new AuthRequest("client_sara", "password");
        WealthProUserDetails principal = buildClientPrincipal("client_sara");

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(authToken));
        when(wealthproClient.resolveClientIdByUsername("client_sara")).thenReturn(Mono.just(99L));
        when(jwtService.generateClientToken(principal, 99L)).thenReturn("client.jwt.token");

        Mono<AuthResponse> result = authController.login(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("client.jwt.token", response.getToken());
                    assertEquals("CLIENT", response.getRole());
                })
                .verifyComplete();
    }

    @Test
    void testLogin_ClientUser_WealthproLookupFails_FallbackToken() {
        // if the Wealthpro lookup returns empty, should still return a token
        AuthRequest request = new AuthRequest("client_bob", "password");
        WealthProUserDetails principal = buildClientPrincipal("client_bob");

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(authToken));
        when(wealthproClient.resolveClientIdByUsername("client_bob")).thenReturn(Mono.empty());
        when(jwtService.generateToken(principal)).thenReturn("fallback.jwt.token");

        Mono<AuthResponse> result = authController.login(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("fallback.jwt.token", response.getToken());
                    assertEquals("CLIENT", response.getRole());
                })
                .verifyComplete();
    }

    // ─── login — bad credentials ──────────────────────────────────────────────

    @Test
    void testLogin_BadCredentials_Returns401() {
        AuthRequest request = new AuthRequest("wrong_user", "wrong_pass");

        when(authenticationManager.authenticate(any()))
                .thenReturn(Mono.error(new BadCredentialsException("Bad credentials")));

        Mono<AuthResponse> result = authController.login(request);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.UNAUTHORIZED)
                .verify();
    }

    // ─── logout ───────────────────────────────────────────────────────────────

    @Test
    void testLogout_WithBearerToken_BlocksTokenAndReturns200() {
        // build a mock request that has Authorization: Bearer <token>
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer my.jwt.token.here")
                .build();

        Mono<ResponseEntity<String>> result = authController.logout(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertTrue(response.getBody().contains("Logged out"));
                })
                .verifyComplete();

        // the token should have been blocked
        verify(tokenBlocklistService, times(1)).block("my.jwt.token.here");
    }

    @Test
    void testLogout_WithoutAuthorizationHeader_StillReturns200() {
        // no Authorization header at all
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/auth/logout")
                .build();

        Mono<ResponseEntity<String>> result = authController.logout(request);

        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.getStatusCode()))
                .verifyComplete();

        // block() should NOT be called because there's no token
        verify(tokenBlocklistService, never()).block(any());
    }

    @Test
    void testLogout_WithNonBearerAuthHeader_DoesNotBlock() {
        // Authorization header that doesn't start with "Bearer "
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();

        Mono<ResponseEntity<String>> result = authController.logout(request);

        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.getStatusCode()))
                .verifyComplete();

        verify(tokenBlocklistService, never()).block(any());
    }
}
