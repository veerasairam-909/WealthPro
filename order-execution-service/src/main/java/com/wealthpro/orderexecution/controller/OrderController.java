package com.wealthpro.orderexecution.controller;

import com.wealthpro.orderexecution.dto.*;
import com.wealthpro.orderexecution.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * API contract for Order management and the complete order lifecycle.
 * <p>
 * All Swagger/OpenAPI annotations live exclusively on this interface.
 * The {@link OrderControllerImpl} provides the runtime implementation.
 * </p>
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Tag(name = "Order Management", description = "Order placement, lifecycle, pre-trade checks, routing, fills, and allocations")
public interface OrderController {


    @Operation(summary = "Place a new order", description = "Creates a new order with status PLACED")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO requestDTO);

    @Operation(summary = "Get order by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId);

    @Operation(summary = "Get all orders")
    ResponseEntity<List<OrderResponseDTO>> getAllOrders();

    @Operation(summary = "Get orders by client ID")
    ResponseEntity<List<OrderResponseDTO>> getOrdersByClientId(@RequestParam Long clientId);

    @Operation(summary = "Get orders by status")
    ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@RequestParam OrderStatus status);

    @Operation(summary = "Update an order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long orderId,
                                                  @Valid @RequestBody OrderRequestDTO requestDTO);

    @Operation(summary = "Delete an order")
    @ApiResponse(responseCode = "204", description = "Order deleted")
    ResponseEntity<Void> deleteOrder(@PathVariable Long orderId);


    @Operation(summary = "Run all pre-trade checks",
            description = "Runs SUITABILITY, LIMIT, EXPOSURE, CASH checks. Order moves to VALIDATED or REJECTED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checks completed"),
            @ApiResponse(responseCode = "400", description = "Invalid order state")
    })
    ResponseEntity<List<PreTradeCheckResponseDTO>> runPreTradeChecks(@PathVariable Long orderId);

    @Operation(summary = "Route order to exchange",
            description = "Routes a VALIDATED order to a simulated venue")
    ResponseEntity<OrderResponseDTO> routeOrder(@PathVariable Long orderId);

    @Operation(summary = "Execute a fill against an order",
            description = "Records a fill and auto-updates PARTIALLY_FILLED / FILLED status")
    ResponseEntity<ExecutionFillResponseDTO> executeFill(@PathVariable Long orderId,
                                                         @Valid @RequestBody ExecutionFillRequestDTO requestDTO);

    @Operation(summary = "Allocate a filled order to an account")
    ResponseEntity<AllocationResponseDTO> allocateOrder(@PathVariable Long orderId,
                                                        @Valid @RequestBody AllocationRequestDTO requestDTO);

    @Operation(summary = "Cancel an order",
            description = "Cancels PLACED / VALIDATED / ROUTED orders")
    ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId);

    @Operation(summary = "Get full order lifecycle",
            description = "Returns order details with all checks, fills, and allocations")
    ResponseEntity<OrderLifecycleDTO> getOrderLifecycle(@PathVariable Long orderId);
}
