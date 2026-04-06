package com.wealthpro.analytics.service;

import com.wealthpro.analytics.dto.*;
import java.util.List;

/**
 * Service interface defining all analytics business operations: performance
 * calculations, risk assessments, compliance breach detection and lifecycle.
 *
 * @author WealthPro Team
 * @version 2.0
 */
public interface AnalyticsService {

    // =================== Performance ===================

    /** Retrieve all performance records for an account. */
    List<PerformanceRecordResponseDTO> getPerformanceByAccountId(Long accountId);

    /**
     * Calculate simulated daily return for an account and persist the record.
     * Uses dummy market data to produce a realistic-looking return percentage.
     */
    PerformanceRecordResponseDTO calculateDailyReturn(Long accountId, Long portfolioId);

    /**
     * Calculate simulated monthly return by aggregating daily returns.
     */
    PerformanceRecordResponseDTO calculateMonthlyReturn(Long accountId, Long portfolioId);

    // =================== Risk ===================

    /** Retrieve all risk measures for an account. */
    List<RiskMeasureResponseDTO> getRiskMeasuresByAccountId(Long accountId);

    /**
     * Run a full risk assessment — calculates VOLATILITY, MAX_DRAWDOWN,
     * VAR_95, and TRACKING_ERROR using simulated data.
     */
    List<RiskMeasureResponseDTO> runRiskAssessment(Long accountId);

    // =================== Compliance ===================

    /** Retrieve all compliance breaches for an account. */
    List<ComplianceBreachResponseDTO> getBreachesByAccountId(Long accountId);

    /**
     * Run a compliance scan detecting CONCENTRATION, EXPOSURE_LIMIT, and
     * RESTRICTED_SECURITY violations using simulated rules.
     */
    List<ComplianceBreachResponseDTO> runComplianceScan(Long accountId);

    /** Move a breach from OPEN to ACKNOWLEDGED. */
    ComplianceBreachResponseDTO acknowledgeBreach(Long breachId);

    /** Move a breach from ACKNOWLEDGED to CLOSED. */
    ComplianceBreachResponseDTO closeBreach(Long breachId);

    // =================== Dashboard ===================

    /** Get combined dashboard: performance + risk + open breaches. */
    AccountDashboardDTO getAccountDashboard(Long accountId);
}
