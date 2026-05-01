package wealthpro.springbootapigateway.utility;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-seconds:18000}")
    private long expirationSeconds;

    // ── Generate Token ────────────────────────────────────────────────────────

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * Backwards-compatible — no userId / clientId claim embedded.
     * Kept for any call sites that do not yet have the numeric userId.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, null, null);
    }

    /**
     * Generate a token for STAFF users (RM, DEALER, COMPLIANCE, ADMIN).
     * Embeds the numeric 'userId' claim so the frontend can identify the user
     * without a separate API call — fixes the notification targeting problem.
     */
    public String generateStaffToken(UserDetails userDetails, Long userId) {
        return generateToken(userDetails, userId, null);
    }

    /**
     * Generate a token for CLIENT users.
     * Embeds the 'clientId' claim so downstream services can enforce
     * row-level ownership without a round-trip to the KYC service.
     */
    public String generateClientToken(UserDetails userDetails, Long clientId) {
        return generateToken(userDetails, null, clientId);
    }

    /**
     * Core token builder — called by all public overloads.
     * Either userId (staff) or clientId (client) is set; never both.
     */
    private String generateToken(UserDetails userDetails, Long userId, Long clientId) {
        Map<String, Object> claims = new HashMap<>();
        String roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(java.util.stream.Collectors.joining(","));
        claims.put("roles", roles);
        if (userId != null) {
            claims.put("userId", userId);
        }
        if (clientId != null) {
            claims.put("clientId", clientId);
        }
        return doGenerateToken(claims, userDetails.getUsername());
    }

    /**
     * @deprecated Use {@link #generateStaffToken} or {@link #generateClientToken}.
     * Kept for backwards compatibility with any callers that pass a Long.
     * Treats the Long as clientId (original semantics).
     */
    @Deprecated
    public String generateToken(UserDetails userDetails, Long clientId) {
        return generateClientToken(userDetails, clientId);
    }

    // ── Validate & Read Claims ────────────────────────────────────────────────

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /** Returns the 'roles' claim (comma-joined authority names), or "" if missing. */
    public String getRolesFromToken(String token) {
        Object roles = getAllClaimsFromToken(token).get("roles");
        return roles == null ? "" : roles.toString();
    }

    /** Returns the 'clientId' claim (for CLIENT users) or null for staff. */
    public Long getClientIdFromToken(String token) {
        Object cid = getAllClaimsFromToken(token).get("clientId");
        if (cid == null) return null;
        if (cid instanceof Number) return ((Number) cid).longValue();
        try { return Long.parseLong(cid.toString()); } catch (NumberFormatException e) { return null; }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
