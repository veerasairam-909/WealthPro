package com.wealthpro.analytics.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthContext — role detection and ownership checks.
 */
public class AuthContextTest {

    // ─── isStaff ──────────────────────────────────────────────────────────────

    @Test
    void testIsStaff_AdminRole_ReturnsTrue() {
        AuthContext ctx = new AuthContext("admin", "ROLE_ADMIN", null);
        assertTrue(ctx.isStaff());
    }

    @Test
    void testIsStaff_RMRole_ReturnsTrue() {
        AuthContext ctx = new AuthContext("rm1", "ROLE_RM", null);
        assertTrue(ctx.isStaff());
    }

    @Test
    void testIsStaff_DealerRole_ReturnsTrue() {
        AuthContext ctx = new AuthContext("dealer1", "ROLE_DEALER", null);
        assertTrue(ctx.isStaff());
    }

    @Test
    void testIsStaff_ComplianceRole_ReturnsTrue() {
        AuthContext ctx = new AuthContext("comp1", "ROLE_COMPLIANCE", null);
        assertTrue(ctx.isStaff());
    }

    @Test
    void testIsStaff_ClientRole_ReturnsFalse() {
        AuthContext ctx = new AuthContext("client1", "ROLE_CLIENT", 5L);
        assertFalse(ctx.isStaff());
    }

    @Test
    void testIsStaff_NoRole_ReturnsFalse() {
        AuthContext ctx = new AuthContext("unknown", null, null);
        assertFalse(ctx.isStaff());
    }

    // ─── isClient ─────────────────────────────────────────────────────────────

    @Test
    void testIsClient_ClientRole_ReturnsTrue() {
        AuthContext ctx = new AuthContext("client1", "ROLE_CLIENT", 5L);
        assertTrue(ctx.isClient());
    }

    @Test
    void testIsClient_StaffRole_ReturnsFalse() {
        // staff roles are not clients even if ROLE_CLIENT is somehow also present
        AuthContext ctx = new AuthContext("rm1", "ROLE_RM", null);
        assertFalse(ctx.isClient());
    }

    @Test
    void testIsClient_NoRole_ReturnsFalse() {
        AuthContext ctx = new AuthContext("user", null, null);
        assertFalse(ctx.isClient());
    }

    // ─── ownsClient ───────────────────────────────────────────────────────────

    @Test
    void testOwnsClient_SameId_ReturnsTrue() {
        AuthContext ctx = new AuthContext("client1", "ROLE_CLIENT", 42L);
        assertTrue(ctx.ownsClient(42L));
    }

    @Test
    void testOwnsClient_DifferentId_ReturnsFalse() {
        AuthContext ctx = new AuthContext("client1", "ROLE_CLIENT", 42L);
        assertFalse(ctx.ownsClient(99L));
    }

    @Test
    void testOwnsClient_NullClientId_ReturnsFalse() {
        AuthContext ctx = new AuthContext("client1", "ROLE_CLIENT", null);
        assertFalse(ctx.ownsClient(42L));
    }

    // ─── getters ──────────────────────────────────────────────────────────────

    @Test
    void testGetUsername() {
        AuthContext ctx = new AuthContext("john_rm", "ROLE_RM", null);
        assertEquals("john_rm", ctx.getUsername());
    }

    @Test
    void testGetClientId() {
        AuthContext ctx = new AuthContext("client1", "ROLE_CLIENT", 77L);
        assertEquals(77L, ctx.getClientId());
    }

    @Test
    void testGetClientId_NullIfNotClient() {
        AuthContext ctx = new AuthContext("rm1", "ROLE_RM", null);
        assertNull(ctx.getClientId());
    }
}
