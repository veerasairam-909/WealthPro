package wealthpro.springbootapigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user data.
 * 'roles' is returned as a plain string (e.g. "RM", "CLIENT", "ADMIN").
 * Using String (not Role enum) means unknown legacy role values in the DB
 * are passed through as-is rather than causing deserialization errors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long   userId;   // Numeric surrogate key — used by frontend for notification targeting
    private String username;
    private String name;
    private String email;
    private String phone;
    private String roles;    // Plain string — any DB value is safe (e.g. "RM", "ADVISOR", "CLIENT")
}
