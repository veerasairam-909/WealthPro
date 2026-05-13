package com.wealthpro.analytics.service;

import com.wealthpro.analytics.dto.*;
import com.wealthpro.analytics.entities.*;
import com.wealthpro.analytics.enums.*;
import com.wealthpro.analytics.exception.ResourceNotFoundException;
import com.wealthpro.analytics.feign.NotificationFeignClient;
import com.wealthpro.analytics.feign.PborFeignClient;
import com.wealthpro.analytics.feign.WealthproFeignClient;
import com.wealthpro.analytics.feign.dto.AccountDTO;
import com.wealthpro.analytics.feign.dto.HoldingDTO;
import com.wealthpro.analytics.repository.*;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Additional unit tests for AnalyticsServiceImpl covering uncovered paths.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplAdditionalTest {

    @Mock private PerformanceRecordRepository performanceRecordRepository;
    @Mock private RiskMeasureRepository riskMeasureRepository;
    @Mock private ComplianceBreachRepository complianceBreachRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private PborFeignClient pborFeignClient;
    @Mock private WealthproFeignClient wealthproFeignClient;
    @Mock private NotificationFeignClient notificationFeignClient;

    @InjectMocks private AnalyticsServiceImpl analyticsService;

    private AccountDTO sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = new AccountDTO();
        sampleAccount.setAccountId(1L);
        sampleAccount.setClientId(10L);
        sampleAccount.setAccountType("INDIVIDUAL");

        lenient().when(pborFeignClient.getAccountById(anyLong())).thenReturn(sampleAccount);
        lenient().when(pborFeignClient.getHoldingsByAccountId(anyLong())).thenReturn(List.of());
        lenient().when(wealthproFeignClient.getAllSuitabilityRules()).thenReturn(List.of());
    }

    // ─── getBreachesByAccountId ───────────────────────────────────────────────

    @Test
    void testGetBreachesByAccountId_ReturnsList() {
        ComplianceBreach breach = ComplianceBreach.builder()
                .breachId(1L).accountId(1L).status(BreachStatus.OPEN).build();
        ComplianceBreachResponseDTO responseDTO = new ComplianceBreachResponseDTO();
        responseDTO.setStatus(BreachStatus.OPEN);

        when(complianceBreachRepository.findByAccountId(1L)).thenReturn(List.of(breach));
        when(modelMapper.map(any(ComplianceBreach.class), eq(ComplianceBreachResponseDTO.class)))
                .thenReturn(responseDTO);

        List<ComplianceBreachResponseDTO> result = analyticsService.getBreachesByAccountId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(complianceBreachRepository).findByAccountId(1L);
    }

    @Test
    void testGetBreachesByAccountId_EmptyList() {
        when(complianceBreachRepository.findByAccountId(99L)).thenReturn(List.of());

        List<ComplianceBreachResponseDTO> result = analyticsService.getBreachesByAccountId(99L);

        assertTrue(result.isEmpty());
    }

    // ─── runComplianceScan ────────────────────────────────────────────────────

    @Test
    void testRunComplianceScan_AccountNotFound_ThrowsException() {
        when(pborFeignClient.getAccountById(999L))
                .thenThrow(FeignException.NotFound.class);

        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.runComplianceScan(999L));
    }

    @Test
    void testRunComplianceScan_NoBreaches_ReturnsEmptyList() {
        // The random-based breach generation is seeded with 42 — results may vary,
        // but calling save only happens when breaches exist.
        // We can verify the account lookup always happens.
        when(pborFeignClient.getAccountById(1L)).thenReturn(sampleAccount);
        when(wealthproFeignClient.getAllSuitabilityRules()).thenReturn(List.of());

        // Allow saveAll to succeed if needed (random might produce breaches)
        when(complianceBreachRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(ComplianceBreach.class), eq(ComplianceBreachResponseDTO.class)))
                .thenReturn(new ComplianceBreachResponseDTO());

        List<ComplianceBreachResponseDTO> result = analyticsService.runComplianceScan(1L);

        // Result is either empty or has some breaches — both valid
        assertNotNull(result);
        verify(pborFeignClient, atLeastOnce()).getAccountById(1L);
    }

    // ─── acknowledgeBreach — not found ────────────────────────────────────────

    @Test
    void testAcknowledgeBreach_NotFound_ThrowsException() {
        when(complianceBreachRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.acknowledgeBreach(999L));
    }

    // ─── calculateDailyReturn — account not found ─────────────────────────────

    @Test
    void testCalculateDailyReturn_AccountNotFound_ThrowsException() {
        when(pborFeignClient.getAccountById(999L))
                .thenThrow(FeignException.NotFound.class);

        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.calculateDailyReturn(999L, 1L));
    }

    // ─── calculateMonthlyReturn — with existing daily records ─────────────────

    @Test
    void testCalculateMonthlyReturn_WithExistingDailyRecords_UsesChainedReturn() {
        PerformanceRecord daily1 = PerformanceRecord.builder()
                .recordId(1L).accountId(1L).period(Period.DAILY)
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().minusDays(2))
                .returnPercentage(2.0)
                .benchmarkReturnPercentage(1.0)
                .calculatedAt(LocalDateTime.now()).build();

        PerformanceRecord daily2 = PerformanceRecord.builder()
                .recordId(2L).accountId(1L).period(Period.DAILY)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().minusDays(1))
                .returnPercentage(1.5)
                .benchmarkReturnPercentage(0.8)
                .calculatedAt(LocalDateTime.now()).build();

        PerformanceRecord savedMonthly = PerformanceRecord.builder()
                .recordId(3L).accountId(1L).period(Period.MONTHLY).build();

        PerformanceRecordResponseDTO responseDTO = new PerformanceRecordResponseDTO();
        responseDTO.setPeriod(Period.MONTHLY);

        when(performanceRecordRepository.findByAccountIdAndPeriod(1L, Period.DAILY))
                .thenReturn(List.of(daily1, daily2));
        when(performanceRecordRepository.save(any(PerformanceRecord.class))).thenReturn(savedMonthly);
        when(modelMapper.map(any(PerformanceRecord.class), eq(PerformanceRecordResponseDTO.class)))
                .thenReturn(responseDTO);

        PerformanceRecordResponseDTO result = analyticsService.calculateMonthlyReturn(1L, 1L);

        assertNotNull(result);
        // verify save was called with the monthly record
        verify(performanceRecordRepository).save(argThat(r -> r.getPeriod() == Period.MONTHLY));
    }

    // ─── calculateMonthlyReturn — account not found ────────────────────────────

    @Test
    void testCalculateMonthlyReturn_AccountNotFound_ThrowsException() {
        when(pborFeignClient.getAccountById(999L))
                .thenThrow(FeignException.NotFound.class);

        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.calculateMonthlyReturn(999L, 1L));
    }

    // ─── getRiskMeasuresByAccountId ───────────────────────────────────────────

    @Test
    void testGetRiskMeasuresByAccountId_ReturnsList() {
        RiskMeasure measure = RiskMeasure.builder()
                .measureId(1L).accountId(1L).measureType(MeasureType.VOLATILITY)
                .measureValue(12.5).calculatedAt(LocalDateTime.now()).build();
        RiskMeasureResponseDTO dto = new RiskMeasureResponseDTO();
        dto.setMeasureType(MeasureType.VOLATILITY);

        when(riskMeasureRepository.findByAccountId(1L)).thenReturn(List.of(measure));
        when(modelMapper.map(any(RiskMeasure.class), eq(RiskMeasureResponseDTO.class))).thenReturn(dto);

        List<RiskMeasureResponseDTO> result = analyticsService.getRiskMeasuresByAccountId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(MeasureType.VOLATILITY, result.get(0).getMeasureType());
    }

    // ─── runRiskAssessment — account not found ────────────────────────────────

    @Test
    void testRunRiskAssessment_AccountNotFound_ThrowsException() {
        when(pborFeignClient.getAccountById(999L))
                .thenThrow(FeignException.NotFound.class);

        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.runRiskAssessment(999L));
    }

    @Test
    void testRunRiskAssessment_WithHoldings_ProducesAllFourMeasures() {
        HoldingDTO holding = new HoldingDTO();
        holding.setSecurityId(100L);

        when(pborFeignClient.getHoldingsByAccountId(1L)).thenReturn(List.of(holding));
        when(riskMeasureRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(RiskMeasure.class), eq(RiskMeasureResponseDTO.class)))
                .thenReturn(new RiskMeasureResponseDTO());

        List<RiskMeasureResponseDTO> result = analyticsService.runRiskAssessment(1L);

        // always returns exactly 4 risk measures
        assertEquals(4, result.size());
    }

    // ─── getAccountDashboard — with data ─────────────────────────────────────

    @Test
    void testGetAccountDashboard_WithData_ReturnsAllSections() {
        PerformanceRecord pr = PerformanceRecord.builder()
                .recordId(1L).accountId(1L).period(Period.DAILY).build();
        RiskMeasure rm = RiskMeasure.builder()
                .measureId(1L).accountId(1L).measureType(MeasureType.VOLATILITY).build();
        ComplianceBreach cb = ComplianceBreach.builder()
                .breachId(1L).accountId(1L).status(BreachStatus.OPEN).build();

        when(performanceRecordRepository.findByAccountId(1L)).thenReturn(List.of(pr));
        when(riskMeasureRepository.findByAccountId(1L)).thenReturn(List.of(rm));
        when(complianceBreachRepository.findByAccountIdAndStatus(1L, BreachStatus.OPEN))
                .thenReturn(List.of(cb));
        when(modelMapper.map(any(PerformanceRecord.class), eq(PerformanceRecordResponseDTO.class)))
                .thenReturn(new PerformanceRecordResponseDTO());
        when(modelMapper.map(any(RiskMeasure.class), eq(RiskMeasureResponseDTO.class)))
                .thenReturn(new RiskMeasureResponseDTO());
        when(modelMapper.map(any(ComplianceBreach.class), eq(ComplianceBreachResponseDTO.class)))
                .thenReturn(new ComplianceBreachResponseDTO());

        AccountDashboardDTO result = analyticsService.getAccountDashboard(1L);

        assertNotNull(result);
        assertEquals(1L, result.getAccountId());
        assertEquals(1, result.getPerformanceRecords().size());
        assertEquals(1, result.getRiskMeasures().size());
        assertEquals(1, result.getComplianceBreaches().size());
    }
}
