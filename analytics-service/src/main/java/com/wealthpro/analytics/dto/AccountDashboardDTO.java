package com.wealthpro.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a combined account dashboard with
 * performance records, risk measures, and compliance breaches.
 */
@Data @NoArgsConstructor
public class AccountDashboardDTO {
    @Schema(description = "Account identifier") private Long accountId;
    @Schema(description = "Recent performance records") private List<PerformanceRecordResponseDTO> performanceRecords;
    @Schema(description = "Recent risk measures") private List<RiskMeasureResponseDTO> riskMeasures;
    @Schema(description = "Open compliance breaches") private List<ComplianceBreachResponseDTO> complianceBreaches;
}
