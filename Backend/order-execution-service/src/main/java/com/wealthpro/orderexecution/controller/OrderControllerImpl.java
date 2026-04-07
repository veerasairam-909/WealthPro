package com.wealthpro.orderexecution.controller;

import com.wealthpro.orderexecution.dto.*;
import com.wealthpro.orderexecution.enums.OrderStatus;
import com.wealthpro.orderexecution.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Override
    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO requestDTO) {
        OrderResponseDTO created = orderService.createOrder(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @Override
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Override
    @GetMapping("/orders/by-client")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByClientId(@RequestParam Long clientId) {
        return ResponseEntity.ok(orderService.getOrdersByClientId(clientId));
    }

    @Override
    @GetMapping("/orders/by-status")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @Override
    @PutMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long orderId,
                                                         @Valid @RequestBody OrderRequestDTO requestDTO) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, requestDTO));
    }

    @Override
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @PostMapping("/orders/{orderId}/pre-trade-checks")
    public ResponseEntity<List<PreTradeCheckResponseDTO>> runPreTradeChecks(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.runAllPreTradeChecks(orderId));
    }

    @Override
    @PostMapping("/orders/{orderId}/route")
    public ResponseEntity<OrderResponseDTO> routeOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.routeOrder(orderId));
    }

    @Override
    @PostMapping("/orders/{orderId}/fills")
    public ResponseEntity<ExecutionFillResponseDTO> executeFill(@PathVariable Long orderId,
                                                                 @Valid @RequestBody ExecutionFillRequestDTO requestDTO) {
        return new ResponseEntity<>(orderService.executeFill(orderId, requestDTO), HttpStatus.CREATED);
    }

    @Override
    @PostMapping("/orders/{orderId}/allocations")
    public ResponseEntity<AllocationResponseDTO> allocateOrder(@PathVariable Long orderId,
                                                                @Valid @RequestBody AllocationRequestDTO requestDTO) {
        return new ResponseEntity<>(orderService.allocateOrder(orderId, requestDTO), HttpStatus.CREATED);
    }

    @Override
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    @Override
    @GetMapping("/orders/{orderId}/lifecycle")
    public ResponseEntity<OrderLifecycleDTO> getOrderLifecycle(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderLifecycle(orderId));
    }
}
