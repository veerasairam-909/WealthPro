package com.wealthpro.orderexecution.dto;

import com.wealthpro.orderexecution.enums.FillStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning execution fill data.
 *
 * @author WealthPro Team
 */
@Data
@NoArgsConstructor
@Schema(description = "Execution fill details for an order")
public class ExecutionFillResponseDTO {

    @Schema(description = "Fill identifier", example = "1")
    private Long fillId;

    @Schema(description = "Order identifier", example = "1")
    private Long orderId;

    @Schema(description = "Number of units filled", example = "50")
    private Integer fillQuantity;

    @Schema(description = "Price per unit", example = "152.75")
    private Double fillPrice;

    @Schema(description = "Fill date and time", example = "2026-03-22T10:35:00")
    private LocalDateTime fillDate;

    @Schema(description = "Execution venue", example = "SIMULATED_NSE")
    private String venue;

    @Schema(description = "Fill status", example = "COMPLETED")
    private FillStatus status;
}
