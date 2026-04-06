package com.wealthpro.orderexecution.dto;

import com.wealthpro.orderexecution.enums.PriceType;
import com.wealthpro.orderexecution.enums.Side;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a trading order.
 *
 * @author WealthPro Team
 */
@Data
@NoArgsConstructor
public class OrderRequestDTO {

    @Schema(description = "Client / investor identifier", example = "101")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.order.clientId.null}")
    private Long clientId;

    @Schema(description = "Security / instrument identifier", example = "5001")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.order.securityId.null}")
    private Long securityId;

    @Schema(description = "Order side", example = "BUY")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.order.side.null}")
    private Side side;

    @Schema(description = "Number of units to trade", example = "100")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.order.quantity.null}")
    @Positive(message = "{com.wealthpro.orderexecution.dto.order.quantity.positive}")
    private Integer quantity;

    @Schema(description = "Pricing mechanism", example = "MARKET")
    @NotNull(message = "{com.wealthpro.orderexecution.dto.order.priceType.null}")
    private PriceType priceType;

    @Schema(description = "Limit price (required when priceType = LIMIT)", example = "150.50")
    @Positive(message = "{com.wealthpro.orderexecution.dto.order.limitPrice.positive}")
    private Double limitPrice;
}
