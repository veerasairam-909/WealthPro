package com.wealthpro.orderexecution.dto;

import com.wealthpro.orderexecution.enums.OrderStatus;
import com.wealthpro.orderexecution.enums.PriceType;
import com.wealthpro.orderexecution.enums.Side;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning order data in API responses.
 *
 * @author WealthPro Team
 */
@Data
@NoArgsConstructor
@Schema(description = "Order response with current status and routing details")
public class OrderResponseDTO {

    @Schema(description = "Unique order identifier", example = "1")
    private Long orderId;

    @Schema(description = "Client / investor identifier", example = "101")
    private Long clientId;

    @Schema(description = "Security / instrument identifier", example = "5001")
    private Long securityId;

    @Schema(description = "Order side", example = "BUY")
    private Side side;

    @Schema(description = "Number of units", example = "100")
    private Integer quantity;

    @Schema(description = "Pricing mechanism", example = "MARKET")
    private PriceType priceType;

    @Schema(description = "Limit price (null for MARKET orders)", example = "150.50")
    private Double limitPrice;

    @Schema(description = "Date and time the order was placed", example = "2026-03-22T10:30:00")
    private LocalDateTime orderDate;

    @Schema(description = "Current order status", example = "PLACED")
    private OrderStatus status;

    @Schema(description = "Venue the order was routed to", example = "SIMULATED_NSE")
    private String routedVenue;
}
