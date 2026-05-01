package com.wealth.pbor.service;

import com.wealth.pbor.dto.request.AccountRequest;
import com.wealth.pbor.dto.response.AccountResponse;
import com.wealth.pbor.enums.AccountStatus;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(AccountRequest request);

    AccountResponse getAccountById(Long accountId);

    List<AccountResponse> getAllAccounts();

    List<AccountResponse> getAccountsByClientId(Long clientId);

    List<AccountResponse> getAccountsByStatus(AccountStatus status);

    List<AccountResponse> getAccountsByClientIdAndStatus(Long clientId, AccountStatus status);

    AccountResponse updateAccount(Long accountId, AccountRequest request);

    AccountResponse updateAccountStatus(Long accountId, AccountStatus status);

    void deleteAccount(Long accountId);
}