package wealthpro.springbootapigateway.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    // use a long enough secret for HS512 (at least 64 chars)
    private static final String TEST_SECRET =
            "mySuperSecretKey1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijkl";

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        // @Value fields need to be set manually in unit tests
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "expirationSeconds", 18000L);
    }

    // helper to build a UserDetails quickly
    private UserDetails makeUser(String username, String role) {
        return new User(
                username,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    // ─── generateStaffToken ────────────────────────────────────────────────────

    @Test
    void testGenerateStaffToken_UsernameExtractedCorrectly() {
        UserDetails user = makeUser("rm_john", "RM");
        String token = jwtTokenUtil.generateStaffToken(user, 42L);

        assertNotNull(token);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals("rm_john", username);
    }

    @Test
    void testGenerateStaffToken_RolesClaimIsPresent() {
        UserDetails user = makeUser("dealer1", "DEALER");
        String token = jwtTokenUtil.generateStaffToken(user, 5L);

        String roles = jwtTokenUtil.getRolesFromToken(token);
        assertTrue(roles.contains("ROLE_DEALER"));
    }

    @Test
    void testGenerateStaffToken_NoClientIdClaim() {
        UserDetails user = makeUser("rm_john", "RM");
        String token = jwtTokenUtil.generateStaffToken(user, 10L);

        // staff token should NOT have a clientId
        Long clientId = jwtTokenUtil.getClientIdFromToken(token);
        assertNull(clientId);
    }

    // ─── generateClientToken ──────────────────────────────────────────────────

    @Test
    void testGenerateClientToken_ClientIdExtractedCorrectly() {
        UserDetails user = makeUser("client_sara", "CLIENT");
        String token = jwtTokenUtil.generateClientToken(user, 99L);

        Long clientId = jwtTokenUtil.getClientIdFromToken(token);
        assertEquals(99L, clientId);
    }

    @Test
    void testGenerateClientToken_UsernameIsCorrect() {
        UserDetails user = makeUser("client_bob", "CLIENT");
        String token = jwtTokenUtil.generateClientToken(user, 77L);

        assertEquals("client_bob", jwtTokenUtil.getUsernameFromToken(token));
    }

    // ─── validateToken ────────────────────────────────────────────────────────

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        UserDetails user = makeUser("admin_user", "ADMIN");
        String token = jwtTokenUtil.generateToken(user);

        boolean isValid = jwtTokenUtil.validateToken(token, user);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WrongUsername_ReturnsFalse() {
        UserDetails user1 = makeUser("user1", "RM");
        UserDetails user2 = makeUser("user2", "RM");

        String token = jwtTokenUtil.generateToken(user1); // token for user1

        // validate against user2 — should fail because usernames don't match
        boolean isValid = jwtTokenUtil.validateToken(token, user2);
        assertFalse(isValid);
    }

    // ─── getExpirationDateFromToken ───────────────────────────────────────────

    @Test
    void testGetExpirationDate_IsFutureDate() {
        UserDetails user = makeUser("someone", "CLIENT");
        String token = jwtTokenUtil.generateToken(user);

        Date expiry = jwtTokenUtil.getExpirationDateFromToken(token);
        assertNotNull(expiry);
        // expiry should be in the future
        assertTrue(expiry.after(new Date()));
    }

    // ─── getRolesFromToken ────────────────────────────────────────────────────

    @Test
    void testGetRolesFromToken_MultipleRoles() {
        // user with two authorities
        UserDetails user = new User(
                "multi_role_user",
                "pass",
                List.of(
                        new SimpleGrantedAuthority("ROLE_RM"),
                        new SimpleGrantedAuthority("ROLE_COMPLIANCE")
                )
        );
        String token = jwtTokenUtil.generateToken(user);

        String roles = jwtTokenUtil.getRolesFromToken(token);
        assertTrue(roles.contains("ROLE_RM"));
        assertTrue(roles.contains("ROLE_COMPLIANCE"));
    }

    @Test
    void testGetRolesFromToken_NoAuthorities_ReturnsEmpty() {
        // user with no authorities at all
        UserDetails user = new User("noRoleUser", "pass", List.of());
        String token = jwtTokenUtil.generateToken(user);

        String roles = jwtTokenUtil.getRolesFromToken(token);
        assertEquals("", roles);
    }

    // ─── getClientIdFromToken edge cases ──────────────────────────────────────

    @Test
    void testGetClientIdFromToken_WhenNotPresent_ReturnsNull() {
        // plain generateToken without clientId
        UserDetails user = makeUser("staff_user", "RM");
        String token = jwtTokenUtil.generateToken(user);

        assertNull(jwtTokenUtil.getClientIdFromToken(token));
    }
}
