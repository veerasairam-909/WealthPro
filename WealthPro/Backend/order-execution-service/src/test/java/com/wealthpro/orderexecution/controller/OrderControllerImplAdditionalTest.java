package com.wealthpro.orderexecution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthpro.orderexecution.dto.*;
import com.wealthpro.orderexecution.enums.*;
import com.wealthpro.orderexecution.exception.ResourceNotFoundException;
import com.wealthpro.orderexecution.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Additional controller tests for OrderControllerImpl — covers endpoints
 * not exercised by the existing OrderControllerImplTest.
 */
@WebMvcTest(OrderControllerImpl.class)
class OrderControllerImplAdditionalTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private OrderService orderService;
    @Autowired private ObjectMapper objectMapper;

    private OrderResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new OrderResponseDTO();
        sampleResponse.setOrderId(1L);
        sampleResponse.setClientId(10L);
        sampleResponse.setStatus(OrderStatus.PLACED);
        sampleResponse.setOrderDate(LocalDateTime.now());

        // Many endpoints call orderService.getOrderById() via ensureOwnership — stub it
        lenient().when(orderService.getOrderById(1L)).thenReturn(sampleResponse);
    }

    // ─── getOrdersByClientId ──────────────────────────────────────────────────

    @Test
    void testGetOrdersByClientId_ReturnsOrders() throws Exception {
        when(orderService.getOrdersByClientId(10L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/orders/by-client")
                        .param("clientId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetOrdersByClientId_Empty_ReturnsEmptyList() throws Exception {
        when(orderService.getOrdersByClientId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/orders/by-client")
                        .param("clientId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── getOrdersByStatus ────────────────────────────────────────────────────

    @Test
    void testGetOrdersByStatus_ReturnsOrders() throws Exception {
        when(orderService.getOrdersByStatus(OrderStatus.PLACED)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/orders/by-status")
                        .param("status", "PLACED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ─── updateOrder ──────────────────────────────────────────────────────────

    @Test
    void testUpdateOrder_positive() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setClientId(10L);
        request.setSecurityId(100L);
        request.setSide(Side.BUY);
        request.setQuantity(50);
        request.setPriceType(PriceType.MARKET);

        OrderResponseDTO updatedResponse = new OrderResponseDTO();
        updatedResponse.setOrderId(1L);
        updatedResponse.setStatus(OrderStatus.PLACED);
        updatedResponse.setOrderDate(LocalDateTime.now());

        when(orderService.updateOrder(eq(1L), any(OrderRequestDTO.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void testUpdateOrder_OrderNotFound_Returns404() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setClientId(10L);
        request.setSecurityId(100L);
        request.setSide(Side.BUY);
        request.setQuantity(50);
        request.setPriceType(PriceType.MARKET);

        when(orderService.getOrderById(999L)).thenThrow(new ResourceNotFoundException("Order", 999L));

        mockMvc.perform(put("/api/orders/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ─── executeFill ──────────────────────────────────────────────────────────

    @Test
    void testExecuteFill_positive() throws Exception {
        ExecutionFillRequestDTO fillRequest = new ExecutionFillRequestDTO();
        fillRequest.setOrderId(1L);
        fillRequest.setFillQuantity(50);
        fillRequest.setFillPrice(150.0);

        ExecutionFillResponseDTO fillResponse = new ExecutionFillResponseDTO();
        fillResponse.setFillQuantity(50);
        fillResponse.setFillPrice(150.0);

        when(orderService.executeFill(eq(1L), any(ExecutionFillRequestDTO.class))).thenReturn(fillResponse);

        mockMvc.perform(post("/api/orders/1/fills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fillRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fillQuantity").value(50));
    }

    @Test
    void testExecuteFill_WrongStatus_Returns400() throws Exception {
        ExecutionFillRequestDTO fillRequest = new ExecutionFillRequestDTO();
        fillRequest.setOrderId(1L);
        fillRequest.setFillQuantity(50);
        fillRequest.setFillPrice(150.0);

        when(orderService.executeFill(eq(1L), any(ExecutionFillRequestDTO.class)))
                .thenThrow(new IllegalStateException("Fills can only be applied to ROUTED orders"));

        mockMvc.perform(post("/api/orders/1/fills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fillRequest)))
                .andExpect(status().isBadRequest());
    }

    // ─── allocateOrder ────────────────────────────────────────────────────────

    @Test
    void testAllocateOrder_positive() throws Exception {
        AllocationRequestDTO allocRequest = new AllocationRequestDTO();
        allocRequest.setOrderId(1L);
        allocRequest.setAccountId(200L);
        allocRequest.setAllocQuantity(50);
        allocRequest.setAllocPrice(150.0);

        AllocationResponseDTO allocResponse = new AllocationResponseDTO();
        allocResponse.setAllocQuantity(50);

        when(orderService.allocateOrder(eq(1L), any(AllocationRequestDTO.class))).thenReturn(allocResponse);

        mockMvc.perform(post("/api/orders/1/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.allocQuantity").value(50));
    }

    @Test
    void testAllocateOrder_NotFilled_Returns400() throws Exception {
        AllocationRequestDTO allocRequest = new AllocationRequestDTO();
        allocRequest.setOrderId(1L);
        allocRequest.setAccountId(200L);
        allocRequest.setAllocQuantity(50);
        allocRequest.setAllocPrice(150.0);

        when(orderService.allocateOrder(eq(1L), any(AllocationRequestDTO.class)))
                .thenThrow(new IllegalStateException("Allocations can only be created for FILLED orders"));

        mockMvc.perform(post("/api/orders/1/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocRequest)))
                .andExpect(status().isBadRequest());
    }

    // ─── getOrderLifecycle ────────────────────────────────────────────────────

    @Test
    void testGetOrderLifecycle_positive() throws Exception {
        OrderLifecycleDTO lifecycleDTO = new OrderLifecycleDTO();
        lifecycleDTO.setOrder(sampleResponse);
        lifecycleDTO.setPreTradeChecks(List.of());
        lifecycleDTO.setExecutionFills(List.of());
        lifecycleDTO.setAllocations(List.of());

        when(orderService.getOrderLifecycle(1L)).thenReturn(lifecycleDTO);

        mockMvc.perform(get("/api/orders/1/lifecycle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.orderId").value(1));
    }

    @Test
    void testGetOrderLifecycle_NotFound_Returns404() throws Exception {
        when(orderService.getOrderById(999L)).thenThrow(new ResourceNotFoundException("Order", 999L));

        mockMvc.perform(get("/api/orders/999/lifecycle"))
                .andExpect(status().isNotFound());
    }

    // ─── runPreTradeChecks — not found ────────────────────────────────────────

    @Test
    void testRunPreTradeChecks_NotFound_Returns404() throws Exception {
        when(orderService.getOrderById(999L)).thenThrow(new ResourceNotFoundException("Order", 999L));

        mockMvc.perform(post("/api/orders/999/pre-trade-checks"))
                .andExpect(status().isNotFound());
    }

    // ─── routeOrder — wrong status ────────────────────────────────────────────

    @Test
    void testRouteOrder_WrongStatus_Returns400() throws Exception {
        when(orderService.routeOrder(1L))
                .thenThrow(new IllegalStateException("Only VALIDATED orders can be routed"));

        mockMvc.perform(post("/api/orders/1/route"))
                .andExpect(status().isBadRequest());
    }

    // ─── cancelOrder — wrong status ───────────────────────────────────────────

    @Test
    void testCancelOrder_WrongStatus_Returns400() throws Exception {
        when(orderService.cancelOrder(1L))
                .thenThrow(new IllegalStateException("Cannot cancel order in FILLED status"));

        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isBadRequest());
    }

    // ─── deleteOrder — not found ──────────────────────────────────────────────

    @Test
    void testDeleteOrder_NotFound_Returns404() throws Exception {
        when(orderService.getOrderById(999L)).thenThrow(new ResourceNotFoundException("Order", 999L));

        mockMvc.perform(delete("/api/orders/999"))
                .andExpect(status().isNotFound());
    }
}
