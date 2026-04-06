package com.wealth.pbor.servicetest;

import com.wealth.pbor.dto.request.AccountRequest;
import com.wealth.pbor.dto.response.AccountResponse;
import com.wealth.pbor.entity.Account;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import com.wealth.pbor.exception.BadRequestException;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;
    private AccountRequest request;
    private AccountResponse response;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAccountId(1L);
        account.setClientId(1L);
        account.setAccountType(AccountType.INDIVIDUAL);
        account.setBaseCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);

        request = new AccountRequest();
        request.setClientId(1L);
        request.setAccountType(AccountType.INDIVIDUAL);
        request.setBaseCurrency("INR");
        request.setStatus(AccountStatus.ACTIVE);

        response = new AccountResponse();
        response.setAccountId(1L);
        response.setClientId(1L);
        response.setAccountType(AccountType.INDIVIDUAL);
        response.setBaseCurrency("INR");
        response.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void testCreateAccount_Success() {
        when(accountRepository.existsByClientIdAndAccountType(1L, AccountType.INDIVIDUAL)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(mapper.map(account, AccountResponse.class)).thenReturn(response);

        AccountResponse result = accountService.createAccount(request);

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo(1L);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccount_DuplicateThrowsBadRequest() {
        when(accountRepository.existsByClientIdAndAccountType(1L, AccountType.INDIVIDUAL)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> accountService.createAccount(request));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testGetAccountById_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(mapper.map(account, AccountResponse.class)).thenReturn(response);

        AccountResponse result = accountService.getAccountById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAccountId()).isEqualTo(1L);
    }

    @Test
    void testGetAccountById_NotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(99L));
    }

    @Test
    void testGetAllAccounts() {
        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(mapper.map(account, AccountResponse.class)).thenReturn(response);

        List<AccountResponse> result = accountService.getAllAccounts();

        assertThat(result).hasSize(1);
    }

    @Test
    void testGetAccountsByClientId() {
        when(accountRepository.findByClientId(1L)).thenReturn(List.of(account));
        when(mapper.map(account, AccountResponse.class)).thenReturn(response);

        List<AccountResponse> result = accountService.getAccountsByClientId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void testGetAccountsByStatus() {
        when(accountRepository.findByStatus(AccountStatus.ACTIVE)).thenReturn(List.of(account));
        when(mapper.map(account, AccountResponse.class)).thenReturn(response);

        List<AccountResponse> result = accountService.getAccountsByStatus(AccountStatus.ACTIVE);

        assertThat(result).hasSize(1);
    }

    @Test
    void testUpdateAccount_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(mapper.map(account, AccountResponse.class)).thenReturn(response);

        AccountResponse result = accountService.updateAccount(1L, request);

        assertThat(result).isNotNull();
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testUpdateAccount_NotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.updateAccount(99L, request));
    }

    @Test
    void testDeleteAccount_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        doNothing().when(accountRepository).delete(account);

        accountService.deleteAccount(1L);

        verify(accountRepository, times(1)).delete(account);
    }

    @Test
    void testDeleteAccount_NotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.deleteAccount(99L));
    }
}