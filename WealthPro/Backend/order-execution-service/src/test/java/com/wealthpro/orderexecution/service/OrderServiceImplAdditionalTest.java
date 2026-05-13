package com.wealthpro.orderexecution.service;

import com.wealthpro.orderexecution.dto.*;
import com.wealthpro.orderexecution.entities.*;
import com.wealthpro.orderexecution.enums.*;
import com.wealthpro.orderexecution.exception.ResourceNotFoundException;
import com.wealthpro.orderexecution.feign.NotificationFeignClient;
import com.wealthpro.orderexecution.feign.PborFeignClient;
import com.wealthpro.orderexecution.feign.ProductCatalogFeignClient;
import com.wealthpro.orderexecution.feign.WealthproFeignClient;
import com.wealthpro.orderexecution.feign.dto.*;
import com.wealthpro.orderexecution.repository.*;
import feign.FeignException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Additional unit tests for OrderServiceImpl covering missing paths.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplAdditionalTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PreTradeCheckRepository preTradeCheckRepository;
    @Mock private ExecutionFillRepository executionFillRepository;
    @Mock private AllocationRepository allocationRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private WealthproFeignClient wealthproFeignClient;
    @Mock private ProductCatalogFeignClient productCatalogFeignClient;
    @Mock private PborFeignClient pborFeignClient;
    @Mock private NotificationFeignClient notificationFeignClient;
    @Mock private SuitabilityRuleEvaluator suitabilityRuleEvaluator;

    @InjectMocks private OrderServiceImpl orderService;

    private Order sampleOrder;
    private OrderResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        // default lenient stubs for feign calls used in many methods
        lenient().when(wealthproFeignClient.getClientById(anyLong())).thenReturn(new ClientDTO());
        lenient().when(productCatalogFeignClient.getSecurityById(anyLong())).thenReturn(new SecurityDTO());
        lenient().when(wealthproFeignClient.getRiskProfileByClientId(anyLong())).thenReturn(new RiskProfileDTO());
        lenient().when(wealthproFeignClient.getAllSuitabilityRules()).thenReturn(List.of());
        lenient().when(pborFeignClient.getAccountsByClientId(anyLong())).thenReturn(List.of());

        sampleOrder = Order.builder()
                .orderId(1L).clientId(10L).securityId(100L)
                .side(Side.BUY).quantity(50).priceType(PriceType.MARKET)
                .status(OrderStatus.PLACED).orderDate(LocalDateTime.now())
                .build();

        sampleResponse = new OrderResponseDTO();
        sampleResponse.setOrderId(1L);
        sampleResponse.setClientId(10L);
        sampleResponse.setStatus(OrderStatus.PLACED);
    }

    // ─── getOrdersByClientId ──────────────────────────────────────────────────

    @Test
    void testGetOrdersByClientId_ReturnsList() {
        when(orderRepository.findByClientId(10L)).thenReturn(List.of(sampleOrder));
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(sampleResponse);

        List<OrderResponseDTO> result = orderService.getOrdersByClientId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByClientId(10L);
    }

    @Test
    void testGetOrdersByClientId_EmptyList() {
        when(orderRepository.findByClientId(99L)).thenReturn(List.of());

        List<OrderResponseDTO> result = orderService.getOrdersByClientId(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── getOrdersByStatus ────────────────────────────────────────────────────

    @Test
    void testGetOrdersByStatus_ReturnsList() {
        when(orderRepository.findByStatus(OrderStatus.PLACED)).thenReturn(List.of(sampleOrder));
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(sampleResponse);

        List<OrderResponseDTO> result = orderService.getOrdersByStatus(OrderStatus.PLACED);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByStatus(OrderStatus.PLACED);
    }

    @Test
    void testGetOrdersByStatus_EmptyList() {
        when(orderRepository.findByStatus(OrderStatus.CANCELLED)).thenReturn(List.of());

        List<OrderResponseDTO> result = orderService.getOrdersByStatus(OrderStatus.CANCELLED);

        assertTrue(result.isEmpty());
    }

    // ─── updateOrder ──────────────────────────────────────────────────────────

    @Test
    void testUpdateOrder_Success() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setClientId(10L);
        request.setSecurityId(200L);
        request.setSide(Side.SELL);
        request.setQuantity(30);
        request.setPriceType(PriceType.LIMIT);
        request.setLimitPrice(150.0);

        OrderResponseDTO updatedResponse = new OrderResponseDTO();
        updatedResponse.setOrderId(1L);
        updatedResponse.setStatus(OrderStatus.PLACED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(updatedResponse);

        OrderResponseDTO result = orderService.updateOrder(1L, request);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testUpdateOrder_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateOrder(999L, new OrderRequestDTO()));
    }

    // ─── deleteOrder ──────────────────────────────────────────────────────────

    @Test
    void testDeleteOrder_NotFound_ThrowsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(999L));
    }

    // ─── routeOrder — venue selection ─────────────────────────────────────────

    @Test
    void testRouteOrder_LimitOrder_RoutesToBSE() {
        sampleOrder.setStatus(OrderStatus.VALIDATED);
        sampleOrder.setPriceType(PriceType.LIMIT);

        OrderResponseDTO routedResponse = new OrderResponseDTO();
        routedResponse.setStatus(OrderStatus.ROUTED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(routedResponse);

        OrderResponseDTO result = orderService.routeOrder(1L);

        assertNotNull(result);
        // verify route was set on the order entity
        assertEquals("SIMULATED_BSE", sampleOrder.getRoutedVenue());
    }

    @Test
    void testRouteOrder_NavOrder_RoutesToMFPlatform() {
        sampleOrder.setStatus(OrderStatus.VALIDATED);
        sampleOrder.setPriceType(PriceType.NAV);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(new OrderResponseDTO());

        orderService.routeOrder(1L);

        assertEquals("SIMULATED_MF_PLATFORM", sampleOrder.getRoutedVenue());
    }

    @Test
    void testRouteOrder_MarketOrder_RoutesToNSE() {
        sampleOrder.setStatus(OrderStatus.VALIDATED);
        sampleOrder.setPriceType(PriceType.MARKET);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(new OrderResponseDTO());

        orderService.routeOrder(1L);

        assertEquals("SIMULATED_NSE", sampleOrder.getRoutedVenue());
    }

    // ─── executeFill — partial fill ───────────────────────────────────────────

    @Test
    void testExecuteFill_PartialFill_StatusBecomesPartiallyFilled() {
        sampleOrder.setStatus(OrderStatus.ROUTED);
        sampleOrder.setQuantity(100); // need 100 units total
        sampleOrder.setRoutedVenue("SIMULATED_NSE");

        ExecutionFillRequestDTO fillRequest = new ExecutionFillRequestDTO();
        fillRequest.setFillQuantity(30); // only 30 of 100 filled
        fillRequest.setFillPrice(100.0);

        ExecutionFill savedFill = ExecutionFill.builder()
                .fillId(1L).order(sampleOrder).fillQuantity(30)
                .fillPrice(100.0).status(FillStatus.COMPLETED)
                .fillDate(LocalDateTime.now()).build();

        ExecutionFillResponseDTO fillResponse = new ExecutionFillResponseDTO();
        fillResponse.setFillQuantity(30);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(executionFillRepository.save(any(ExecutionFill.class))).thenReturn(savedFill);
        when(executionFillRepository.findByOrder_OrderId(1L)).thenReturn(List.of(savedFill));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(ExecutionFill.class), eq(ExecutionFillResponseDTO.class))).thenReturn(fillResponse);

        ExecutionFillResponseDTO result = orderService.executeFill(1L, fillRequest);

        assertNotNull(result);
        // order should be PARTIALLY_FILLED since 30 < 100
        assertEquals(OrderStatus.PARTIALLY_FILLED, sampleOrder.getStatus());
    }

    @Test
    void testExecuteFill_WrongStatus_ThrowsException() {
        sampleOrder.setStatus(OrderStatus.PLACED); // must be ROUTED or PARTIALLY_FILLED

        ExecutionFillRequestDTO fillRequest = new ExecutionFillRequestDTO();
        fillRequest.setFillQuantity(10);
        fillRequest.setFillPrice(50.0);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThrows(IllegalStateException.class, () -> orderService.executeFill(1L, fillRequest));
    }

    // ─── allocateOrder ────────────────────────────────────────────────────────

    @Test
    void testAllocateOrder_NotFilledStatus_ThrowsException() {
        sampleOrder.setStatus(OrderStatus.ROUTED); // must be FILLED

        AllocationRequestDTO allocRequest = new AllocationRequestDTO();
        allocRequest.setAccountId(200L);
        allocRequest.setAllocQuantity(10);
        allocRequest.setAllocPrice(100.0);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThrows(IllegalStateException.class,
                () -> orderService.allocateOrder(1L, allocRequest));
    }

    @Test
    void testAllocateOrder_OverAllocate_ThrowsException() {
        sampleOrder.setStatus(OrderStatus.FILLED);
        sampleOrder.setQuantity(50);

        // 30 units filled, 20 already allocated → only 10 remain
        ExecutionFill fill = ExecutionFill.builder()
                .fillId(1L).order(sampleOrder).fillQuantity(30).status(FillStatus.COMPLETED).build();

        Allocation existingAlloc = Allocation.builder()
                .allocationId(1L).order(sampleOrder).accountId(100L).allocQuantity(20).build();

        AllocationRequestDTO allocRequest = new AllocationRequestDTO();
        allocRequest.setAccountId(200L);
        allocRequest.setAllocQuantity(15); // requesting 15 but only 10 remain
        allocRequest.setAllocPrice(100.0);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(executionFillRepository.findByOrder_OrderId(1L)).thenReturn(List.of(fill));
        when(allocationRepository.findByOrder_OrderId(1L)).thenReturn(List.of(existingAlloc));

        assertThrows(IllegalStateException.class,
                () -> orderService.allocateOrder(1L, allocRequest));
    }

    @Test
    void testAllocateOrder_Success() {
        sampleOrder.setStatus(OrderStatus.FILLED);

        ExecutionFill fill = ExecutionFill.builder()
                .fillId(1L).order(sampleOrder).fillQuantity(50).status(FillStatus.COMPLETED).build();

        Allocation savedAlloc = Allocation.builder()
                .allocationId(1L).order(sampleOrder).accountId(200L)
                .allocQuantity(50).allocPrice(100.0).allocDate(LocalDateTime.now()).build();

        AllocationRequestDTO allocRequest = new AllocationRequestDTO();
        allocRequest.setAccountId(200L);
        allocRequest.setAllocQuantity(50);
        allocRequest.setAllocPrice(100.0);

        AllocationResponseDTO allocResponse = new AllocationResponseDTO();
        allocResponse.setAllocQuantity(50);

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setBaseCurrency("INR");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(executionFillRepository.findByOrder_OrderId(1L)).thenReturn(List.of(fill));
        when(allocationRepository.findByOrder_OrderId(1L)).thenReturn(List.of());
        when(allocationRepository.save(any(Allocation.class))).thenReturn(savedAlloc);
        when(pborFeignClient.getAccountById(200L)).thenReturn(accountDTO);
        doNothing().when(pborFeignClient).createHolding(any(HoldingRequestDTO.class));
        doNothing().when(pborFeignClient).createCashLedgerEntry(any(CashLedgerRequestDTO.class));
        when(modelMapper.map(any(Allocation.class), eq(AllocationResponseDTO.class))).thenReturn(allocResponse);

        AllocationResponseDTO result = orderService.allocateOrder(1L, allocRequest);

        assertNotNull(result);
        assertEquals(50, result.getAllocQuantity());
    }

    // ─── cancelOrder — valid statuses ─────────────────────────────────────────

    @Test
    void testCancelOrder_ValidatedOrder_CancelledSuccessfully() {
        sampleOrder.setStatus(OrderStatus.VALIDATED);

        OrderResponseDTO cancelledResponse = new OrderResponseDTO();
        cancelledResponse.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(cancelledResponse);

        OrderResponseDTO result = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelOrder_RoutedOrder_CancelledSuccessfully() {
        sampleOrder.setStatus(OrderStatus.ROUTED);

        OrderResponseDTO cancelledResponse = new OrderResponseDTO();
        cancelledResponse.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(cancelledResponse);

        OrderResponseDTO result = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelOrder_PartiallyFilledOrder_ThrowsException() {
        sampleOrder.setStatus(OrderStatus.PARTIALLY_FILLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L));
    }

    // ─── getOrderLifecycle ────────────────────────────────────────────────────

    @Test
    void testGetOrderLifecycle_ReturnsFullLifecycle() {
        PreTradeCheck check = PreTradeCheck.builder()
                .checkId(1L).order(sampleOrder).checkType(CheckType.LIMIT)
                .result(CheckResult.PASS).build();

        ExecutionFill fill = ExecutionFill.builder()
                .fillId(1L).order(sampleOrder).fillQuantity(50)
                .status(FillStatus.COMPLETED).fillDate(LocalDateTime.now()).build();

        Allocation alloc = Allocation.builder()
                .allocationId(1L).order(sampleOrder)
                .accountId(200L).allocQuantity(50)
                .allocDate(LocalDateTime.now()).build();

        OrderResponseDTO orderDTO = new OrderResponseDTO();
        orderDTO.setOrderId(1L);

        PreTradeCheckResponseDTO checkDTO = new PreTradeCheckResponseDTO();
        ExecutionFillResponseDTO fillDTO = new ExecutionFillResponseDTO();
        AllocationResponseDTO allocDTO = new AllocationResponseDTO();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(preTradeCheckRepository.findByOrder_OrderId(1L)).thenReturn(List.of(check));
        when(executionFillRepository.findByOrder_OrderId(1L)).thenReturn(List.of(fill));
        when(allocationRepository.findByOrder_OrderId(1L)).thenReturn(List.of(alloc));
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(orderDTO);
        when(modelMapper.map(any(PreTradeCheck.class), eq(PreTradeCheckResponseDTO.class))).thenReturn(checkDTO);
        when(modelMapper.map(any(ExecutionFill.class), eq(ExecutionFillResponseDTO.class))).thenReturn(fillDTO);
        when(modelMapper.map(any(Allocation.class), eq(AllocationResponseDTO.class))).thenReturn(allocDTO);

        OrderLifecycleDTO result = orderService.getOrderLifecycle(1L);

        assertNotNull(result);
        assertEquals(1, result.getPreTradeChecks().size());
        assertEquals(1, result.getExecutionFills().size());
        assertEquals(1, result.getAllocations().size());
    }

    @Test
    void testGetOrderLifecycle_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderLifecycle(999L));
    }

    // ─── createOrder — feign failure cases ───────────────────────────────────

    @Test
    void testCreateOrder_ClientNotFound_ThrowsException() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setClientId(999L);
        request.setSecurityId(100L);
        request.setSide(Side.BUY);
        request.setQuantity(10);
        request.setPriceType(PriceType.MARKET);

        // override the lenient stub to throw
        when(wealthproFeignClient.getClientById(999L))
                .thenThrow(FeignException.NotFound.class);

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
    }

    @Test
    void testCreateOrder_SecurityNotFound_ThrowsException() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setClientId(10L);
        request.setSecurityId(999L);
        request.setSide(Side.BUY);
        request.setQuantity(10);
        request.setPriceType(PriceType.MARKET);

        when(wealthproFeignClient.getClientById(10L)).thenReturn(new ClientDTO());
        when(productCatalogFeignClient.getSecurityById(999L))
                .thenThrow(FeignException.NotFound.class);

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
    }
}
