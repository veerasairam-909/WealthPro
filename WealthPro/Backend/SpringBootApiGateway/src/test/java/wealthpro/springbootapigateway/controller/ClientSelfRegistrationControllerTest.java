package wealthpro.springbootapigateway.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wealthpro.springbootapigateway.client.WealthproClient;
import wealthpro.springbootapigateway.dto.UserRegistrationRequest;
import wealthpro.springbootapigateway.dto.UserResponse;
import wealthpro.springbootapigateway.entities.Users;
import wealthpro.springbootapigateway.repository.UsersRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientSelfRegistrationControllerTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WealthproClient wealthproClient;

    @InjectMocks
    private ClientSelfRegistrationController controller;

    // ─── helpers ──────────────────────────────────────────────────────────────

    private UserRegistrationRequest buildRequest(String username, String password,
                                                  String name, String email) {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername(username);
        req.setPassword(password);
        req.setName(name);
        req.setEmail(email);
        req.setPhone("9876543210");
        return req;
    }

    private Users buildSavedUser(String username) {
        Users user = new Users();
        user.setUsername(username);
        user.setPassword("encoded_pass");
        user.setName("Test Client");
        user.setEmail("client@example.com");
        user.setPhone("9876543210");
        user.setRoles("CLIENT");
        user.setUserId(101L);
        return user;
    }

    // ─── validation — required fields ─────────────────────────────────────────

    @Test
    void testRegisterClient_NullUsername_Returns400() {
        UserRegistrationRequest req = buildRequest(null, "pass123", "Test Name", "test@mail.com");

        StepVerifier.create(controller.registerClient(req))
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void testRegisterClient_BlankUsername_Returns400() {
        UserRegistrationRequest req = buildRequest("   ", "pass123", "Test Name", "test@mail.com");

        StepVerifier.create(controller.registerClient(req))
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void testRegisterClient_NullPassword_Returns400() {
        UserRegistrationRequest req = buildRequest("client1", null, "Test Name", "test@mail.com");

        StepVerifier.create(controller.registerClient(req))
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void testRegisterClient_BlankPassword_Returns400() {
        UserRegistrationRequest req = buildRequest("client1", "", "Test Name", "test@mail.com");

        StepVerifier.create(controller.registerClient(req))
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void testRegisterClient_NullName_Returns400() {
        UserRegistrationRequest req = buildRequest("client1", "pass123", null, "test@mail.com");

        StepVerifier.create(controller.registerClient(req))
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void testRegisterClient_NullEmail_Returns400() {
        UserRegistrationRequest req = buildRequest("client1", "pass123", "Test Name", null);

        StepVerifier.create(controller.registerClient(req))
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void testRegisterClient_PasswordTooShort_Returns400() {
        // password must be at least 6 characters
        UserRegistrationRequest req = buildRequest("client1", "abc", "Test Name", "test@mail.com");

        StepVerifier.create(controller.registerClient(req))
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    // ─── duplicate username ───────────────────────────────────────────────────

    @Test
    void testRegisterClient_DuplicateUsername_Returns409() {
        UserRegistrationRequest req = buildRequest("existing_client", "pass123", "Test", "t@t.com");
        Users existingUser = buildSavedUser("existing_client");

        when(usersRepository.findByUsername("existing_client")).thenReturn(Mono.just(existingUser));

        StepVerifier.create(controller.registerClient(req))
                .expectErrorMatches(e ->
                        e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.CONFLICT)
                .verify();
    }

    // ─── success ─────────────────────────────────────────────────────────────

    @Test
    void testRegisterClient_Success_RoleIsAlwaysClient() {
        UserRegistrationRequest req = buildRequest("new_client", "password123", "Arjun Sharma", "arjun@mail.com");
        // caller tries to set a custom role — it should be ignored
        req.setRoles("RM");

        Users savedUser = buildSavedUser("new_client");

        // first call: no duplicate; subsequent call (via wealthproClient chain): re-fetch
        when(usersRepository.findByUsername("new_client"))
                .thenReturn(Mono.empty())
                .thenReturn(Mono.just(savedUser));
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(usersRepository.save(any(Users.class))).thenReturn(Mono.just(savedUser));
        // WealthproClient creates the stub client, then we return saved username
        when(wealthproClient.provisionStubClient(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        Mono<UserResponse> result = controller.registerClient(req);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("new_client", response.getUsername());
                    // role MUST be CLIENT regardless of what was requested
                    assertEquals("CLIENT", response.getRoles());
                })
                .verifyComplete();
    }

    @Test
    void testRegisterClient_Success_WealthproClientIsCalledOnce() {
        UserRegistrationRequest req = buildRequest("new_client2", "pass1234", "Priya", "priya@mail.com");
        Users savedUser = buildSavedUser("new_client2");

        when(usersRepository.findByUsername("new_client2"))
                .thenReturn(Mono.empty())
                .thenReturn(Mono.just(savedUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usersRepository.save(any(Users.class))).thenReturn(Mono.just(savedUser));
        when(wealthproClient.provisionStubClient(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        controller.registerClient(req).block();

        // make sure we called provisionStubClient exactly once
        verify(wealthproClient, times(1))
                .provisionStubClient(anyString(), anyString(), anyString(), anyString());
    }
}
