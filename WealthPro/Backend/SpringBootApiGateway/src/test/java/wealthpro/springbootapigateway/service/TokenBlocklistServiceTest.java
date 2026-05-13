package wealthpro.springbootapigateway.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import wealthpro.springbootapigateway.utility.JwtTokenUtil;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenBlocklistServiceTest {

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private TokenBlocklistService tokenBlocklistService;

    // ─── block + isBlocked ────────────────────────────────────────────────────

    @Test
    void testBlock_ThenIsBlocked_ReturnsTrue() {
        String token = "some.valid.token";
        // mock expiry 1 hour from now
        Date futureDate = new Date(System.currentTimeMillis() + 3600_000L);
        when(jwtTokenUtil.getExpirationDateFromToken(token)).thenReturn(futureDate);

        tokenBlocklistService.block(token);

        assertTrue(tokenBlocklistService.isBlocked(token));
    }

    @Test
    void testIsBlocked_TokenNotBlocked_ReturnsFalse() {
        // we never called block(), so any token should return false
        assertFalse(tokenBlocklistService.isBlocked("completely.random.token"));
    }

    @Test
    void testBlock_WhenJwtParseFails_StillBlocksWithDefaultTTL() {
        String badToken = "not.a.real.jwt.token";
        // simulate parse failure
        when(jwtTokenUtil.getExpirationDateFromToken(badToken))
                .thenThrow(new RuntimeException("Cannot parse token"));

        // should NOT throw — falls back to 24h TTL
        tokenBlocklistService.block(badToken);

        assertTrue(tokenBlocklistService.isBlocked(badToken));
    }

    @Test
    void testBlock_MultipleTokens_AllShowAsBlocked() {
        String token1 = "token.one";
        String token2 = "token.two";
        String token3 = "token.three";

        Date futureDate = new Date(System.currentTimeMillis() + 3600_000L);
        when(jwtTokenUtil.getExpirationDateFromToken(any())).thenReturn(futureDate);

        tokenBlocklistService.block(token1);
        tokenBlocklistService.block(token2);
        tokenBlocklistService.block(token3);

        assertTrue(tokenBlocklistService.isBlocked(token1));
        assertTrue(tokenBlocklistService.isBlocked(token2));
        assertTrue(tokenBlocklistService.isBlocked(token3));
    }

    // ─── purgeExpiredTokens ───────────────────────────────────────────────────

    @Test
    void testPurgeExpiredTokens_RemovesExpiredOnes() {
        // inject a map with one expired and one still-valid token
        Map<String, Long> blockedTokens = new ConcurrentHashMap<>();
        blockedTokens.put("expired.token", System.currentTimeMillis() - 5000); // already past
        blockedTokens.put("valid.token",   System.currentTimeMillis() + 99999); // still future

        ReflectionTestUtils.setField(tokenBlocklistService, "blockedTokens", blockedTokens);

        tokenBlocklistService.purgeExpiredTokens();

        assertFalse(blockedTokens.containsKey("expired.token"), "expired token should be removed");
        assertTrue(blockedTokens.containsKey("valid.token"),    "valid token should remain");
    }

    @Test
    void testPurgeExpiredTokens_EmptyMap_NothingBreaks() {
        // just make sure an empty blocklist doesn't crash
        Map<String, Long> emptyMap = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(tokenBlocklistService, "blockedTokens", emptyMap);

        // should complete without exception
        assertDoesNotThrow(() -> tokenBlocklistService.purgeExpiredTokens());
    }

    @Test
    void testIsBlocked_AfterPurge_ExpiredTokenIsRemoved() {
        // put an expired token directly in the map
        Map<String, Long> blockedTokens = new ConcurrentHashMap<>();
        blockedTokens.put("old.expired.token", System.currentTimeMillis() - 1000);
        ReflectionTestUtils.setField(tokenBlocklistService, "blockedTokens", blockedTokens);

        // before purge it should appear blocked
        assertTrue(tokenBlocklistService.isBlocked("old.expired.token"));

        // after purge it should be gone
        tokenBlocklistService.purgeExpiredTokens();
        assertFalse(tokenBlocklistService.isBlocked("old.expired.token"));
    }
}
