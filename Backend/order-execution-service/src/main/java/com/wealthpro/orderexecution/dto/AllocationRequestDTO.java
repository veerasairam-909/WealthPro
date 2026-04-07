package com.wealthpro.orderexecution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating an allocation.
 *
 * @author WealthPro Team
 */
@Data
@NoArgsConstructor
public class AllocationRequestDTO {

    @Schema(description = "ID of the filled order", example = "1")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.allocation.orderId.null}")
    private Long orderId;

    @Schema(description = "Target account identifier", example = "2001")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.allocation.accountId.null}")
    private Long accountId;

    @Schema(description = "Number of units to allocate", example = "50")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.allocation.allocQuantity.null}")
    @Positive(message = "{com.wealthpro.orderexecution.dto.allocation.allocQuantity.positive}")
    private Integer allocQuantity;

    @Schema(description = "Price per allocated unit", example = "152.75")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.allocation.allocPrice.null}")
    @Positive(message = "{com.wealthpro.orderexecution.dto.allocation.allocPrice.positive}")
    private Double allocPrice;
}
