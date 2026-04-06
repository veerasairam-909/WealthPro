package com.wealthpro.analytics.service;

import com.wealthpro.analytics.dto.*;
import com.wealthpro.analytics.entities.*;
import com.wealthpro.analytics.enums.*;
import com.wealthpro.analytics.exception.ResourceNotFoundException;
import com.wealthpro.analytics.repository.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AnalyticsServiceImpl} covering core business logic.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock private PerformanceRecordRepository performanceRecordRepository;
    @Mock private RiskMeasureRepository riskMeasureRepository;
    @Mock private ComplianceBreachRepository complianceBreachRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private AnalyticsServiceImpl analyticsService;

    @Test
    void testCalculateDailyReturn_positive() {
        PerformanceRecord saved = PerformanceRecord.builder()
                .recordId(1L).accountId(1L).portfolioId(1L).period(Period.DAILY)
                .startDate(LocalDate.now()).endDate(LocalDate.now())
                .returnPercentage(2.5).benchmarkReturnPercentage(1.5)
                .calculatedAt(LocalDateTime.now()).build();

        PerformanceRecordResponseDTO responseDTO = new PerformanceRecordResponseDTO();
        responseDTO.setReturnPercentage(2.5);

        when(performanceRecordRepository.save(any())).thenReturn(saved);
        when(modelMapper.map(any(PerformanceRecord.class), eq(PerformanceRecordResponseDTO.class)))
                .thenReturn(responseDTO);

        PerformanceRecordResponseDTO result = analyticsService.calculateDailyReturn(1L, 1L);
        assertNotNull(result);
        verify(performanceRecordRepository).save(any());
    }

    @Test
    void testCalculateMonthlyReturn_withNoExistingDailyRecords() {
        PerformanceRecord saved = PerformanceRecord.builder()
                .recordId(1L).accountId(1L).period(Period.MONTHLY).build();
        PerformanceRecordResponseDTO responseDTO = new PerformanceRecordResponseDTO();

        when(performanceRecordRepository.findByAccountIdAndPeriod(1L, Period.DAILY)).thenReturn(List.of());
        when(performanceRecordRepository.save(any())).thenReturn(saved);
        when(modelMapper.map(any(PerformanceRecord.class), eq(PerformanceRecordResponseDTO.class)))
                .thenReturn(responseDTO);

        PerformanceRecordResponseDTO result = analyticsService.calculateMonthlyReturn(1L, 1L);
        assertNotNull(result);
    }

    @Test
    void testRunRiskAssessment_positive() {
        when(riskMeasureRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(RiskMeasure.class), eq(RiskMeasureResponseDTO.class)))
                .thenReturn(new RiskMeasureResponseDTO());

        List<RiskMeasureResponseDTO> results = analyticsService.runRiskAssessment(1L);
        assertEquals(4, results.size()); // VOLATILITY, MAX_DRAWDOWN, VAR_95, TRACKING_ERROR
    }

    @Test
    void testAcknowledgeBreach_positive() {
        ComplianceBreach breach = ComplianceBreach.builder()
                .breachId(1L).status(BreachStatus.OPEN).build();
        ComplianceBreachResponseDTO responseDTO = new ComplianceBreachResponseDTO();
        responseDTO.setStatus(BreachStatus.ACKNOWLEDGED);

        when(complianceBreachRepository.findById(1L)).thenReturn(Optional.of(breach));
        when(complianceBreachRepository.save(any())).thenReturn(breach);
        when(modelMapper.map(any(ComplianceBreach.class), eq(ComplianceBreachResponseDTO.class)))
                .thenReturn(responseDTO);

        ComplianceBreachResponseDTO result = analyticsService.acknowledgeBreach(1L);
        assertEquals(BreachStatus.ACKNOWLEDGED, result.getStatus());
    }

    @Test
    void testAcknowledgeBreach_wrongStatus() {
        ComplianceBreach breach = ComplianceBreach.builder()
                .breachId(1L).status(BreachStatus.CLOSED).build();
        when(complianceBreachRepository.findById(1L)).thenReturn(Optional.of(breach));
        assertThrows(IllegalStateException.class, () -> analyticsService.acknowledgeBreach(1L));
    }

    @Test
    void testCloseBreach_positive() {
        ComplianceBreach breach = ComplianceBreach.builder()
                .breachId(1L).status(BreachStatus.ACKNOWLEDGED).build();
        ComplianceBreachResponseDTO responseDTO = new ComplianceBreachResponseDTO();
        responseDTO.setStatus(BreachStatus.CLOSED);

        when(complianceBreachRepository.findById(1L)).thenReturn(Optional.of(breach));
        when(complianceBreachRepository.save(any())).thenReturn(breach);
        when(modelMapper.map(any(ComplianceBreach.class), eq(ComplianceBreachResponseDTO.class)))
                .thenReturn(responseDTO);

        ComplianceBreachResponseDTO result = analyticsService.closeBreach(1L);
        assertEquals(BreachStatus.CLOSED, result.getStatus());
    }

    @Test
    void testCloseBreach_wrongStatus() {
        ComplianceBreach breach = ComplianceBreach.builder()
                .breachId(1L).status(BreachStatus.OPEN).build();
        when(complianceBreachRepository.findById(1L)).thenReturn(Optional.of(breach));
        assertThrows(IllegalStateException.class, () -> analyticsService.closeBreach(1L));
    }

    @Test
    void testCloseBreach_notFound() {
        when(complianceBreachRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> analyticsService.closeBreach(999L));
    }

    @Test
    void testGetPerformanceByAccountId_positive() {
        PerformanceRecord record = PerformanceRecord.builder().recordId(1L).accountId(1L).build();
        when(performanceRecordRepository.findByAccountId(1L)).thenReturn(List.of(record));
        when(modelMapper.map(any(), eq(PerformanceRecordResponseDTO.class)))
                .thenReturn(new PerformanceRecordResponseDTO());

        List<PerformanceRecordResponseDTO> results = analyticsService.getPerformanceByAccountId(1L);
        assertEquals(1, results.size());
    }

    @Test
    void testGetAccountDashboard_positive() {
        when(performanceRecordRepository.findByAccountId(1L)).thenReturn(List.of());
        when(riskMeasureRepository.findByAccountId(1L)).thenReturn(List.of());
        when(complianceBreachRepository.findByAccountIdAndStatus(1L, BreachStatus.OPEN)).thenReturn(List.of());

        AccountDashboardDTO result = analyticsService.getAccountDashboard(1L);
        assertNotNull(result);
        assertEquals(1L, result.getAccountId());
    }
}
