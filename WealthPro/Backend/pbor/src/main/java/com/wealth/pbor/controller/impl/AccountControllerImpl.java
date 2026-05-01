package com.wealth.pbor.controller.impl;

import com.wealth.pbor.controller.AccountController;
import com.wealth.pbor.dto.request.AccountRequest;
import com.wealth.pbor.dto.response.AccountResponse;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.security.AuthContext;
import com.wealth.pbor.security.OwnershipGuard;
import com.wealth.pbor.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountControllerImpl implements AccountController {

    private final AccountService accountService;
    private final OwnershipGuard ownershipGuard;

    @Override
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        // Only staff can create accounts (gateway restricts CLIENT from POST here).
        AccountResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long accountId,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        ownershipGuard.checkAccount(new AuthContext(null, roles, authClientId), accountId);
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles) {
        AuthContext ctx = new AuthContext(null, roles, null);
        // CLIENT cannot list all accounts — staff only.
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot list all accounts. Use /client/{yourClientId}.");
        }
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @Override
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByClientId(
            @PathVariable Long clientId,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        ownershipGuard.checkClient(new AuthContext(null, roles, authClientId), clientId);
        return ResponseEntity.ok(accountService.getAccountsByClientId(clientId));
    }

    @Override
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AccountResponse>> getAccountsByStatus(@PathVariable AccountStatus status) {
        // Staff-only analytics endpoint (gateway already excludes CLIENT from hasRole checks
        // for this precise path if you want). For CLIENT, filter results by their clientId.
        return ResponseEntity.ok(accountService.getAccountsByStatus(status));
    }

    @Override
    @GetMapping("/client/{clientId}/status/{status}")
    public ResponseEntity<List<AccountResponse>> getAccountsByClientIdAndStatus(
            @PathVariable Long clientId,
            @PathVariable AccountStatus status,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        ownershipGuard.checkClient(new AuthContext(null, roles, authClientId), clientId);
        return ResponseEntity.ok(accountService.getAccountsByClientIdAndStatus(clientId, status));
    }

    @Override
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable Long accountId,
                                                         @Valid @RequestBody AccountRequest request) {
        // Staff-only at gateway — no extra check required here.
        return ResponseEntity.ok(accountService.updateAccount(accountId, request));
    }

    @Override
    @PatchMapping("/{accountId}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(@PathVariable Long accountId,
                                                               @RequestParam AccountStatus status) {
        return ResponseEntity.ok(accountService.updateAccountStatus(accountId, status));
    }

    @Override
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Internal endpoint used by Analytics / Review services to resolve
     * accountId → clientId for ownership checks.
     * Returns a bare clientId number.
     */
    @Override
    @GetMapping("/{accountId}/owner")
    public ResponseEntity<Long> getAccountOwner(@PathVariable Long accountId) {
        return ResponseEntity.ok(ownershipGuard.resolveOwner(accountId));
    }
}
