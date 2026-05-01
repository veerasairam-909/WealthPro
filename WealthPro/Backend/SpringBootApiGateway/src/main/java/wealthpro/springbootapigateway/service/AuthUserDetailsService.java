package wealthpro.springbootapigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import wealthpro.springbootapigateway.repository.UsersRepository;
import wealthpro.springbootapigateway.security.WealthProUserDetails;

import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AuthUserDetailsService implements ReactiveUserDetailsService {

    @Autowired
    private UsersRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    // roles is stored as String in DB — use directly (no .name() needed)
                    String roleName = user.getRoles() != null ? user.getRoles().trim() : "";
                    // Spring Security expects authority strings prefixed with "ROLE_"
                    var authority = new SimpleGrantedAuthority("ROLE_" + roleName);
                    return (UserDetails) new WealthProUserDetails(
                            user.getUsername(),
                            user.getPassword(),
                            List.of(authority),
                            user.getUserId(),   // carried through — no second DB lookup needed
                            roleName
                    );
                });
    }
}
