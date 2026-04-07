package com.wealthpro.analytics.controller;

import com.wealthpro.analytics.dto.*;
import com.wealthpro.analytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller implementation for {@link AnalyticsController}.
 * Delegates all business logic to {@link AnalyticsService}.
 *
 * @author WealthPro Team
 * @version 2.0
 */
@RestController
@RequestMapping("api")
public class AnalyticsControllerImpl implements AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsControllerImpl(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // =================== Performance ===================

    @Override
    @GetMapping("/analytics/accounts/{accountId}/performance")
    public ResponseEntity<List<PerformanceRecordResponseDTO>> getPerformance(@PathVariable Long accountId) {
        return ResponseEntity.ok(analyticsService.getPerformanceByAccountId(accountId));
    }

    @Override
    @PostMapping("/analytics/accounts/{accountId}/daily-return")
    public ResponseEntity<PerformanceRecordResponseDTO> calculateDailyReturn(
            @PathVariable Long accountId, @RequestParam Long portfolioId) {
        return new ResponseEntity<>(analyticsService.calculateDailyReturn(accountId, portfolioId), HttpStatus.CREATED);
    }

    @Override
    @PostMapping("/analytics/accounts/{accountId}/monthly-return")
    public ResponseEntity<PerformanceRecordResponseDTO> calculateMonthlyReturn(
            @PathVariable Long accountId, @RequestParam Long portfolioId) {
        return new ResponseEntity<>(analyticsService.calculateMonthlyReturn(accountId, portfolioId), HttpStatus.CREATED);
    }

    // =================== Risk ===================

    @Override
    @GetMapping("/analytics/accounts/{accountId}/risk-measures")
    public ResponseEntity<List<RiskMeasureResponseDTO>> getRiskMeasures(@PathVariable Long accountId) {
        return ResponseEntity.ok(analyticsService.getRiskMeasuresByAccountId(accountId));
    }

    @Override
    @PostMapping("/analytics/accounts/{accountId}/risk-assessment")
    public ResponseEntity<List<RiskMeasureResponseDTO>> runRiskAssessment(@PathVariable Long accountId) {
        return new ResponseEntity<>(analyticsService.runRiskAssessment(accountId), HttpStatus.CREATED);
    }

    // =================== Compliance ===================

    @Override
    @GetMapping("/analytics/accounts/{accountId}/breaches")
    public ResponseEntity<List<ComplianceBreachResponseDTO>> getBreaches(@PathVariable Long accountId) {
        return ResponseEntity.ok(analyticsService.getBreachesByAccountId(accountId));
    }

    @Override
    @PostMapping("/analytics/accounts/{accountId}/compliance-scan")
    public ResponseEntity<List<ComplianceBreachResponseDTO>> runComplianceScan(@PathVariable Long accountId) {
        return ResponseEntity.ok(analyticsService.runComplianceScan(accountId));
    }

    @Override
    @PatchMapping("/compliance-breaches/{breachId}/acknowledge")
    public ResponseEntity<ComplianceBreachResponseDTO> acknowledgeBreach(@PathVariable Long breachId) {
        return ResponseEntity.ok(analyticsService.acknowledgeBreach(breachId));
    }

    @Override
    @PatchMapping("/compliance-breaches/{breachId}/close")
    public ResponseEntity<ComplianceBreachResponseDTO> closeBreach(@PathVariable Long breachId) {
        return ResponseEntity.ok(analyticsService.closeBreach(breachId));
    }

    // =================== Dashboard ===================

    @Override
    @GetMapping("/analytics/accounts/{accountId}/dashboard")
    public ResponseEntity<AccountDashboardDTO> getAccountDashboard(@PathVariable Long accountId) {
        return ResponseEntity.ok(analyticsService.getAccountDashboard(accountId));
    }
}
