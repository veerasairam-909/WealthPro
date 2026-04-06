package com.wealthpro.orderexecution.service;

import com.wealthpro.orderexecution.dto.*;
import com.wealthpro.orderexecution.enums.OrderStatus;

import java.util.List;

/**
 * Service interface defining all business operations for the Order domain.
 * <p>
 * Covers the full order lifecycle: placement, pre-trade validation,
 * routing, execution fills, allocations, and cancellation.
 * </p>
 *
 * @author WealthPro Team
 * @version 2.0
 */
public interface OrderService {

    /** Create a new order with status PLACED. */
    OrderResponseDTO createOrder(OrderRequestDTO requestDTO);

    /** Retrieve an order by its ID. */
    OrderResponseDTO getOrderById(Long orderId);

    /** Retrieve all orders. */
    List<OrderResponseDTO> getAllOrders();

    /** Retrieve all orders for a specific client. */
    List<OrderResponseDTO> getOrdersByClientId(Long clientId);

    /** Retrieve all orders with a given status. */
    List<OrderResponseDTO> getOrdersByStatus(OrderStatus status);

    /** Update an existing order's details. */
    OrderResponseDTO updateOrder(Long orderId, OrderRequestDTO requestDTO);

    /** Delete an order by ID. */
    void deleteOrder(Long orderId);

    /**
     * Run all four pre-trade compliance checks (SUITABILITY, LIMIT, EXPOSURE, CASH)
     * against the specified order. Auto-transitions the order to VALIDATED if all pass,
     * or REJECTED if any fail.
     */
    List<PreTradeCheckResponseDTO> runAllPreTradeChecks(Long orderId);

    /**
     * Route a VALIDATED order to a simulated exchange venue.
     */
    OrderResponseDTO routeOrder(Long orderId);

    /**
     * Record an execution fill and auto-update order status
     * (PARTIALLY_FILLED or FILLED based on cumulative fill quantity).
     */
    ExecutionFillResponseDTO executeFill(Long orderId, ExecutionFillRequestDTO requestDTO);

    /**
     * Allocate a FILLED order to a client account.
     */
    AllocationResponseDTO allocateOrder(Long orderId, AllocationRequestDTO requestDTO);

    /**
     * Cancel an order. Only orders in PLACED, VALIDATED, or ROUTED status
     * can be cancelled.
     */
    OrderResponseDTO cancelOrder(Long orderId);

    /**
     * Retrieve the full lifecycle view of an order
     */
    OrderLifecycleDTO getOrderLifecycle(Long orderId);
}
