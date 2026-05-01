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
 * Controller tests for OrderControllerImpl using MockMvc.
 */
@WebMvcTest(OrderControllerImpl.class)
class OrderControllerImplTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private OrderService orderService;
    @Autowired private ObjectMapper objectMapper;

    private OrderRequestDTO validRequest;
    private OrderResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = new OrderRequestDTO();
        validRequest.setClientId(10L);
        validRequest.setSecurityId(100L);
        validRequest.setSide(Side.BUY);
        validRequest.setQuantity(50);
        validRequest.setPriceType(PriceType.MARKET);

        sampleResponse = new OrderResponseDTO();
        sampleResponse.setOrderId(1L);
        sampleResponse.setStatus(OrderStatus.PLACED);
        sampleResponse.setOrderDate(LocalDateTime.now());
    }

    @Test
    void testCreateOrder_positive() throws Exception {
        when(orderService.createOrder(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void testCreateOrder_negative() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequestDTO())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetOrderById_positive() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(sampleResponse);
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void testGetOrderById_exception() throws Exception {
        when(orderService.getOrderById(999L)).thenThrow(new ResourceNotFoundException("Order", 999L));
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRunPreTradeChecks_positive() throws Exception {
        PreTradeCheckResponseDTO checkDTO = new PreTradeCheckResponseDTO();
        checkDTO.setResult(CheckResult.PASS);
        when(orderService.runAllPreTradeChecks(1L)).thenReturn(List.of(checkDTO));

        mockMvc.perform(post("/api/orders/1/pre-trade-checks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testRouteOrder_positive() throws Exception {
        sampleResponse.setStatus(OrderStatus.ROUTED);
        when(orderService.routeOrder(1L)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/orders/1/route"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ROUTED"));
    }

    @Test
    void testCancelOrder_positive() throws Exception {
        sampleResponse.setStatus(OrderStatus.CANCELLED);
        when(orderService.cancelOrder(1L)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void testDeleteOrder_positive() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetAllOrders_positive() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(sampleResponse));
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
