package com.wealth.pbor.servicetest;

import com.wealth.pbor.dto.request.CorporateActionRequest;
import com.wealth.pbor.dto.response.CorporateActionResponse;
import com.wealth.pbor.entity.Account;
import com.wealth.pbor.entity.CashLedger;
import com.wealth.pbor.entity.CorporateAction;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import com.wealth.pbor.enums.CAType;
import com.wealth.pbor.exception.BadRequestException;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.feign.NotificationFeignClient;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.repository.CashLedgerRepository;
import com.wealth.pbor.repository.CorporateActionRepository;
import com.wealth.pbor.repository.HoldingRepository;
import com.wealth.pbor.service.impl.CorporateActionServiceImpl;
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
class CorporateActionServiceTest {

    @Mock
    private CorporateActionRepository corporateActionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private CashLedgerRepository cashLedgerRepository;

    @Mock
    private ModelMapper mapper;

    @Mock
    private NotificationFeignClient notificationFeignClient;

    @InjectMocks
    private CorporateActionServiceImpl corporateActionService;

    private Account account;
    private CorporateAction corporateAction;
    private CorporateActionRequest request;
    private CorporateActionResponse response;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAccountId(1L);
        account.setClientId(1L);
        account.setAccountType(AccountType.INDIVIDUAL);
        account.setBaseCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);

        corporateAction = new CorporateAction();
        corporateAction.setCaId(1L);
        corporateAction.setSecurityId(101L);
        corporateAction.setCaType(CAType.DIVIDEND);
        corporateAction.setRecordDate(LocalDate.of(2024, 6, 15));
        corporateAction.setExDate(LocalDate.of(2024, 6, 14));
        corporateAction.setPayDate(LocalDate.of(2024, 6, 20));
        corporateAction.setTermsJson("{\"amount\":5.0}");

        request = new CorporateActionRequest();
        request.setAccountId(1L);
        request.setSecurityId(101L);
        request.setCaType(CAType.DIVIDEND);
        request.setRecordDate(LocalDate.of(2024, 6, 15));
        request.setExDate(LocalDate.of(2024, 6, 14));
        request.setPayDate(LocalDate.of(2024, 6, 20));
        request.setTermsJson("{\"amount\":5.0}");
        request.setAmount(new BigDecimal("5.0"));
        request.setCurrency("INR");

        response = new CorporateActionResponse();
        response.setCaId(1L);
        response.setSecurityId(101L);
        response.setCaType(CAType.DIVIDEND);
    }

    @Test
    void testCreateCorporateAction_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(corporateActionRepository.save(any(CorporateAction.class))).thenReturn(corporateAction);
        when(cashLedgerRepository.save(any(CashLedger.class))).thenReturn(new CashLedger());
        when(mapper.map(corporateAction, CorporateActionResponse.class)).thenReturn(response);

        CorporateActionResponse result = corporateActionService.createCorporateAction(request);

        assertThat(result).isNotNull();
        assertThat(result.getCaType()).isEqualTo(CAType.DIVIDEND);
        verify(corporateActionRepository, times(1)).save(any(CorporateAction.class));
    }

    @Test
    void testCreateCorporateAction_ExDateAfterRecordDate() {
        request.setExDate(LocalDate.of(2024, 6, 16));

        assertThrows(BadRequestException.class, () -> corporateActionService.createCorporateAction(request));
        verify(corporateActionRepository, never()).save(any());
    }

    @Test
    void testCreateCorporateAction_PayDateNotAfterRecordDate() {
        request.setPayDate(LocalDate.of(2024, 6, 15));

        assertThrows(BadRequestException.class, () -> corporateActionService.createCorporateAction(request));
        verify(corporateActionRepository, never()).save(any());
    }

    @Test
    void testGetCorporateActionById_Success() {
        when(corporateActionRepository.findById(1L)).thenReturn(Optional.of(corporateAction));
        when(mapper.map(corporateAction, CorporateActionResponse.class)).thenReturn(response);

        CorporateActionResponse result = corporateActionService.getCorporateActionById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getCaId()).isEqualTo(1L);
    }

    @Test
    void testGetCorporateActionById_NotFound() {
        when(corporateActionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> corporateActionService.getCorporateActionById(99L));
    }

    @Test
    void testGetCorporateActionsBySecurityId() {
        when(corporateActionRepository.findBySecurityId(101L)).thenReturn(List.of(corporateAction));
        when(mapper.map(corporateAction, CorporateActionResponse.class)).thenReturn(response);

        List<CorporateActionResponse> result = corporateActionService.getCorporateActionsBySecurityId(101L);

        assertThat(result).hasSize(1);
    }

    @Test
    void testGetCorporateActionsByRecordDateRange_InvalidRange() {
        assertThrows(BadRequestException.class, () ->
                corporateActionService.getCorporateActionsByRecordDateRange(
                        LocalDate.of(2024, 6, 30), LocalDate.of(2024, 6, 1)));
    }

    @Test
    void testDeleteCorporateAction_Success() {
        when(corporateActionRepository.findById(1L)).thenReturn(Optional.of(corporateAction));
        doNothing().when(corporateActionRepository).delete(corporateAction);

        corporateActionService.deleteCorporateAction(1L);

        verify(corporateActionRepository, times(1)).delete(corporateAction);
    }

    @Test
    void testDeleteCorporateAction_NotFound() {
        when(corporateActionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> corporateActionService.deleteCorporateAction(99L));
    }
}
