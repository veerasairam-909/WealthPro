package wealthpro.springbootapigateway.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wealthpro.springbootapigateway.dto.UserRegistrationRequest;
import wealthpro.springbootapigateway.dto.UserResponse;
import wealthpro.springbootapigateway.entities.Users;
import wealthpro.springbootapigateway.repository.UsersRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    // ─── helpers ──────────────────────────────────────────────────────────────

    private Users buildUser(String username, String role, Long userId) {
        Users user = new Users();
        user.setUsername(username);
        user.setPassword("encoded_password");
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPhone("9876543210");
        user.setRoles(role);
        user.setUserId(userId);
        return user;
    }

    private UserRegistrationRequest buildRequest(String username, String password, String role) {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername(username);
        req.setPassword(password);
        req.setName("Test Name");
        req.setEmail("email@test.com");
        req.setPhone("1234567890");
        req.setRoles(role);
        return req;
    }

    // ─── registerUser — validation errors ─────────────────────────────────────

    @Test
    void testRegisterUser_BlankUsername_Returns400() {
        UserRegistrationRequest req = buildRequest("", "pass123", "RM");

        Mono<UserResponse> result = userController.registerUser(req);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void testRegisterUser_NullUsername_Returns400() {
        UserRegistrationRequest req = buildRequest(null, "pass123", "RM");

        Mono<UserResponse> result = userController.registerUser(req);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void testRegisterUser_BlankPassword_Returns400() {
        UserRegistrationRequest req = buildRequest("rm_john", "  ", "RM");

        Mono<UserResponse> result = userController.registerUser(req);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void testRegisterUser_InvalidRole_Returns400() {
        UserRegistrationRequest req = buildRequest("rm_john", "pass123", "SUPER_ADMIN");

        Mono<UserResponse> result = userController.registerUser(req);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void testRegisterUser_AdminRole_Returns403() {
        UserRegistrationRequest req = buildRequest("new_admin", "pass123", "ADMIN");

        Mono<UserResponse> result = userController.registerUser(req);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.FORBIDDEN)
                .verify();
    }

    // ─── registerUser — duplicate username ────────────────────────────────────

    @Test
    void testRegisterUser_DuplicateUsername_Returns409() {
        UserRegistrationRequest req = buildRequest("existing_user", "pass123", "RM");
        Users existingUser = buildUser("existing_user", "RM", 1L);

        // findByUsername returns an existing user → conflict
        when(usersRepository.findByUsername("existing_user"))
                .thenReturn(Mono.just(existingUser));

        Mono<UserResponse> result = userController.registerUser(req);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.CONFLICT)
                .verify();
    }

    // ─── registerUser — success ───────────────────────────────────────────────

    @Test
    void testRegisterUser_Success_ReturnsUserResponse() {
        UserRegistrationRequest req = buildRequest("new_rm", "password123", "RM");
        Users savedUser = buildUser("new_rm", "RM", 10L);

        // first call: no duplicate found; second call: re-fetch after save
        when(usersRepository.findByUsername("new_rm"))
                .thenReturn(Mono.empty())
                .thenReturn(Mono.just(savedUser));
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(usersRepository.save(any(Users.class))).thenReturn(Mono.just(savedUser));

        Mono<UserResponse> result = userController.registerUser(req);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("new_rm", response.getUsername());
                    assertEquals("RM", response.getRoles());
                    assertEquals(10L, response.getUserId());
                })
                .verifyComplete();
    }

    @Test
    void testRegisterUser_NoRoleProvided_DefaultsToClient() {
        // if roles field is null, controller defaults to "CLIENT"
        UserRegistrationRequest req = buildRequest("new_client", "pass123", null);
        Users savedUser = buildUser("new_client", "CLIENT", 20L);

        when(usersRepository.findByUsername("new_client"))
                .thenReturn(Mono.empty())
                .thenReturn(Mono.just(savedUser));
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(usersRepository.save(any(Users.class))).thenReturn(Mono.just(savedUser));

        Mono<UserResponse> result = userController.registerUser(req);

        StepVerifier.create(result)
                .assertNext(response -> assertEquals("CLIENT", response.getRoles()))
                .verifyComplete();
    }

    // ─── getAllUsers ──────────────────────────────────────────────────────────

    @Test
    void testGetAllUsers_ReturnsList() {
        Users u1 = buildUser("user1", "RM", 1L);
        Users u2 = buildUser("user2", "DEALER", 2L);

        when(usersRepository.findAll()).thenReturn(Flux.fromIterable(List.of(u1, u2)));

        Mono<List<UserResponse>> result = userController.getAllUsers();

        StepVerifier.create(result)
                .assertNext(list -> {
                    assertNotNull(list);
                    assertEquals(2, list.size());
                })
                .verifyComplete();
    }

    @Test
    void testGetAllUsers_EmptyDB_ReturnsEmptyList() {
        when(usersRepository.findAll()).thenReturn(Flux.empty());

        Mono<List<UserResponse>> result = userController.getAllUsers();

        StepVerifier.create(result)
                .assertNext(list -> assertEquals(0, list.size()))
                .verifyComplete();
    }

    // ─── getUserByUsername ────────────────────────────────────────────────────

    @Test
    void testGetUserByUsername_Success() {
        Users user = buildUser("rm_john", "RM", 5L);
        when(usersRepository.findByUsername("rm_john")).thenReturn(Mono.just(user));

        Mono<UserResponse> result = userController.getUserByUsername("rm_john");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("rm_john", response.getUsername());
                    assertEquals("RM", response.getRoles());
                })
                .verifyComplete();
    }

    @Test
    void testGetUserByUsername_NotFound_Returns404() {
        when(usersRepository.findByUsername("ghost")).thenReturn(Mono.empty());

        Mono<UserResponse> result = userController.getUserByUsername("ghost");

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.NOT_FOUND)
                .verify();
    }

    // ─── updateUser ───────────────────────────────────────────────────────────

    @Test
    void testUpdateUser_AdminAccount_Returns403() {
        UserRegistrationRequest req = buildRequest("admin", "newpass", "RM");

        // admin account is immutable
        Mono<UserResponse> result = userController.updateUser("admin", req);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void testUpdateUser_AdminAccountCaseInsensitive_Returns403() {
        UserRegistrationRequest req = buildRequest("ADMIN", "newpass", "RM");

        Mono<UserResponse> result = userController.updateUser("ADMIN", req);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void testUpdateUser_UserNotFound_Returns404() {
        UserRegistrationRequest req = buildRequest("ghost_user", "newpass", "RM");
        when(usersRepository.findByUsername("ghost_user")).thenReturn(Mono.empty());

        Mono<UserResponse> result = userController.updateUser("ghost_user", req);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    void testUpdateUser_Success() {
        Users existingUser = buildUser("rm_john", "RM", 5L);
        Users updatedUser  = buildUser("rm_john", "DEALER", 5L);

        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setRoles("DEALER"); // just changing role

        when(usersRepository.findByUsername("rm_john"))
                .thenReturn(Mono.just(existingUser))
                .thenReturn(Mono.just(updatedUser));  // re-fetch after save
        when(usersRepository.save(any(Users.class))).thenReturn(Mono.just(updatedUser));

        Mono<UserResponse> result = userController.updateUser("rm_john", req);

        StepVerifier.create(result)
                .assertNext(response -> assertEquals("DEALER", response.getRoles()))
                .verifyComplete();
    }

    @Test
    void testUpdateUser_InvalidRole_Returns400() {
        UserRegistrationRequest req = buildRequest("rm_john", "pass", "INVALID_ROLE");

        Mono<UserResponse> result = userController.updateUser("rm_john", req);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    // ─── deleteUser ───────────────────────────────────────────────────────────

    @Test
    void testDeleteUser_AdminAccount_Returns403() {
        Mono<Void> result = userController.deleteUser("admin");

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void testDeleteUser_NotFound_Returns404() {
        when(usersRepository.findByUsername("nobody")).thenReturn(Mono.empty());

        Mono<Void> result = userController.deleteUser("nobody");

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    void testDeleteUser_Success_Completes() {
        Users user = buildUser("rm_john", "RM", 5L);
        when(usersRepository.findByUsername("rm_john")).thenReturn(Mono.just(user));
        when(usersRepository.delete(user)).thenReturn(Mono.empty());

        Mono<Void> result = userController.deleteUser("rm_john");

        StepVerifier.create(result)
                .verifyComplete();

        verify(usersRepository, times(1)).delete(user);
    }
}
