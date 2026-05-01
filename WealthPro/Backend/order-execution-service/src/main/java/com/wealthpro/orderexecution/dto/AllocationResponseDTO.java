package com.wealthpro.orderexecution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning allocation data.
 *
 * @author WealthPro Team
 */
@Data
@NoArgsConstructor
@Schema(description = "Allocation of a filled order to a client account")
public class AllocationResponseDTO {

    @Schema(description = "Allocation identifier", example = "1")
    private Long allocationId;

    @Schema(description = "Order identifier", example = "1")
    private Long orderId;

    @Schema(description = "Target account identifier", example = "2001")
    private Long accountId;

    @Schema(description = "Number of allocated units", example = "50")
    private Integer allocQuantity;

    @Schema(description = "Price per allocated unit", example = "152.75")
    private Double allocPrice;

    @Schema(description = "Allocation date", example = "2026-03-22T10:40:00")
    private LocalDateTime allocDate;
}
