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
    ResponseEntity<List<AccountResponse>> getAllAccounts(String roles);
    ResponseEntity<AccountResponse> getAccountById(Long accountId, String roles, Long authClientId);
    ResponseEntity<List<AccountResponse>> getAccountsByClientId(Long clientId, String roles, Long authClientId);
    ResponseEntity<List<AccountResponse>> getAccountsByStatus(AccountStatus status);
    ResponseEntity<List<AccountResponse>> getAccountsByClientIdAndStatus(Long clientId, AccountStatus status, String roles, Long authClientId);
    ResponseEntity<AccountResponse> updateAccount(Long accountId, AccountRequest request);
    ResponseEntity<AccountResponse> updateAccountStatus(Long accountId, AccountStatus status);
    ResponseEntity<Void> deleteAccount(Long accountId);
    ResponseEntity<Long> getAccountOwner(Long accountId);
}
