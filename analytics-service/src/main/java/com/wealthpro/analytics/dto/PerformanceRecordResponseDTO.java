package com.wealthpro.analytics.dto;

import com.wealthpro.analytics.enums.Period;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for performance record data.
 */
@Data
@NoArgsConstructor
@Schema(description = "Portfolio performance return record")
public class PerformanceRecordResponseDTO {

    @Schema(description = "Record identifier", example = "1")
    private Long recordId;

    @Schema(description = "Account identifier", example = "101")
    private Long accountId;

    @Schema(description = "Portfolio identifier", example = "501")
    private Long portfolioId;

    @Schema(description = "Measurement period", example = "DAILY")
    private Period period;

    @Schema(description = "Period start date", example = "2026-03-22")
    private LocalDate startDate;

    @Schema(description = "Period end date", example = "2026-03-22")
    private LocalDate endDate;

    @Schema(description = "Portfolio return percentage", example = "2.45")
    private Double returnPercentage;

    @Schema(description = "Benchmark return percentage for comparison", example = "1.80")
    private Double benchmarkReturnPercentage;

    @Schema(description = "Calculation timestamp", example = "2026-03-22T10:30:00")
    private LocalDateTime calculatedAt;
}
