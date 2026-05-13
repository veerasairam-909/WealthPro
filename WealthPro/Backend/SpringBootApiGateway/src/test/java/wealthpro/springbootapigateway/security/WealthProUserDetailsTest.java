package wealthpro.springbootapigateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WealthProUserDetailsTest {

    // ─── getUserId ────────────────────────────────────────────────────────────

    @Test
    void testGetUserId_ReturnsCorrectLong() {
        WealthProUserDetails details = new WealthProUserDetails(
                "rm_john",
                "hashedpass",
                List.of(new SimpleGrantedAuthority("ROLE_RM")),
                100L,
                "RM"
        );

        assertEquals(100L, details.getUserId());
    }

    @Test
    void testGetUserId_CanBeNull_ForClientUsers() {
        // CLIENT users may not have a userId — only a clientId
        WealthProUserDetails details = new WealthProUserDetails(
                "client_sara",
                "pass",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")),
                null,  // no userId for client
                "CLIENT"
        );

        assertNull(details.getUserId());
    }

    // ─── getRoleName ──────────────────────────────────────────────────────────

    @Test
    void testGetRoleName_ReturnsCorrectString() {
        WealthProUserDetails details = new WealthProUserDetails(
                "comp1",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_COMPLIANCE")),
                200L,
                "COMPLIANCE"
        );

        assertEquals("COMPLIANCE", details.getRoleName());
    }

    @Test
    void testGetRoleName_ForAdmin() {
        WealthProUserDetails details = new WealthProUserDetails(
                "admin",
                "adminpass",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                1L,
                "ADMIN"
        );

        assertEquals("ADMIN", details.getRoleName());
    }

    // ─── inherited UserDetails methods ────────────────────────────────────────

    @Test
    void testGetUsername_InheritedFromParent() {
        WealthProUserDetails details = new WealthProUserDetails(
                "dealer_bob",
                "pass",
                List.of(new SimpleGrantedAuthority("ROLE_DEALER")),
                50L,
                "DEALER"
        );

        assertEquals("dealer_bob", details.getUsername());
    }

    @Test
    void testGetAuthorities_ContainsExpectedAuthority() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_RM");
        WealthProUserDetails details = new WealthProUserDetails(
                "rm_user",
                "pass",
                List.of(authority),
                7L,
                "RM"
        );

        assertTrue(details.getAuthorities().contains(authority));
    }

    @Test
    void testIsAccountNonExpired_DefaultTrue() {
        WealthProUserDetails details = new WealthProUserDetails(
                "testuser",
                "pass",
                List.of(new SimpleGrantedAuthority("ROLE_RM")),
                3L,
                "RM"
        );

        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }
}
