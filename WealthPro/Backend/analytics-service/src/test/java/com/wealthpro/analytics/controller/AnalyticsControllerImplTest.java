package com.wealthpro.analytics.controller;

import com.wealthpro.analytics.dto.*;
import com.wealthpro.analytics.enums.*;
import com.wealthpro.analytics.exception.ResourceNotFoundException;
import com.wealthpro.analytics.security.OwnershipGuard;
import com.wealthpro.analytics.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for AnalyticsControllerImpl.
 */
@WebMvcTest(AnalyticsControllerImpl.class)
class AnalyticsControllerImplTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AnalyticsService analyticsService;
    @MockitoBean private OwnershipGuard ownershipGuard;

    @Test
    void testCalculateDailyReturn_positive() throws Exception {
        PerformanceRecordResponseDTO dto = new PerformanceRecordResponseDTO();
        dto.setReturnPercentage(2.5);
        when(analyticsService.calculateDailyReturn(1L, 1L)).thenReturn(dto);

        mockMvc.perform(post("/api/analytics/accounts/1/daily-return")
                        .param("portfolioId", "1")
                        .header("X-Auth-Roles", "ROLE_ADMIN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.returnPercentage").value(2.5));
    }

    @Test
    void testRunRiskAssessment_positive() throws Exception {
        RiskMeasureResponseDTO dto = new RiskMeasureResponseDTO();
        dto.setMeasureType(MeasureType.VOLATILITY);
        when(analyticsService.runRiskAssessment(1L)).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/analytics/accounts/1/risk-assessment")
                        .header("X-Auth-Roles", "ROLE_ADMIN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testRunComplianceScan_positive() throws Exception {
        when(analyticsService.runComplianceScan(1L)).thenReturn(List.of());
        mockMvc.perform(post("/api/analytics/accounts/1/compliance-scan")
                        .header("X-Auth-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void testAcknowledgeBreach_positive() throws Exception {
        ComplianceBreachResponseDTO dto = new ComplianceBreachResponseDTO();
        dto.setStatus(BreachStatus.ACKNOWLEDGED);
        when(analyticsService.acknowledgeBreach(1L)).thenReturn(dto);

        mockMvc.perform(patch("/api/compliance-breaches/1/acknowledge")
                        .header("X-Auth-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));
    }

    @Test
    void testCloseBreach_notFound() throws Exception {
        when(analyticsService.closeBreach(999L)).thenThrow(new ResourceNotFoundException("Breach", 999L));
        mockMvc.perform(patch("/api/compliance-breaches/999/close")
                        .header("X-Auth-Roles", "ROLE_ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDashboard_positive() throws Exception {
        AccountDashboardDTO dto = new AccountDashboardDTO();
        dto.setAccountId(1L);
        dto.setPerformanceRecords(List.of());
        dto.setRiskMeasures(List.of());
        dto.setComplianceBreaches(List.of());
        when(analyticsService.getAccountDashboard(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/analytics/accounts/1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1));
    }

    @Test
    void testGetPerformance_positive() throws Exception {
        when(analyticsService.getPerformanceByAccountId(1L)).thenReturn(List.of());
        mockMvc.perform(get("/api/analytics/accounts/1/performance"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetBreaches_positive() throws Exception {
        when(analyticsService.getBreachesByAccountId(1L)).thenReturn(List.of());
        mockMvc.perform(get("/api/analytics/accounts/1/breaches"))
                .andExpect(status().isOk());
    }
}
