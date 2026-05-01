package com.wealthpro.orderexecution.controller;

import com.wealthpro.orderexecution.dto.*;
import com.wealthpro.orderexecution.enums.OrderStatus;
import com.wealthpro.orderexecution.security.AuthContext;
import com.wealthpro.orderexecution.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller implementation for {@link OrderController}.
 * <p>
 * Delegates all business logic to {@link OrderService}.
 * No Swagger annotations here — those live exclusively on the interface.
 * </p>
 *
 * @author WealthPro Team
 * @version 2.0
 */
@RestController
@RequestMapping("api")
public class OrderControllerImpl implements OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderControllerImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * For endpoints identified by orderId, fetch the order and ensure a CLIENT
     * caller is acting on their own data. Staff roles bypass the check.
     */
    private OrderResponseDTO ensureOwnership(AuthContext ctx, Long orderId) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        if (ctx.isClient() && !ctx.ownsClient(order.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Client does not own this order");
        }
        return order;
    }

    @Override
    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO requestDTO,
                                                         @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                         @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(requestDTO.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients may only create orders for their own clientId");
        }
        OrderResponseDTO created = orderService.createOrder(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId,
                                                         @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                         @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        OrderResponseDTO order = ensureOwnership(ctx, orderId);
        return ResponseEntity.ok(order);
    }

    @Override
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(@RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                               @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot list all orders; use ?clientId={yourClientId}");
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Override
    @GetMapping("/orders/by-client")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByClientId(@RequestParam Long clientId,
                                                                      @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                                      @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients may only view their own orders");
        }
        return ResponseEntity.ok(orderService.getOrdersByClientId(clientId));
    }

    @Override
    @GetMapping("/orders/by-status")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@RequestParam OrderStatus status,
                                                                    @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                                    @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot list orders by status across clients");
        }
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @Override
    @PutMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long orderId,
                                                         @Valid @RequestBody OrderRequestDTO requestDTO,
                                                         @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                         @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        ensureOwnership(ctx, orderId);
        if (ctx.isClient() && !ctx.ownsClient(requestDTO.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients may not reassign an order to another clientId");
        }
        return ResponseEntity.ok(orderService.updateOrder(orderId, requestDTO));
    }

    @Override
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId,
                                            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        ensureOwnership(ctx, orderId);
        orderService.deleteOrder(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @PostMapping("/orders/{orderId}/pre-trade-checks")
    public ResponseEntity<List<PreTradeCheckResponseDTO>> runPreTradeChecks(@PathVariable Long orderId,
                                                                            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                                            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        ensureOwnership(ctx, orderId);
        return ResponseEntity.ok(orderService.runAllPreTradeChecks(orderId));
    }

    @Override
    @PostMapping("/orders/{orderId}/route")
    public ResponseEntity<OrderResponseDTO> routeOrder(@PathVariable Long orderId,
                                                       @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                       @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        ensureOwnership(ctx, orderId);
        return ResponseEntity.ok(orderService.routeOrder(orderId));
    }

    @Override
    @PostMapping("/orders/{orderId}/fills")
    public ResponseEntity<ExecutionFillResponseDTO> executeFill(@PathVariable Long orderId,
                                                                 @Valid @RequestBody ExecutionFillRequestDTO requestDTO,
                                                                 @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                                 @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        ensureOwnership(ctx, orderId);
        return new ResponseEntity<>(orderService.executeFill(orderId, requestDTO), HttpStatus.CREATED);
    }

    @Override
    @PostMapping("/orders/{orderId}/allocations")
    public ResponseEntity<AllocationResponseDTO> allocateOrder(@PathVariable Long orderId,
                                                                @Valid @RequestBody AllocationRequestDTO requestDTO,
                                                                @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                                @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        ensureOwnership(ctx, orderId);
        return new ResponseEntity<>(orderService.allocateOrder(orderId, requestDTO), HttpStatus.CREATED);
    }

    @Override
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId,
                                                        @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                        @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        ensureOwnership(ctx, orderId);
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    @Override
    @GetMapping("/orders/{orderId}/lifecycle")
    public ResponseEntity<OrderLifecycleDTO> getOrderLifecycle(@PathVariable Long orderId,
                                                               @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
                                                               @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        ensureOwnership(ctx, orderId);
        return ResponseEntity.ok(orderService.getOrderLifecycle(orderId));
    }
}
