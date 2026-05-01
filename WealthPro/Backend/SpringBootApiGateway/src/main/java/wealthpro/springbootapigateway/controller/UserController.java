package wealthpro.springbootapigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import wealthpro.springbootapigateway.dto.UserRegistrationRequest;
import wealthpro.springbootapigateway.dto.UserResponse;
import wealthpro.springbootapigateway.entities.Users;
import wealthpro.springbootapigateway.enums.Role;
import wealthpro.springbootapigateway.repository.UsersRepository;

import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/auth/users")
public class UserController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── Shared role parsing & validation ──────────────────────────────────────

    /**
     * Parses the raw role string from the request into a Role enum.
     * Accepts any case (e.g. "rm", "RM", "Rm") — normalised to uppercase.
     * Returns null if the string is null/blank or not a valid Role name.
     */
    private Role parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) return null;
        try {
            return Role.valueOf(rawRole.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;  // unknown role name
        }
    }

    /**
     * Validates that the parsed role is allowed to be assigned via API.
     * ADMIN is reserved — it can never be created or assigned through the API.
     * Returns a 403/400 error Mono if blocked, or null (no error) if allowed.
     */
    private <T> Mono<T> guardRole(Role role, String rawInput) {
        if (role == null) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role '" + rawInput + "'. " +
                    "Allowed values: CLIENT, RM, DEALER, COMPLIANCE."));
        }
        if (role == Role.ADMIN) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Role 'ADMIN' is reserved and cannot be assigned via API. " +
                    "Allowed values: CLIENT, RM, DEALER, COMPLIANCE."));
        }
        return null; // no error — role is valid and allowed
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * Admin-only: create a new user (RM, DEALER, COMPLIANCE, CLIENT).
     * ADMIN role is blocked — the single Admin is pre-seeded directly in DB.
     */
    @PostMapping("/register")
    public Mono<UserResponse> registerUser(@RequestBody UserRegistrationRequest request) {

        // Validate required fields first
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required."));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required."));
        }

        // Parse & validate role — defaults to CLIENT if not provided
        String rawRole = (request.getRoles() != null) ? request.getRoles() : "CLIENT";
        Role assignedRole = parseRole(rawRole);
        Mono<UserResponse> roleError = guardRole(assignedRole, rawRole);
        if (roleError != null) return roleError;

        return usersRepository.findByUsername(request.getUsername())
                .flatMap(existing -> Mono.<UserResponse>error(
                        new ResponseStatusException(HttpStatus.CONFLICT,
                                "Username '" + request.getUsername() + "' already exists.")))
                .switchIfEmpty(Mono.defer(() -> {
                    Users user = new Users();
                    user.setUsername(request.getUsername());
                    user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt auto
                    user.setName(request.getName());
                    user.setEmail(request.getEmail());
                    user.setPhone(request.getPhone());
                    user.setRoles(assignedRole.name());  // Store enum name as String in DB
                    user.setNew(true);
                    // Re-fetch after insert so R2DBC reads back the DB-generated userId
                    return usersRepository.save(user)
                            .flatMap(saved -> usersRepository.findByUsername(saved.getUsername()))
                            .map(this::toResponse);
                }));
    }

    /**
     * Returns the full user list as a single buffered JSON array.
     * Using Mono<List<T>> instead of Flux<T> ensures the entire response is
     * collected in memory before writing — prevents ERR_INCOMPLETE_CHUNKED_ENCODING
     * that can occur when Flux streams elements one-by-one and a mid-stream error
     * (e.g. R2DBC row-mapping failure) truncates the HTTP response body.
     */
    @GetMapping
    public Mono<List<UserResponse>> getAllUsers() {
        return usersRepository.findAll()
                .map(this::toResponse)
                .collectList();
    }

    @GetMapping("/{username}")
    public Mono<UserResponse> getUserByUsername(@PathVariable String username) {
        return usersRepository.findByUsername(username)
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "User '" + username + "' not found.")));
    }

    /**
     * Admin-only: update a user's details or role.
     * ADMIN role cannot be assigned via update.
     * The built-in "admin" account is fully immutable — cannot be changed via API.
     */
    @PutMapping("/{username}")
    public Mono<UserResponse> updateUser(@PathVariable String username,
                                         @RequestBody UserRegistrationRequest request) {

        // Immutability guard — admin account is locked from API changes
        if ("admin".equalsIgnoreCase(username)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The admin account cannot be modified via API. " +
                    "Update credentials directly in the database."));
        }

        // Parse & validate role only if provided in the request
        Role updatedRole = null;
        if (request.getRoles() != null && !request.getRoles().isBlank()) {
            updatedRole = parseRole(request.getRoles());
            Mono<UserResponse> roleError = guardRole(updatedRole, request.getRoles());
            if (roleError != null) return roleError;
        }

        final Role finalRole = updatedRole;

        return usersRepository.findByUsername(username)
                .flatMap(user -> {
                    if (request.getName() != null)  user.setName(request.getName());
                    if (request.getEmail() != null) user.setEmail(request.getEmail());
                    if (request.getPhone() != null) user.setPhone(request.getPhone());
                    if (finalRole != null)           user.setRoles(finalRole.name()); // Store enum name as String
                    if (request.getPassword() != null && !request.getPassword().isBlank()) {
                        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt auto
                    }
                    user.setNew(false);
                    return usersRepository.save(user)
                            .flatMap(saved -> usersRepository.findByUsername(saved.getUsername()))
                            .map(this::toResponse);
                })
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "User '" + username + "' not found.")));
    }

    /**
     * Admin-only: delete a user.
     * The built-in "admin" account cannot be deleted.
     */
    @DeleteMapping("/{username}")
    public Mono<Void> deleteUser(@PathVariable String username) {

        if ("admin".equalsIgnoreCase(username)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "The admin account cannot be deleted."));
        }

        // Find first; if missing → 404. Otherwise delete and complete (Mono<Void>).
        // Without the explicit .then() at the end, the empty Mono from delete()
        // collides with switchIfEmpty and incorrectly raises 404 on success.
        return usersRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "User '" + username + "' not found.")))
                .flatMap(usersRepository::delete)
                .then();
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private UserResponse toResponse(Users user) {
        return new UserResponse(
                user.getUserId(),    // Numeric surrogate key — may be null for very old rows
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRoles()      // String — passed through as-is ("RM", "CLIENT", "ADVISOR", etc.)
        );
    }
}
