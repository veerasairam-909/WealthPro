package com.wealthpro.analytics.controller;

import com.wealthpro.analytics.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * API contract for the Analytics-Service.
 * All Swagger annotations live on this interface.
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Tag(name = "Analytics", description = "Performance, Risk Assessment, and Compliance Breach Management")
public interface AnalyticsController {

    // =================== Performance ===================

    @Operation(summary = "Get performance records by account ID")
    ResponseEntity<List<PerformanceRecordResponseDTO>> getPerformance(
            @PathVariable Long accountId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String roles,
            @RequestHeader(value = "X-Auth-Client-Id", required = false) Long authClientId);

    @Operation(summary = "Calculate daily return",
            description = "Simulates and persists a daily portfolio return")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Daily return calculated")})
    ResponseEntity<PerformanceRecordResponseDTO> calculateDailyReturn(
            @PathVariable Long accountId, @RequestParam Long portfolioId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String roles,
            @RequestHeader(value = "X-Auth-Client-Id", required = false) Long authClientId);

    @Operation(summary = "Calculate monthly return",
            description = "Aggregates daily returns into a monthly figure")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Monthly return calculated")})
    ResponseEntity<PerformanceRecordResponseDTO> calculateMonthlyReturn(
            @PathVariable Long accountId, @RequestParam Long portfolioId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String roles,
            @RequestHeader(value = "X-Auth-Client-Id", required = false) Long authClientId);

    // =================== Risk ===================

    @Operation(summary = "Get risk measures by account ID")
    ResponseEntity<List<RiskMeasureResponseDTO>> getRiskMeasures(
            @PathVariable Long accountId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String roles,
            @RequestHeader(value = "X-Auth-Client-Id", required = false) Long authClientId);

    @Operation(summary = "Run risk assessment",
            description = "Calculates VOLATILITY, MAX_DRAWDOWN, VAR_95, TRACKING_ERROR")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Risk assessment completed")})
    ResponseEntity<List<RiskMeasureResponseDTO>> runRiskAssessment(
            @PathVariable Long accountId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String roles,
            @RequestHeader(value = "X-Auth-Client-Id", required = false) Long authClientId);

    // =================== Compliance ===================

    @Operation(summary = "Get compliance breaches by account ID")
    ResponseEntity<List<ComplianceBreachResponseDTO>> getBreaches(
            @PathVariable Long accountId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String roles,
            @RequestHeader(value = "X-Auth-Client-Id", required = false) Long authClientId);

    @Operation(summary = "Run compliance scan",
            description = "Detects concentration, exposure, and restricted security violations")
    ResponseEntity<List<ComplianceBreachResponseDTO>> runComplianceScan(
            @PathVariable Long accountId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String roles,
            @RequestHeader(value = "X-Auth-Client-Id", required = false) Long authClientId);

    @Operation(summary = "Acknowledge a breach", description = "Moves OPEN → ACKNOWLEDGED")
    ResponseEntity<ComplianceBreachResponseDTO> acknowledgeBreach(
            @PathVariable Long breachId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String roles,
            @RequestHeader(value = "X-Auth-Client-Id", required = false) Long authClientId);

    @Operation(summary = "Close a breach", description = "Moves ACKNOWLEDGED → CLOSED")
    ResponseEntity<ComplianceBreachResponseDTO> closeBreach(
            @PathVariable Long breachId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String roles,
            @RequestHeader(value = "X-Auth-Client-Id", required = false) Long authClientId);

    // =================== Dashboard ===================

    @Operation(summary = "Get account dashboard",
            description = "Combined view of performance, risk, and open compliance breaches")
    ResponseEntity<AccountDashboardDTO> getAccountDashboard(
            @PathVariable Long accountId,
            @RequestHeader(value = "X-Auth-Roles", required = false) String roles,
            @RequestHeader(value = "X-Auth-Client-Id", required = false) Long authClientId);
}
