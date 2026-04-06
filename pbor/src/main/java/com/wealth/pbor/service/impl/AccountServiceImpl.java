package com.wealth.pbor.service.impl;

import com.wealth.pbor.dto.request.AccountRequest;
import com.wealth.pbor.dto.response.AccountResponse;
import com.wealth.pbor.entity.Account;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.exception.BadRequestException;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ModelMapper mapper;

    @Override
    public AccountResponse createAccount(AccountRequest request) {
        boolean exists = accountRepository.existsByClientIdAndAccountType(
                request.getClientId(), request.getAccountType());
        if (exists) {
            throw new BadRequestException("An account of type " + request.getAccountType()
                    + " already exists for client ID " + request.getClientId() + ".");
        }
        Account account = new Account();
        account.setClientId(request.getClientId());
        account.setAccountType(request.getAccountType());
        account.setBaseCurrency(request.getBaseCurrency());
        account.setStatus(request.getStatus());
        Account saved = accountRepository.save(account);
        return mapper.map(saved, AccountResponse.class);
    }

    @Override
    public AccountResponse getAccountById(Long accountId) {
        Optional<Account> optional = accountRepository.findById(accountId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        return mapper.map(optional.get(), AccountResponse.class);
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        List<AccountResponse> responseList = new ArrayList<>();
        for (Account account : accounts) {
            responseList.add(mapper.map(account, AccountResponse.class));
        }
        return responseList;
    }

    @Override
    public List<AccountResponse> getAccountsByClientId(Long clientId) {
        List<Account> accounts = accountRepository.findByClientId(clientId);
        List<AccountResponse> responseList = new ArrayList<>();
        for (Account account : accounts) {
            responseList.add(mapper.map(account, AccountResponse.class));
        }
        return responseList;
    }

    @Override
    public List<AccountResponse> getAccountsByStatus(AccountStatus status) {
        List<Account> accounts = accountRepository.findByStatus(status);
        List<AccountResponse> responseList = new ArrayList<>();
        for (Account account : accounts) {
            responseList.add(mapper.map(account, AccountResponse.class));
        }
        return responseList;
    }

    @Override
    public AccountResponse updateAccount(Long accountId, AccountRequest request) {
        Optional<Account> optional = accountRepository.findById(accountId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        Account account = optional.get();
        boolean typeOrClientChanged = !account.getAccountType().equals(request.getAccountType())
                || !account.getClientId().equals(request.getClientId());
        if (typeOrClientChanged) {
            boolean exists = accountRepository.existsByClientIdAndAccountType(
                    request.getClientId(), request.getAccountType());
            if (exists) {
                throw new BadRequestException("An account of type " + request.getAccountType()
                        + " already exists for client ID " + request.getClientId() + ".");
            }
        }
        account.setClientId(request.getClientId());
        account.setAccountType(request.getAccountType());
        account.setBaseCurrency(request.getBaseCurrency());
        account.setStatus(request.getStatus());
        Account updated = accountRepository.save(account);
        return mapper.map(updated, AccountResponse.class);
    }

    @Override
    public void deleteAccount(Long accountId) {
        Optional<Account> optional = accountRepository.findById(accountId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
//        boolean hasAssets = holdingRepository.existsByAccountId(accountId);
//        boolean hasCash = cashLedgerRepository.existsByAccountId(accountId);
        accountRepository.delete(optional.get());
    }
}