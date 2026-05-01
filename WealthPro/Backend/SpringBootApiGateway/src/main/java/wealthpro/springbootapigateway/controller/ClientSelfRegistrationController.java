package wealthpro.springbootapigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import wealthpro.springbootapigateway.client.WealthproClient;
import wealthpro.springbootapigateway.dto.UserRegistrationRequest;
import wealthpro.springbootapigateway.dto.UserResponse;
import wealthpro.springbootapigateway.entities.Users;
import wealthpro.springbootapigateway.enums.Role;
import wealthpro.springbootapigateway.repository.UsersRepository;

import reactor.core.publisher.Mono;

/**
 * Public self-registration endpoint — CLIENT role ONLY.
 *
 * Unlike {@link UserController} (which is ADMIN-only and can create any non-admin
 * role), this endpoint is publicly accessible and FORCES the role to CLIENT
 * regardless of what the caller sends. Staff roles (RM, DEALER,
 * COMPLIANCE) can only be created by an ADMIN via /auth/users/register.
 *
 * Security:
 *   - Exposed as permitAll() in SecurityConfig at path /auth/register/client
 *   - Role is hardcoded to Role.CLIENT — any 'roles' field in the payload is ignored
 *   - Password is BCrypt-hashed automatically via PasswordEncoder
 */
@RestController
@RequestMapping("/auth/register")
public class ClientSelfRegistrationController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WealthproClient wealthproClient;

    @PostMapping("/client")
    public Mono<UserResponse> registerClient(@RequestBody UserRegistrationRequest request) {

        // ── Required field validation ────────────────────────────────────────
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required."));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required."));
        }
        if (request.getName() == null || request.getName().isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required."));
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required."));
        }
        if (request.getPassword().length() < 6) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 6 characters long."));
        }

        // ── Duplicate username check ─────────────────────────────────────────
        return usersRepository.findByUsername(request.getUsername())
                .flatMap(existing -> Mono.<UserResponse>error(
                        new ResponseStatusException(HttpStatus.CONFLICT,
                                "Username '" + request.getUsername() + "' is already taken.")))
                .switchIfEmpty(Mono.defer(() -> {
                    Users user = new Users();
                    user.setUsername(request.getUsername());
                    user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt auto
                    user.setName(request.getName());
                    user.setEmail(request.getEmail());
                    user.setPhone(request.getPhone());
                    user.setRoles(Role.CLIENT.name());   // ── HARDCODED — self-registration is CLIENT only ──
                    user.setNew(true);
                    return usersRepository.save(user)
                            // After the user row is saved, ask Wealthpro to create a
                            // stub Client record (status=PENDING_KYC) linked to this
                            // username. If the call fails we still return success —
                            // an RM can link them manually later.
                            .flatMap(saved -> wealthproClient
                                    .provisionStubClient(
                                            saved.getUsername(),
                                            saved.getName(),
                                            saved.getEmail(),
                                            saved.getPhone())
                                    .thenReturn(saved.getUsername()))
                            // Re-fetch so R2DBC reads back the DB-generated userId
                            .flatMap(usersRepository::findByUsername)
                            .map(this::toResponse);
                }));
    }

    private UserResponse toResponse(Users user) {
        return new UserResponse(
                user.getUserId(),   // Numeric surrogate key — DB-generated, read back after re-fetch
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRoles()
        );
    }
}
