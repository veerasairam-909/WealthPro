package com.wealthpro.orderexecution.dto;

import com.wealthpro.orderexecution.enums.FillStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating an execution fill.
 *
 * @author WealthPro Team
 */
@Data
@NoArgsConstructor
public class ExecutionFillRequestDTO {

    @Schema(description = "ID of the order being filled", example = "1")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.fill.orderId.null}")
    private Long orderId;

    @Schema(description = "Number of units filled", example = "50")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.fill.fillQuantity.null}")
    @Positive(message = "{com.wealthpro.orderexecution.dto.fill.fillQuantity.positive}")
    private Integer fillQuantity;

    @Schema(description = "Price per unit", example = "152.75")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.fill.fillPrice.null}")
    @Positive(message = "{com.wealthpro.orderexecution.dto.fill.fillPrice.positive}")
    private Double fillPrice;

    @Schema(description = "Execution venue", example = "NSE")
    private String venue;
}
