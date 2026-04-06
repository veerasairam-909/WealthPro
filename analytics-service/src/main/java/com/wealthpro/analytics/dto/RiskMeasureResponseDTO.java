package com.wealthpro.analytics.dto;

import com.wealthpro.analytics.enums.MeasureType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for risk measure data.
 */
@Data
@NoArgsConstructor
@Schema(description = "Risk measure calculation result")
public class RiskMeasureResponseDTO {

    @Schema(description = "Measure identifier", example = "1")
    private Long measureId;

    @Schema(description = "Account identifier", example = "101")
    private Long accountId;

    @Schema(description = "Type of risk measure", example = "VOLATILITY")
    private MeasureType measureType;

    @Schema(description = "Calculated measure value", example = "14.75")
    private Double measureValue;

    @Schema(description = "Description of the calculation", example = "Annualised portfolio volatility based on simulated daily returns")
    private String description;

    @Schema(description = "Calculation timestamp", example = "2026-03-22T10:30:00")
    private LocalDateTime calculatedAt;
}
