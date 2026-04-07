package com.wealthpro.orderexecution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing the full lifecycle view of an order,
 * including its pre-trade checks, fills, and allocations.
 *
 * @author WealthPro Team
 */
@Data
@NoArgsConstructor
public class OrderLifecycleDTO {

    @Schema(description = "Order details")
    private OrderResponseDTO order;

    @Schema(description = "All pre-trade checks run against this order")
    private List<PreTradeCheckResponseDTO> preTradeChecks;

    @Schema(description = "All execution fills for this order")
    private List<ExecutionFillResponseDTO> executionFills;

    @Schema(description = "All allocations for this order")
    private List<AllocationResponseDTO> allocations;
}
