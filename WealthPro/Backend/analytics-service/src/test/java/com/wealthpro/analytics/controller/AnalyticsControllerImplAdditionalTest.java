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
 * Additional controller tests for AnalyticsControllerImpl — covers
 * endpoints not exercised by the existing AnalyticsControllerImplTest.
 */
@WebMvcTest(AnalyticsControllerImpl.class)
class AnalyticsControllerImplAdditionalTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AnalyticsService analyticsService;
    @MockitoBean private OwnershipGuard ownershipGuard;

    // ─── calculateMonthlyReturn ───────────────────────────────────────────────

    @Test
    void testCalculateMonthlyReturn_positive() throws Exception {
        PerformanceRecordResponseDTO dto = new PerformanceRecordResponseDTO();
        dto.setReturnPercentage(5.5);
        dto.setPeriod(Period.MONTHLY);

        when(analyticsService.calculateMonthlyReturn(1L, 1L)).thenReturn(dto);

        mockMvc.perform(post("/api/analytics/accounts/1/monthly-return")
                        .param("portfolioId", "1")
                        .header("X-Auth-Roles", "ROLE_RM"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.returnPercentage").value(5.5));
    }

    @Test
    void testCalculateMonthlyReturn_AccountNotFound_Returns404() throws Exception {
        when(analyticsService.calculateMonthlyReturn(999L, 1L))
                .thenThrow(new ResourceNotFoundException("Account", 999L));

        mockMvc.perform(post("/api/analytics/accounts/999/monthly-return")
                        .param("portfolioId", "1")
                        .header("X-Auth-Roles", "ROLE_RM"))
                .andExpect(status().isNotFound());
    }

    // ─── calculateDailyReturn — not found ─────────────────────────────────────

    @Test
    void testCalculateDailyReturn_AccountNotFound_Returns404() throws Exception {
        when(analyticsService.calculateDailyReturn(999L, 1L))
                .thenThrow(new ResourceNotFoundException("Account", 999L));

        mockMvc.perform(post("/api/analytics/accounts/999/daily-return")
                        .param("portfolioId", "1")
                        .header("X-Auth-Roles", "ROLE_ADMIN"))
                .andExpect(status().isNotFound());
    }

    // ─── getRiskMeasures ──────────────────────────────────────────────────────

    @Test
    void testGetRiskMeasures_positive() throws Exception {
        RiskMeasureResponseDTO dto = new RiskMeasureResponseDTO();
        dto.setMeasureType(MeasureType.VAR_95);

        when(analyticsService.getRiskMeasuresByAccountId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/analytics/accounts/1/risk-measures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetRiskMeasures_Empty_ReturnsEmptyList() throws Exception {
        when(analyticsService.getRiskMeasuresByAccountId(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/analytics/accounts/2/risk-measures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── runRiskAssessment — error case ──────────────────────────────────────

    @Test
    void testRunRiskAssessment_AccountNotFound_Returns404() throws Exception {
        when(analyticsService.runRiskAssessment(999L))
                .thenThrow(new ResourceNotFoundException("Account", 999L));

        mockMvc.perform(post("/api/analytics/accounts/999/risk-assessment")
                        .header("X-Auth-Roles", "ROLE_ADMIN"))
                .andExpect(status().isNotFound());
    }

    // ─── closeBreach ──────────────────────────────────────────────────────────

    @Test
    void testCloseBreach_positive() throws Exception {
        ComplianceBreachResponseDTO dto = new ComplianceBreachResponseDTO();
        dto.setStatus(BreachStatus.CLOSED);

        when(analyticsService.closeBreach(1L)).thenReturn(dto);

        mockMvc.perform(patch("/api/compliance-breaches/1/close")
                        .header("X-Auth-Roles", "ROLE_COMPLIANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void testCloseBreach_WrongStatus_Returns400() throws Exception {
        when(analyticsService.closeBreach(1L))
                .thenThrow(new IllegalStateException("Can only close ACKNOWLEDGED breaches"));

        mockMvc.perform(patch("/api/compliance-breaches/1/close")
                        .header("X-Auth-Roles", "ROLE_COMPLIANCE"))
                .andExpect(status().isBadRequest());
    }

    // ─── acknowledgeBreach — error case ──────────────────────────────────────

    @Test
    void testAcknowledgeBreach_NotFound_Returns404() throws Exception {
        when(analyticsService.acknowledgeBreach(999L))
                .thenThrow(new ResourceNotFoundException("ComplianceBreach", 999L));

        mockMvc.perform(patch("/api/compliance-breaches/999/acknowledge")
                        .header("X-Auth-Roles", "ROLE_ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAcknowledgeBreach_WrongStatus_Returns400() throws Exception {
        when(analyticsService.acknowledgeBreach(1L))
                .thenThrow(new IllegalStateException("Can only acknowledge OPEN breaches"));

        mockMvc.perform(patch("/api/compliance-breaches/1/acknowledge")
                        .header("X-Auth-Roles", "ROLE_ADMIN"))
                .andExpect(status().isBadRequest());
    }

    // ─── runComplianceScan ────────────────────────────────────────────────────

    @Test
    void testRunComplianceScan_WithBreaches_ReturnsBreaches() throws Exception {
        ComplianceBreachResponseDTO dto = new ComplianceBreachResponseDTO();
        dto.setStatus(BreachStatus.OPEN);

        when(analyticsService.runComplianceScan(1L)).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/analytics/accounts/1/compliance-scan")
                        .header("X-Auth-Roles", "ROLE_COMPLIANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testRunComplianceScan_AccountNotFound_Returns404() throws Exception {
        when(analyticsService.runComplianceScan(999L))
                .thenThrow(new ResourceNotFoundException("Account", 999L));

        mockMvc.perform(post("/api/analytics/accounts/999/compliance-scan")
                        .header("X-Auth-Roles", "ROLE_ADMIN"))
                .andExpect(status().isNotFound());
    }

    // ─── getBreaches ──────────────────────────────────────────────────────────

    @Test
    void testGetBreaches_WithData_ReturnsList() throws Exception {
        ComplianceBreachResponseDTO dto = new ComplianceBreachResponseDTO();
        dto.setStatus(BreachStatus.OPEN);

        when(analyticsService.getBreachesByAccountId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/analytics/accounts/1/breaches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ─── getDashboard — not found ─────────────────────────────────────────────

    @Test
    void testGetDashboard_withAllData() throws Exception {
        AccountDashboardDTO dto = new AccountDashboardDTO();
        dto.setAccountId(2L);
        dto.setPerformanceRecords(List.of(new PerformanceRecordResponseDTO()));
        dto.setRiskMeasures(List.of(new RiskMeasureResponseDTO()));
        dto.setComplianceBreaches(List.of());

        when(analyticsService.getAccountDashboard(2L)).thenReturn(dto);

        mockMvc.perform(get("/api/analytics/accounts/2/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(2))
                .andExpect(jsonPath("$.performanceRecords.length()").value(1));
    }
}
