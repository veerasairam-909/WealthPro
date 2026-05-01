package com.wealthpro.notifications.security;

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
    public boolean isClient() { return roles.contains("ROLE_CLIENT") && !isStaff(); }
    public boolean isStaff() {
        return roles.contains("ROLE_ADMIN") || roles.contains("ROLE_RM")
            || roles.contains("ROLE_DEALER") || roles.contains("ROLE_COMPLIANCE");
    }
    public boolean ownsClient(Long requested) { return clientId != null && clientId.equals(requested); }
}
