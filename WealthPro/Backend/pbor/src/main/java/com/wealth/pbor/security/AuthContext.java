package com.wealth.pbor.security;

/**
 * Reads the X-Auth-* headers injected by the API Gateway and answers
 * row-level ownership questions.
 *
 * Contract (set by the gateway's JwtAuthenticationFilter):
 *   X-Auth-Username   — username from the validated JWT subject
 *   X-Auth-Roles      — comma-joined authority list (e.g. "ROLE_CLIENT")
 *   X-Auth-Client-Id  — numeric clientId (present only for CLIENT users)
 */
public class AuthContext {

    public static final String HDR_USERNAME  = "X-Auth-Username";
    public static final String HDR_ROLES     = "X-Auth-Roles";
    public static final String HDR_CLIENT_ID = "X-Auth-Client-Id";

    private final String username;
    private final String roles;
    private final Long clientId;

    public AuthContext(String username, String roles, Long clientId) {
        this.username = username;
        this.roles    = roles == null ? "" : roles;
        this.clientId = clientId;
    }

    public String getUsername() { return username; }
    public Long   getClientId() { return clientId; }

    public boolean isClient() {
        return roles.contains("ROLE_CLIENT") && !isStaff();
    }

    public boolean isStaff() {
        return roles.contains("ROLE_ADMIN")
            || roles.contains("ROLE_RM")
            || roles.contains("ROLE_DEALER")
            || roles.contains("ROLE_COMPLIANCE");
    }

    public boolean ownsClient(Long requestedClientId) {
        return clientId != null && clientId.equals(requestedClientId);
    }
}
