package com.wealth.pbor.controller;

import com.wealth.pbor.dto.request.AccountRequest;
import com.wealth.pbor.dto.response.AccountResponse;
import com.wealth.pbor.enums.AccountStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/accounts")
public interface AccountController {

    ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request);
    ResponseEntity<List<AccountResponse>> getAllAccounts();
    ResponseEntity<AccountResponse> getAccountById(@PathVariable Long accountId);
    ResponseEntity<List<AccountResponse>> getAccountsByClientId(@PathVariable Long clientId);
    ResponseEntity<List<AccountResponse>> getAccountsByStatus(@PathVariable AccountStatus status);
    ResponseEntity<List<AccountResponse>> getAccountsByClientIdAndStatus(@PathVariable Long clientId, @PathVariable AccountStatus status);
    ResponseEntity<AccountResponse> updateAccount(@PathVariable Long accountId, @Valid @RequestBody AccountRequest request);
    ResponseEntity<AccountResponse> updateAccountStatus(@PathVariable Long accountId, @RequestParam AccountStatus status);
    ResponseEntity<Void> deleteAccount(@PathVariable Long accountId);
}