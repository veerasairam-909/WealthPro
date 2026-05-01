package com.wealthpro.orderexecution.service;

import com.wealthpro.orderexecution.dto.*;
import com.wealthpro.orderexecution.entities.Order;
import com.wealthpro.orderexecution.entities.PreTradeCheck;
import com.wealthpro.orderexecution.entities.ExecutionFill;
import com.wealthpro.orderexecution.enums.*;
import com.wealthpro.orderexecution.exception.ResourceNotFoundException;
import com.wealthpro.orderexecution.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrderServiceImpl} covering CRUD and business logic.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PreTradeCheckRepository preTradeCheckRepository;
    @Mock private ExecutionFillRepository executionFillRepository;
    @Mock private AllocationRepository allocationRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private OrderServiceImpl orderService;

    private Order sampleOrder;
    private OrderRequestDTO sampleRequest;
    private OrderResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        sampleOrder = Order.builder()
                .orderId(1L).clientId(10L).securityId(100L)
                .side(Side.BUY).quantity(50).priceType(PriceType.MARKET)
                .status(OrderStatus.PLACED).orderDate(LocalDateTime.now())
                .build();

        sampleRequest = new OrderRequestDTO();
        sampleRequest.setClientId(10L);
        sampleRequest.setSecurityId(100L);
        sampleRequest.setSide(Side.BUY);
        sampleRequest.setQuantity(50);
        sampleRequest.setPriceType(PriceType.MARKET);

        sampleResponse = new OrderResponseDTO();
        sampleResponse.setOrderId(1L);
        sampleResponse.setStatus(OrderStatus.PLACED);
    }

    @Test
    void testCreateOrder_positive() {
        when(modelMapper.map(any(OrderRequestDTO.class), eq(Order.class))).thenReturn(sampleOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(sampleResponse);

        OrderResponseDTO result = orderService.createOrder(sampleRequest);
        assertNotNull(result);
        assertEquals(OrderStatus.PLACED, result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testGetOrderById_positive() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(sampleResponse);

        OrderResponseDTO result = orderService.getOrderById(1L);
        assertNotNull(result);
    }

    @Test
    void testGetOrderById_exception() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    void testRunAllPreTradeChecks_positive() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(preTradeCheckRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(PreTradeCheck.class), eq(PreTradeCheckResponseDTO.class)))
                .thenReturn(new PreTradeCheckResponseDTO());
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        List<PreTradeCheckResponseDTO> results = orderService.runAllPreTradeChecks(1L);
        assertEquals(4, results.size()); // 4 check types
    }

    @Test
    void testRunAllPreTradeChecks_wrongStatus() {
        sampleOrder.setStatus(OrderStatus.FILLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        assertThrows(IllegalStateException.class, () -> orderService.runAllPreTradeChecks(1L));
    }

    @Test
    void testRouteOrder_positive() {
        sampleOrder.setStatus(OrderStatus.VALIDATED);
        OrderResponseDTO routedResponse = new OrderResponseDTO();
        routedResponse.setStatus(OrderStatus.ROUTED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(routedResponse);

        OrderResponseDTO result = orderService.routeOrder(1L);
        assertEquals(OrderStatus.ROUTED, result.getStatus());
    }

    @Test
    void testRouteOrder_wrongStatus() {
        sampleOrder.setStatus(OrderStatus.PLACED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        assertThrows(IllegalStateException.class, () -> orderService.routeOrder(1L));
    }

    @Test
    void testCancelOrder_positive() {
        sampleOrder.setStatus(OrderStatus.PLACED);
        OrderResponseDTO cancelledResponse = new OrderResponseDTO();
        cancelledResponse.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(cancelledResponse);

        OrderResponseDTO result = orderService.cancelOrder(1L);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelOrder_wrongStatus() {
        sampleOrder.setStatus(OrderStatus.FILLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void testExecuteFill_positive() {
        sampleOrder.setStatus(OrderStatus.ROUTED);
        sampleOrder.setRoutedVenue("SIMULATED_NSE");

        ExecutionFillRequestDTO fillRequest = new ExecutionFillRequestDTO();
        fillRequest.setFillQuantity(50);
        fillRequest.setFillPrice(152.75);

        ExecutionFill savedFill = ExecutionFill.builder()
                .fillId(1L).order(sampleOrder).fillQuantity(50).fillPrice(152.75)
                .status(FillStatus.COMPLETED).fillDate(LocalDateTime.now()).build();

        ExecutionFillResponseDTO fillResponse = new ExecutionFillResponseDTO();
        fillResponse.setFillQuantity(50);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(executionFillRepository.save(any(ExecutionFill.class))).thenReturn(savedFill);
        when(executionFillRepository.findByOrder_OrderId(1L)).thenReturn(List.of(savedFill));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(ExecutionFill.class), eq(ExecutionFillResponseDTO.class))).thenReturn(fillResponse);

        ExecutionFillResponseDTO result = orderService.executeFill(1L, fillRequest);
        assertNotNull(result);
        assertEquals(50, result.getFillQuantity());
    }

    @Test
    void testDeleteOrder_positive() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        doNothing().when(orderRepository).delete(sampleOrder);
        assertDoesNotThrow(() -> orderService.deleteOrder(1L));
    }

    @Test
    void testGetAllOrders_positive() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder));
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(sampleResponse);

        List<OrderResponseDTO> results = orderService.getAllOrders();
        assertEquals(1, results.size());
    }
}
