package wealthpro.springbootapigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned by POST /auth/login.
 *
 * Includes the JWT token plus the resolved userId and role so the
 * frontend can store them immediately — without waiting for the
 * admin to visit the Users page or parsing the JWT sub claim.
 *
 * Frontend (src/api/auth.ts) already handles both:
 *   - legacy raw-string token  → typeof res.data === 'string'
 *   - this object              → res.data.token + res.data.userId
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long   userId;   // Numeric userId from the users table — null if lookup fails
    private String role;     // Role name string, e.g. "RM", "COMPLIANCE"
}
