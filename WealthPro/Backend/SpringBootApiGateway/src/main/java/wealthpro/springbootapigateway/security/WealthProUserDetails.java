package wealthpro.springbootapigateway.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Extended UserDetails that carries the numeric userId from the users table.
 *
 * Loaded once in AuthUserDetailsService during authentication.
 * Passed through the Authentication principal so AuthController can
 * read userId directly — no second DB query needed.
 */
public class WealthProUserDetails extends User {

    private final Long   userId;
    private final String roleName; // e.g. "RM", "COMPLIANCE", "ADMIN"

    public WealthProUserDetails(String username,
                                String password,
                                Collection<? extends GrantedAuthority> authorities,
                                Long   userId,
                                String roleName) {
        super(username, password, authorities);
        this.userId   = userId;
        this.roleName = roleName;
    }

    public Long   getUserId()   { return userId;   }
    public String getRoleName() { return roleName; }
}
