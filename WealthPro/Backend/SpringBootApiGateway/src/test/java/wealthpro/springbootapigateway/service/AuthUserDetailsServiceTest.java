package wealthpro.springbootapigateway.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wealthpro.springbootapigateway.entities.Users;
import wealthpro.springbootapigateway.repository.UsersRepository;
import wealthpro.springbootapigateway.security.WealthProUserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthUserDetailsServiceTest {

    @Mock
    private UsersRepository userRepository;

    @InjectMocks
    private AuthUserDetailsService authUserDetailsService;

    // helper to build a Users entity
    private Users buildUsersEntity(String username, String role, Long userId) {
        Users user = new Users();
        user.setUsername(username);
        user.setPassword("$2a$10$hashedPassword");
        user.setRoles(role);
        user.setUserId(userId);
        user.setName("Test User");
        user.setEmail("test@email.com");
        user.setPhone("9999999999");
        return user;
    }

    // ─── findByUsername success ───────────────────────────────────────────────

    @Test
    void testFindByUsername_MapsToWealthProUserDetails() {
        Users user = buildUsersEntity("rm_john", "RM", 10L);
        when(userRepository.findByUsername("rm_john")).thenReturn(Mono.just(user));

        Mono<UserDetails> result = authUserDetailsService.findByUsername("rm_john");

        StepVerifier.create(result)
                .assertNext(ud -> {
                    // result should be our custom WealthProUserDetails
                    assertInstanceOf(WealthProUserDetails.class, ud);
                    WealthProUserDetails wp = (WealthProUserDetails) ud;

                    assertEquals("rm_john", wp.getUsername());
                    assertEquals(10L, wp.getUserId());
                    assertEquals("RM", wp.getRoleName());

                    // Spring Security expects "ROLE_RM" authority
                    assertTrue(wp.getAuthorities().stream()
                            .anyMatch(a -> "ROLE_RM".equals(a.getAuthority())));
                })
                .verifyComplete();
    }

    @Test
    void testFindByUsername_ClientRole_MapsCorrectly() {
        Users user = buildUsersEntity("client_sara", "CLIENT", 55L);
        when(userRepository.findByUsername("client_sara")).thenReturn(Mono.just(user));

        Mono<UserDetails> result = authUserDetailsService.findByUsername("client_sara");

        StepVerifier.create(result)
                .assertNext(ud -> {
                    WealthProUserDetails wp = (WealthProUserDetails) ud;
                    assertEquals("CLIENT", wp.getRoleName());
                    assertTrue(wp.getAuthorities().stream()
                            .anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority())));
                })
                .verifyComplete();
    }

    // ─── findByUsername not found ─────────────────────────────────────────────

    @Test
    void testFindByUsername_UserNotFound_ReturnsEmpty() {
        when(userRepository.findByUsername("ghost_user")).thenReturn(Mono.empty());

        Mono<UserDetails> result = authUserDetailsService.findByUsername("ghost_user");

        // should complete with no items emitted
        StepVerifier.create(result)
                .verifyComplete();
    }

    // ─── null role edge case ──────────────────────────────────────────────────

    @Test
    void testFindByUsername_NullRole_UsesEmptyString() {
        Users user = buildUsersEntity("someuser", null, 5L);
        when(userRepository.findByUsername("someuser")).thenReturn(Mono.just(user));

        Mono<UserDetails> result = authUserDetailsService.findByUsername("someuser");

        StepVerifier.create(result)
                .assertNext(ud -> {
                    WealthProUserDetails wp = (WealthProUserDetails) ud;
                    // null role should be treated as empty string
                    assertEquals("", wp.getRoleName());
                    // authority should be "ROLE_"
                    assertTrue(wp.getAuthorities().stream()
                            .anyMatch(a -> "ROLE_".equals(a.getAuthority())));
                })
                .verifyComplete();
    }

    @Test
    void testFindByUsername_VerifyRepositoryCalledOnce() {
        Users user = buildUsersEntity("dealer1", "DEALER", 20L);
        when(userRepository.findByUsername("dealer1")).thenReturn(Mono.just(user));

        authUserDetailsService.findByUsername("dealer1").block();

        verify(userRepository, times(1)).findByUsername("dealer1");
    }
}
