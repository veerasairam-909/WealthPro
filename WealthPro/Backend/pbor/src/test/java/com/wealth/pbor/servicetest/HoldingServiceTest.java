package com.wealth.pbor.servicetest;

import com.wealth.pbor.dto.request.HoldingRequest;
import com.wealth.pbor.dto.response.HoldingResponse;
import com.wealth.pbor.entity.Account;
import com.wealth.pbor.entity.Holding;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.feign.ProductCatalogFeignClient;
import com.wealth.pbor.feign.dto.SecurityDTO;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.repository.HoldingRepository;
import com.wealth.pbor.service.impl.HoldingServiceImpl;
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
class HoldingServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ModelMapper mapper;

    @Mock
    private ProductCatalogFeignClient productCatalogFeignClient;

    @InjectMocks
    private HoldingServiceImpl holdingService;

    private Account account;
    private Holding holding;
    private HoldingRequest request;
    private HoldingResponse response;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAccountId(1L);
        account.setClientId(1L);
        account.setAccountType(AccountType.INDIVIDUAL);
        account.setBaseCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);

        holding = new Holding();
        holding.setHoldingId(1L);
        holding.setAccount(account);
        holding.setSecurityId(101L);
        holding.setQuantity(new BigDecimal("100.0000"));
        holding.setAvgCost(new BigDecimal("250.0000"));
        holding.setValuationCurrency("INR");
        holding.setLastValuationDate(LocalDate.now());

        request = new HoldingRequest();
        request.setAccountId(1L);
        request.setSecurityId(101L);
        request.setQuantity(new BigDecimal("100.0000"));
        request.setAvgCost(new BigDecimal("250.0000"));
        request.setValuationCurrency("INR");
        request.setLastValuationDate(LocalDate.now());

        response = new HoldingResponse();
        response.setHoldingId(1L);
        response.setAccountId(1L);
        response.setSecurityId(101L);
    }

    @Test
    void testCreateHolding_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(productCatalogFeignClient.getSecurityById(101L)).thenReturn(new SecurityDTO());
        when(holdingRepository.findByAccountAccountIdAndSecurityId(1L, 101L)).thenReturn(Optional.empty());
        when(holdingRepository.save(any(Holding.class))).thenReturn(holding);
        when(mapper.map(holding, HoldingResponse.class)).thenReturn(response);

        HoldingResponse result = holdingService.createHolding(request);

        assertThat(result).isNotNull();
        assertThat(result.getSecurityId()).isEqualTo(101L);
        verify(holdingRepository, times(1)).save(any(Holding.class));
    }

    @Test
    void testCreateHolding_AccountNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> holdingService.createHolding(request));
        verify(holdingRepository, never()).save(any());
    }

    @Test
    void testCreateHolding_ExistingHoldingMerges() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(productCatalogFeignClient.getSecurityById(101L)).thenReturn(new SecurityDTO());
        when(holdingRepository.findByAccountAccountIdAndSecurityId(1L, 101L)).thenReturn(Optional.of(holding));
        when(holdingRepository.save(any(Holding.class))).thenReturn(holding);
        when(mapper.map(holding, HoldingResponse.class)).thenReturn(response);

        HoldingResponse result = holdingService.createHolding(request);

        assertThat(result).isNotNull();
        verify(holdingRepository, times(1)).save(any(Holding.class));
    }

    @Test
    void testGetHoldingById_Success() {
        when(holdingRepository.findById(1L)).thenReturn(Optional.of(holding));
        when(mapper.map(holding, HoldingResponse.class)).thenReturn(response);

        HoldingResponse result = holdingService.getHoldingById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getHoldingId()).isEqualTo(1L);
    }

    @Test
    void testGetHoldingById_NotFound() {
        when(holdingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> holdingService.getHoldingById(99L));
    }

    @Test
    void testGetHoldingsByAccountId_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(holdingRepository.findByAccountAccountId(1L)).thenReturn(List.of(holding));
        when(mapper.map(holding, HoldingResponse.class)).thenReturn(response);

        List<HoldingResponse> result = holdingService.getHoldingsByAccountId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void testGetHoldingsByAccountId_AccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> holdingService.getHoldingsByAccountId(99L));
    }

    @Test
    void testGetHoldingsBySecurityId() {
        when(holdingRepository.findBySecurityId(101L)).thenReturn(List.of(holding));
        when(mapper.map(holding, HoldingResponse.class)).thenReturn(response);

        List<HoldingResponse> result = holdingService.getHoldingsBySecurityId(101L);

        assertThat(result).hasSize(1);
    }

    @Test
    void testDeleteHolding_Success() {
        when(holdingRepository.findById(1L)).thenReturn(Optional.of(holding));
        doNothing().when(holdingRepository).delete(holding);

        holdingService.deleteHolding(1L);

        verify(holdingRepository, times(1)).delete(holding);
    }

    @Test
    void testDeleteHolding_NotFound() {
        when(holdingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> holdingService.deleteHolding(99L));
    }
}
