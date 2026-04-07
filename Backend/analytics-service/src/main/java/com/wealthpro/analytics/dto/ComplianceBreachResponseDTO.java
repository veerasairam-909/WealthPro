package com.wealthpro.analytics.dto;

import com.wealthpro.analytics.enums.BreachStatus;
import com.wealthpro.analytics.enums.Severity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for compliance breach data.
 */
@Data
@NoArgsConstructor
@Schema(description = "Compliance breach detection result")
public class ComplianceBreachResponseDTO {

    @Schema(description = "Breach identifier", example = "1")
    private Long breachId;

    @Schema(description = "Account identifier", example = "101")
    private Long accountId;

    @Schema(description = "Compliance rule that was violated", example = "CONCENTRATION_LIMIT")
    private String ruleViolated;

    @Schema(description = "Severity level", example = "HIGH")
    private Severity severity;

    @Schema(description = "Breach description", example = "Single holding 'RELIANCE' exceeds 15% concentration limit at 22.5%")
    private String description;

    @Schema(description = "Current breach status", example = "OPEN")
    private BreachStatus status;

    @Schema(description = "Detection timestamp", example = "2026-03-22T10:30:00")
    private LocalDateTime detectedAt;

    @Schema(description = "Resolution timestamp (null if unresolved)", example = "2026-03-22T14:00:00")
    private LocalDateTime resolvedAt;
}
