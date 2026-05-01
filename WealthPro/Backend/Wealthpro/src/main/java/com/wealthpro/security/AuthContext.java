package com.wealthpro.security;

/**
 * Helper that reads the X-Auth-* headers injected by the API Gateway
 * and answers row-level ownership questions.
 *
 * Header contract (set by gateway's JwtAuthenticationFilter):
 *   X-Auth-Username   — username from the validated JWT subject
 *   X-Auth-Roles      — comma-joined authority list (e.g. "ROLE_CLIENT")
 *   X-Auth-Client-Id  — numeric clientId (present only for CLIENT users)
 *
 * Controllers are expected to inject these via @RequestHeader(required=false)
 * and construct an AuthContext to gate access.
 */
public class AuthContext {

    public static final String HDR_USERNAME  = "X-Auth-Username";
    public static final String HDR_ROLES     = "X-Auth-Roles";
    public static final String HDR_CLIENT_ID = "X-Auth-Client-Id";

    private final String username;
    private final String roles;    // raw comma-joined, e.g. "ROLE_CLIENT"
    private final Long clientId;

    public AuthContext(String username, String roles, Long clientId) {
        this.username = username;
        this.roles    = roles == null ? "" : roles;
        this.clientId = clientId;
    }

    public String getUsername() { return username; }
    public Long   getClientId() { return clientId; }

    /** True if the caller has ROLE_CLIENT (and no staff role that would override ownership). */
    public boolean isClient() {
        return roles.contains("ROLE_CLIENT") && !isStaff();
    }

    /** Staff roles bypass ownership checks — they can see any client's data. */
    public boolean isStaff() {
        return roles.contains("ROLE_ADMIN")
            || roles.contains("ROLE_RM")
            || roles.contains("ROLE_DEALER")
            || roles.contains("ROLE_COMPLIANCE");
    }

    /** True if the authenticated CLIENT owns the given clientId. */
    public boolean ownsClient(Long requestedClientId) {
        return clientId != null && clientId.equals(requestedClientId);
    }
}
