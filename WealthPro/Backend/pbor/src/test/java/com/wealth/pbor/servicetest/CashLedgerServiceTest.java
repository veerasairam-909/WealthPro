package com.wealth.pbor.servicetest;

import com.wealth.pbor.dto.request.CashLedgerRequest;
import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.entity.Account;
import com.wealth.pbor.entity.CashLedger;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import com.wealth.pbor.enums.TxnType;
import com.wealth.pbor.exception.BadRequestException;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.repository.CashLedgerRepository;
import com.wealth.pbor.repository.HoldingRepository;
import com.wealth.pbor.service.impl.CashLedgerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashLedgerServiceTest {

    @Mock
    private CashLedgerRepository cashLedgerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private CashLedgerServiceImpl cashLedgerService;

    private Account account;
    private CashLedger cashLedger;
    private CashLedgerRequest request;
    private CashLedgerResponse response;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAccountId(1L);
        account.setClientId(1L);
        account.setAccountType(AccountType.INDIVIDUAL);
        account.setBaseCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);

        cashLedger = new CashLedger();
        cashLedger.setLedgerId(1L);
        cashLedger.setAccount(account);
        cashLedger.setTxnType(TxnType.SUBSCRIPTION);
        cashLedger.setAmount(new BigDecimal("5000.00"));
        cashLedger.setCurrency("INR");
        cashLedger.setTxnDate(LocalDate.now().minusDays(1));
        cashLedger.setNarrative("Test subscription");

        request = new CashLedgerRequest();
        request.setAccountId(1L);
        request.setTxnType(TxnType.SUBSCRIPTION);
        request.setAmount(new BigDecimal("5000.00"));
        request.setCurrency("INR");
        request.setSecurityId(101L);
        request.setQuantity(new BigDecimal("10"));
        request.setPrice(new BigDecimal("500"));
        request.setAmount(new BigDecimal("5000.00"));
        request.setTxnDate(LocalDate.now().minusDays(1));
        request.setNarrative("Test subscription");

        response = new CashLedgerResponse();
        response.setLedgerId(1L);
        response.setAccountId(1L);
        response.setTxnType(TxnType.SUBSCRIPTION);
    }

    @Test
    void testCreateCashLedgerEntry_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(holdingRepository.findByAccountAccountIdAndSecurityId(1L, 101L)).thenReturn(Optional.empty());
        when(cashLedgerRepository.save(any(CashLedger.class))).thenReturn(cashLedger);
        when(mapper.map(cashLedger, CashLedgerResponse.class)).thenReturn(response);

        CashLedgerResponse result = cashLedgerService.createCashLedgerEntry(request);

        assertThat(result).isNotNull();
        assertThat(result.getTxnType()).isEqualTo(TxnType.SUBSCRIPTION);
        verify(cashLedgerRepository, times(1)).save(any(CashLedger.class));
    }

    @Test
    void testCreateCashLedgerEntry_AccountNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cashLedgerService.createCashLedgerEntry(request));
        verify(cashLedgerRepository, never()).save(any());
    }

    @Test
    void testCreateCashLedgerEntry_FutureDateThrowsBadRequest() {
        request.setTxnDate(LocalDate.now().plusDays(1));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(BadRequestException.class, () -> cashLedgerService.createCashLedgerEntry(request));
        verify(cashLedgerRepository, never()).save(any());
    }

    @Test
    void testGetCashLedgerEntryById_Success() {
        when(cashLedgerRepository.findById(1L)).thenReturn(Optional.of(cashLedger));
        when(mapper.map(cashLedger, CashLedgerResponse.class)).thenReturn(response);

        CashLedgerResponse result = cashLedgerService.getCashLedgerEntryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getLedgerId()).isEqualTo(1L);
    }

    @Test
    void testGetCashLedgerEntryById_NotFound() {
        when(cashLedgerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cashLedgerService.getCashLedgerEntryById(99L));
    }

    @Test
    void testGetCashLedgerEntriesByAccountId_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(cashLedgerRepository.findByAccountAccountId(1L)).thenReturn(List.of(cashLedger));
        when(mapper.map(cashLedger, CashLedgerResponse.class)).thenReturn(response);

        List<CashLedgerResponse> result = cashLedgerService.getCashLedgerEntriesByAccountId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void testGetCashLedgerEntriesByAccountIdAndDateRange_InvalidRange() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(BadRequestException.class, () ->
                cashLedgerService.getCashLedgerEntriesByAccountIdAndDateRange(
                        1L, LocalDate.now(), LocalDate.now().minusDays(5)));
    }

    @Test
    void testDeleteCashLedgerEntry_Success() {
        when(cashLedgerRepository.findById(1L)).thenReturn(Optional.of(cashLedger));
        doNothing().when(cashLedgerRepository).delete(cashLedger);

        cashLedgerService.deleteCashLedgerEntry(1L);

        verify(cashLedgerRepository, times(1)).delete(cashLedger);
    }

    @Test
    void testDeleteCashLedgerEntry_NotFound() {
        when(cashLedgerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cashLedgerService.deleteCashLedgerEntry(99L));
    }
}