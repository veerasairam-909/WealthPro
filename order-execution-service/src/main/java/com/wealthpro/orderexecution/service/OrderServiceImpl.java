package com.wealthpro.orderexecution.service;

import com.wealthpro.orderexecution.dto.*;
import com.wealthpro.orderexecution.entities.*;
import com.wealthpro.orderexecution.enums.*;
import com.wealthpro.orderexecution.exception.ResourceNotFoundException;
import com.wealthpro.orderexecution.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link OrderService} containing the core business logic
 * for the complete order lifecycle — from placement through pre-trade
 * validation, routing, execution, allocation, and cancellation.
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PreTradeCheckRepository preTradeCheckRepository;
    private final ExecutionFillRepository executionFillRepository;
    private final AllocationRepository allocationRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            PreTradeCheckRepository preTradeCheckRepository,
                            ExecutionFillRepository executionFillRepository,
                            AllocationRepository allocationRepository,
                            ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.preTradeCheckRepository = preTradeCheckRepository;
        this.executionFillRepository = executionFillRepository;
        this.allocationRepository = allocationRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        Order order = modelMapper.map(requestDTO, Order.class);
        order.setStatus(OrderStatus.PLACED);
        order.setOrderDate(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        return modelMapper.map(savedOrder, OrderResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "#orderId")
    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        return modelMapper.map(order, OrderResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        // Convert each Order entity to OrderResponseDTO using stream + method reference
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByClientId(Long clientId) {
        return orderRepository.findByClientId(clientId).stream()
                .map(order -> modelMapper.map(order, OrderResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(order -> modelMapper.map(order, OrderResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @CachePut(value = "orders", key = "#orderId")
    public OrderResponseDTO updateOrder(Long orderId, OrderRequestDTO requestDTO) {
        Order existingOrder = findOrderOrThrow(orderId);

        existingOrder.setClientId(requestDTO.getClientId());
        existingOrder.setSecurityId(requestDTO.getSecurityId());
        existingOrder.setSide(requestDTO.getSide());
        existingOrder.setQuantity(requestDTO.getQuantity());
        existingOrder.setPriceType(requestDTO.getPriceType());
        existingOrder.setLimitPrice(requestDTO.getLimitPrice());

        Order updatedOrder = orderRepository.save(existingOrder);
        return modelMapper.map(updatedOrder, OrderResponseDTO.class);
    }

    @Override
    @CacheEvict(value = "orders", key = "#orderId")
    public void deleteOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        orderRepository.delete(order);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Runs all four simulated pre-trade checks (SUITABILITY, LIMIT, EXPOSURE, CASH).
     * If every check passes, the order status is set to VALIDATED.
     * If any check fails, the order status is set to REJECTED.
     * </p>
     */
    @Override
    @CacheEvict(value = "orders", key = "#orderId")
    public List<PreTradeCheckResponseDTO> runAllPreTradeChecks(Long orderId) {
        Order order = findOrderOrThrow(orderId);

        // Pre-trade checks can only run on PLACED orders
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException(
                    "Pre-trade checks can only run on PLACED orders. Current status: " + order.getStatus());
        }

        // Run each of the four check types using stream + map
        List<PreTradeCheck> checkResults = Arrays.stream(CheckType.values())
                .map(checkType -> simulateSingleCheck(checkType, order))
                .collect(Collectors.toList());

        // Persist all check results
        List<PreTradeCheck> savedChecks = preTradeCheckRepository.saveAll(checkResults);

        // Determine if ALL checks passed using stream allMatch
        boolean allPassed = savedChecks.stream()
                .allMatch(check -> check.getResult() == CheckResult.PASS);

        // Update order status based on check results
        order.setStatus(allPassed ? OrderStatus.VALIDATED : OrderStatus.REJECTED);
        orderRepository.save(order);

        // Convert saved check entities to response DTOs
        return savedChecks.stream()
                .map(this::mapCheckToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>Simulates routing by assigning a dummy venue name.</p>
     */
    @Override
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderResponseDTO routeOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.VALIDATED) {
            throw new IllegalStateException(
                    "Only VALIDATED orders can be routed. Current status: " + order.getStatus());
        }

        // Simulate venue assignment based on price type
        String venue = switch (order.getPriceType()) {
            case MARKET -> "SIMULATED_NSE";
            case LIMIT -> "SIMULATED_BSE";
            case NAV -> "SIMULATED_MF_PLATFORM";
        };

        order.setRoutedVenue(venue);
        order.setStatus(OrderStatus.ROUTED);
        Order updatedOrder = orderRepository.save(order);

        return modelMapper.map(updatedOrder, OrderResponseDTO.class);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Creates a fill and calculates the cumulative filled quantity to determine
     * whether the order should move to PARTIALLY_FILLED or FILLED.
     * </p>
     */
    @Override
    @CacheEvict(value = "orders", key = "#orderId")
    public ExecutionFillResponseDTO executeFill(Long orderId, ExecutionFillRequestDTO requestDTO) {
        Order order = findOrderOrThrow(orderId);

        // Fills can only be created for ROUTED or PARTIALLY_FILLED orders
        Set<OrderStatus> fillableStatuses = Set.of(OrderStatus.ROUTED, OrderStatus.PARTIALLY_FILLED);
        if (!fillableStatuses.contains(order.getStatus())) {
            throw new IllegalStateException(
                    "Fills can only be applied to ROUTED or PARTIALLY_FILLED orders. Current status: "
                            + order.getStatus());
        }

        // Build the fill entity
        ExecutionFill fill = ExecutionFill.builder()
                .order(order)
                .fillQuantity(requestDTO.getFillQuantity())
                .fillPrice(requestDTO.getFillPrice())
                .venue(requestDTO.getVenue() != null ? requestDTO.getVenue() : order.getRoutedVenue())
                .fillDate(LocalDateTime.now())
                .status(FillStatus.COMPLETED)
                .build();

        ExecutionFill savedFill = executionFillRepository.save(fill);

        // Calculate total filled quantity across all COMPLETED fills using stream
        // mapToInt extracts the integer fill quantity, sum() totals them
        int totalFilled = executionFillRepository.findByOrder_OrderId(orderId).stream()
                .filter(f -> f.getStatus() == FillStatus.COMPLETED)
                .mapToInt(ExecutionFill::getFillQuantity)
                .sum();

        // Update order status based on fill progress
        if (totalFilled >= order.getQuantity()) {
            order.setStatus(OrderStatus.FILLED);
        } else {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        }
        orderRepository.save(order);

        return mapFillToResponseDTO(savedFill);
    }

    /**
     * {@inheritDoc}
     * <p>Only FILLED orders can have allocations created.</p>
     */
    @Override
    public AllocationResponseDTO allocateOrder(Long orderId, AllocationRequestDTO requestDTO) {
        Order order = findOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.FILLED) {
            throw new IllegalStateException(
                    "Allocations can only be created for FILLED orders. Current status: " + order.getStatus());
        }

        Allocation allocation = Allocation.builder()
                .order(order)
                .accountId(requestDTO.getAccountId())
                .allocQuantity(requestDTO.getAllocQuantity())
                .allocPrice(requestDTO.getAllocPrice())
                .allocDate(LocalDateTime.now())
                .build();

        Allocation savedAllocation = allocationRepository.save(allocation);
        return mapAllocationToResponseDTO(savedAllocation);
    }

    /**
     * {@inheritDoc}
     * <p>Cancellation is only allowed for PLACED, VALIDATED, or ROUTED orders.</p>
     */
    @Override
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderResponseDTO cancelOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);

        // Define which statuses allow cancellation
        Set<OrderStatus> cancellableStatuses = Set.of(
                OrderStatus.PLACED, OrderStatus.VALIDATED, OrderStatus.ROUTED);

        if (!cancellableStatuses.contains(order.getStatus())) {
            throw new IllegalStateException(
                    "Cannot cancel order in " + order.getStatus() + " status. "
                            + "Only PLACED, VALIDATED, or ROUTED orders can be cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        return modelMapper.map(updatedOrder, OrderResponseDTO.class);
    }

    /**
     * {@inheritDoc}
     * <p>Assembles the full lifecycle view by querying all related entities.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public OrderLifecycleDTO getOrderLifecycle(Long orderId) {
        Order order = findOrderOrThrow(orderId);

        OrderLifecycleDTO lifecycle = new OrderLifecycleDTO();
        lifecycle.setOrder(modelMapper.map(order, OrderResponseDTO.class));

        // Fetch and map all pre-trade checks for this order
        lifecycle.setPreTradeChecks(
                preTradeCheckRepository.findByOrder_OrderId(orderId).stream()
                        .map(this::mapCheckToResponseDTO)
                        .collect(Collectors.toList()));

        // Fetch and map all fills for this order
        lifecycle.setExecutionFills(
                executionFillRepository.findByOrder_OrderId(orderId).stream()
                        .map(this::mapFillToResponseDTO)
                        .collect(Collectors.toList()));

        // Fetch and map all allocations for this order
        lifecycle.setAllocations(
                allocationRepository.findByOrder_OrderId(orderId).stream()
                        .map(this::mapAllocationToResponseDTO)
                        .collect(Collectors.toList()));

        return lifecycle;
    }


    /**
     * Finds an order by ID or throws {@link ResourceNotFoundException}.
     */
    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    /**
     * Simulates a single pre-trade compliance check with dummy business rules.
     * <ul>
     *   <li>SUITABILITY: always passes (simulated)</li>
     *   <li>LIMIT: fails if quantity exceeds 10,000 units</li>
     *   <li>EXPOSURE: always passes (simulated)</li>
     *   <li>CASH: fails if limit price exceeds 50,000</li>
     * </ul>
     */
    private PreTradeCheck simulateSingleCheck(CheckType checkType, Order order) {
        CheckResult result;
        String message;

        switch (checkType) {
            case SUITABILITY -> {
                result = CheckResult.PASS;
                message = "Suitability check passed — client profile is compatible";
            }
            case LIMIT -> {
                // Dummy rule: fail if quantity > 10,000
                boolean passed = order.getQuantity() <= 10000;
                result = passed ? CheckResult.PASS : CheckResult.FAIL;
                message = passed
                        ? "Limit check passed — quantity within threshold"
                        : "Limit check FAILED — quantity " + order.getQuantity() + " exceeds 10,000 limit";
            }
            case EXPOSURE -> {
                result = CheckResult.PASS;
                message = "Exposure check passed — within portfolio exposure limits";
            }
            case CASH -> {
                // Dummy rule: fail if limitPrice > 50,000
                boolean passed = order.getLimitPrice() == null || order.getLimitPrice() <= 50000;
                result = passed ? CheckResult.PASS : CheckResult.FAIL;
                message = passed
                        ? "Cash check passed — sufficient cash balance"
                        : "Cash check FAILED — limit price " + order.getLimitPrice() + " exceeds 50,000 cap";
            }
            default -> {
                result = CheckResult.PASS;
                message = "Default check passed";
            }
        }

        return PreTradeCheck.builder()
                .order(order)
                .checkType(checkType)
                .result(result)
                .message(message)
                .checkedDate(LocalDateTime.now())
                .build();
    }

    /** Maps a PreTradeCheck entity to its response DTO, setting the orderId. */
    private PreTradeCheckResponseDTO mapCheckToResponseDTO(PreTradeCheck check) {
        PreTradeCheckResponseDTO dto = modelMapper.map(check, PreTradeCheckResponseDTO.class);
        dto.setOrderId(check.getOrder().getOrderId());
        return dto;
    }

    /** Maps an ExecutionFill entity to its response DTO, setting the orderId. */
    private ExecutionFillResponseDTO mapFillToResponseDTO(ExecutionFill fill) {
        ExecutionFillResponseDTO dto = modelMapper.map(fill, ExecutionFillResponseDTO.class);
        dto.setOrderId(fill.getOrder().getOrderId());
        return dto;
    }

    /** Maps an Allocation entity to its response DTO, setting the orderId. */
    private AllocationResponseDTO mapAllocationToResponseDTO(Allocation allocation) {
        AllocationResponseDTO dto = modelMapper.map(allocation, AllocationResponseDTO.class);
        dto.setOrderId(allocation.getOrder().getOrderId());
        return dto;
    }
}
