package com.wealthpro.orderexecution.dto;

import com.wealthpro.orderexecution.enums.CheckResult;
import com.wealthpro.orderexecution.enums.CheckType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning pre-trade check results.
 *
 * @author WealthPro Team
 */
@Data
@NoArgsConstructor
@Schema(description = "Pre-trade compliance check result")
public class PreTradeCheckResponseDTO {

    @Schema(description = "Check identifier", example = "1")
    private Long checkId;

    @Schema(description = "Order this check belongs to", example = "1")
    private Long orderId;

    @Schema(description = "Type of compliance check", example = "SUITABILITY")
    private CheckType checkType;

    @Schema(description = "Check outcome", example = "PASS")
    private CheckResult result;

    @Schema(description = "Human-readable check message", example = "Suitability check passed — client profile is compatible")
    private String message;

    @Schema(description = "Date the check was performed", example = "2026-03-22T10:31:00")
    private LocalDateTime checkedDate;
}
