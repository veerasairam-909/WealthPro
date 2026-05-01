package wealthpro.springbootapigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration and update.
 * 'roles' is kept as String here so the API accepts any case input
 * (e.g. "rm", "RM", "Rm") — the controller validates and converts to Role enum.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String roles;   // Raw string from JSON — validated & converted to Role enum in controller
}
