package wealthpro.springbootapigateway.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wealthpro.springbootapigateway.utility.JwtTokenUtil;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of tokens that have been invalidated via logout.
 * Uses an in-memory map of token → expiry time so that expired tokens
 * are automatically purged by the scheduled cleanup task.
 * In production replace with a Redis set with TTL.
 */
@Service
public class TokenBlocklistService {

    // token → expiry timestamp in millis
    private final Map<String, Long> blockedTokens = new ConcurrentHashMap<>();

    private final JwtTokenUtil jwtTokenUtil;

    public TokenBlocklistService(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public void block(String token) {
        try {
            Date expiry = jwtTokenUtil.getExpirationDateFromToken(token);
            blockedTokens.put(token, expiry.getTime());
        } catch (Exception e) {
            // If we can't parse the expiry, store with a 24-hour default TTL
            blockedTokens.put(token, System.currentTimeMillis() + 86_400_000L);
        }
    }

    public boolean isBlocked(String token) {
        return blockedTokens.containsKey(token);
    }

    /**
     * Runs every 30 minutes and removes tokens that have already expired.
     * An expired token is rejected by JWT validation before the blocklist
     * check anyway, so keeping them wastes memory.
     */
    @Scheduled(fixedDelay = 1_800_000)
    public void purgeExpiredTokens() {
        long now = System.currentTimeMillis();
        int before = blockedTokens.size();
        blockedTokens.entrySet().removeIf(e -> e.getValue() < now);
        int removed = before - blockedTokens.size();
        // purge complete — no console output in production
    }
}
