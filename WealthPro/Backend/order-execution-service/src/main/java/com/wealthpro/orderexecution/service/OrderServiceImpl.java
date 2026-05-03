package com.wealthpro.orderexecution.service;

import com.wealthpro.orderexecution.dto.*;
import com.wealthpro.orderexecution.entities.*;
import com.wealthpro.orderexecution.enums.*;
import com.wealthpro.orderexecution.exception.ResourceNotFoundException;
import com.wealthpro.orderexecution.feign.NotificationFeignClient;
import com.wealthpro.orderexecution.feign.PborFeignClient;
import com.wealthpro.orderexecution.feign.ProductCatalogFeignClient;
import com.wealthpro.orderexecution.feign.WealthproFeignClient;
import com.wealthpro.orderexecution.feign.dto.CashLedgerRequestDTO;
import com.wealthpro.orderexecution.feign.dto.ClientDTO;
import com.wealthpro.orderexecution.feign.dto.HoldingRequestDTO;
import com.wealthpro.orderexecution.feign.dto.NotificationRequestDTO;
import com.wealthpro.orderexecution.repository.*;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PreTradeCheckRepository preTradeCheckRepository;
    private final ExecutionFillRepository executionFillRepository;
    private final AllocationRepository allocationRepository;
    private final ModelMapper modelMapper;
    private final WealthproFeignClient wealthproFeignClient;
    private final ProductCatalogFeignClient productCatalogFeignClient;
    private final PborFeignClient pborFeignClient;
    private final NotificationFeignClient notificationFeignClient;
    private final SuitabilityRuleEvaluator suitabilityRuleEvaluator;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            PreTradeCheckRepository preTradeCheckRepository,
                            ExecutionFillRepository executionFillRepository,
                            AllocationRepository allocationRepository,
                            ModelMapper modelMapper,
                            WealthproFeignClient wealthproFeignClient,
                            ProductCatalogFeignClient productCatalogFeignClient,
                            PborFeignClient pborFeignClient,
                            NotificationFeignClient notificationFeignClient,
                            SuitabilityRuleEvaluator suitabilityRuleEvaluator) {
        this.orderRepository = orderRepository;
        this.preTradeCheckRepository = preTradeCheckRepository;
        this.executionFillRepository = executionFillRepository;
        this.allocationRepository = allocationRepository;
        this.modelMapper = modelMapper;
        this.wealthproFeignClient = wealthproFeignClient;
        this.productCatalogFeignClient = productCatalogFeignClient;
        this.pborFeignClient = pborFeignClient;
        this.notificationFeignClient = notificationFeignClient;
        this.suitabilityRuleEvaluator = suitabilityRuleEvaluator;
    }


    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {

        // ── Feign: Validate client exists in Wealthpro service ──────────────
        try {
            var client = wealthproFeignClient.getClientById(requestDTO.getClientId());
            log.info("[FEIGN] Client validated from WEALTHPRO-SERVICE → id={}, name={}",
                    client.getClientId(), client.getName());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Client", requestDTO.getClientId());
        }

        // ── Feign: Validate security exists in ProductCatalog service ────────
        try {
            var security = productCatalogFeignClient.getSecurityById(requestDTO.getSecurityId());
            log.info("[FEIGN] Security validated from PRODUCTCATALOG-SERVICE → id={}, symbol={}, status={}",
                    security.getSecurityId(), security.getSymbol(), security.getStatus());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Security", requestDTO.getSecurityId());
        }
        // ────────────────────────────────────────────────────────────────────

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

        // Notify client about pre-trade check result
        sendOrderNotification(order, allPassed
                ? "Order #" + orderId + " passed all pre-trade checks and is VALIDATED"
                : "Order #" + orderId + " REJECTED — one or more pre-trade checks failed");

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
            sendOrderNotification(order, "Order #" + orderId + " is fully FILLED (" + totalFilled + "/" + order.getQuantity() + " units)");
        } else {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
            sendOrderNotification(order, "Order #" + orderId + " PARTIALLY_FILLED (" + totalFilled + "/" + order.getQuantity() + " units)");
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

        // Make sure we are not allocating more than what was actually filled
        int totalFilled = executionFillRepository.findByOrder_OrderId(orderId)
                .stream()
                .mapToInt(ExecutionFill::getFillQuantity)
                .sum();
        if (requestDTO.getAllocQuantity() > totalFilled) {
            throw new IllegalStateException(
                    "Cannot allocate " + requestDTO.getAllocQuantity()
                    + " units — only " + totalFilled + " units were filled.");
        }

        Allocation allocation = Allocation.builder()
                .order(order)
                .accountId(requestDTO.getAccountId())
                .allocQuantity(requestDTO.getAllocQuantity())
                .allocPrice(requestDTO.getAllocPrice())
                .allocDate(LocalDateTime.now())
                .build();

        Allocation savedAllocation = allocationRepository.save(allocation);

        // ── PBOR Update: Create holding and cash ledger entry ──────────────
        try {
            updatePborAfterAllocation(order, requestDTO);
        } catch (Exception e) {
            log.error("[PBOR] Failed to update PBOR after allocation for orderId={}, accountId={}: {}",
                    orderId, requestDTO.getAccountId(), e.getMessage());
        }
        // ────────────────────────────────────────────────────────────────────

        return mapAllocationToResponseDTO(savedAllocation);
    }

    /**
     * Updates PBOR service after a successful allocation by creating a holding
     * entry and a cash ledger entry via Feign.
     * <p>
     * BUY/SUBSCRIBE → creates holding + SUBSCRIPTION cash debit<br>
     * SELL/REDEEM → creates holding (negative not supported, so skip) + REDEMPTION cash credit
     * </p>
     */
    private void updatePborAfterAllocation(Order order, AllocationRequestDTO allocationDTO) {
        Long accountId = allocationDTO.getAccountId();
        BigDecimal quantity = BigDecimal.valueOf(allocationDTO.getAllocQuantity());
        BigDecimal price = BigDecimal.valueOf(allocationDTO.getAllocPrice());
        BigDecimal amount = quantity.multiply(price);

        // Determine txn type based on order side
        String txnType;
        switch (order.getSide()) {
            case BUY, SUBSCRIBE -> txnType = "SUBSCRIPTION";
            case SELL, REDEEM -> txnType = "REDEMPTION";
            default -> txnType = "SUBSCRIPTION";
        }

        // Fetch account to get its base currency
        String currency;
        try {
            var account = pborFeignClient.getAccountById(accountId);
            currency = (account.getBaseCurrency() != null) ? account.getBaseCurrency() : "INR";
        } catch (Exception e) {
            log.warn("[PBOR] Could not fetch account {}; defaulting currency to INR", accountId);
            currency = "INR";
        }

        // Create holding entry for BUY/SUBSCRIBE orders
        if (order.getSide() == Side.BUY || order.getSide() == Side.SUBSCRIBE) {
            HoldingRequestDTO holdingRequest = new HoldingRequestDTO(
                    accountId,
                    order.getSecurityId(),
                    quantity,
                    price,
                    currency,
                    LocalDate.now()
            );
            pborFeignClient.createHolding(holdingRequest);
            log.info("[PBOR] Holding created for accountId={}, securityId={}, qty={}",
                    accountId, order.getSecurityId(), quantity);
        }

        // Create cash ledger entry
        CashLedgerRequestDTO cashRequest = new CashLedgerRequestDTO(
                accountId,
                txnType,
                order.getSecurityId(),
                quantity,
                price,
                amount,
                currency,
                LocalDate.now(),
                "Order #" + order.getOrderId() + " " + order.getSide() + " allocation"
        );
        pborFeignClient.createCashLedgerEntry(cashRequest);
        log.info("[PBOR] Cash ledger entry created for accountId={}, txnType={}, amount={}",
                accountId, txnType, amount);
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

        sendOrderNotification(order, "Order #" + orderId + " has been CANCELLED");

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
     * Runs a single pre-trade compliance check using real Feign calls where possible.
     * <ul>
     *   <li>SUITABILITY: checks client risk profile via Wealthpro-service</li>
     *   <li>LIMIT: fails if quantity exceeds 10,000 units</li>
     *   <li>EXPOSURE: checks holdings exposure via PBOR-service</li>
     *   <li>CASH: checks account cash balance via PBOR-service against order value</li>
     * </ul>
     */
    private PreTradeCheck simulateSingleCheck(CheckType checkType, Order order) {
        CheckResult result;
        String message;

        switch (checkType) {
            case SUITABILITY -> {
                try {
                    // Fetch risk profile and security details needed for rule evaluation
                    var riskProfile = wealthproFeignClient.getRiskProfileByClientId(order.getClientId());
                    var security    = productCatalogFeignClient.getSecurityById(order.getSecurityId());

                    // Fetch client for segment / status context variables
                    ClientDTO client = null;
                    try {
                        client = wealthproFeignClient.getClientById(order.getClientId());
                    } catch (FeignException ignored) {
                        log.warn("[SUITABILITY] Could not fetch client {} for context", order.getClientId());
                    }

                    // Build evaluation context from order + client + security data
                    Map<String, String> ctx = new HashMap<>();
                    ctx.put("riskClass",  riskProfile != null && riskProfile.getRiskClass() != null
                            ? riskProfile.getRiskClass().toUpperCase() : "");
                    ctx.put("assetClass", security != null && security.getAssetClass() != null
                            ? security.getAssetClass().toUpperCase() : "");
                    ctx.put("side",       order.getSide() != null
                            ? order.getSide().name().toUpperCase() : "");
                    ctx.put("priceType",  order.getPriceType() != null
                            ? order.getPriceType().name().toUpperCase() : "");
                    ctx.put("quantity",   String.valueOf(order.getQuantity()));
                    ctx.put("segment",    client != null && client.getSegment() != null
                            ? client.getSegment() : "");
                    ctx.put("status",     client != null && client.getStatus() != null
                            ? client.getStatus() : "");
                    ctx.put("currency",   security != null && security.getCurrency() != null
                            ? security.getCurrency().toUpperCase() : "");
                    // orderValue = quantity × price  (use limitPrice when set; fall back to current market price)
                    double unitPrice = order.getLimitPrice() != null ? order.getLimitPrice()
                            : (security != null && security.getCurrentPrice() != null
                                    ? security.getCurrentPrice().doubleValue() : 0.0);
                    ctx.put("orderValue", String.valueOf(order.getQuantity() * unitPrice));

                    // Fetch and evaluate all active suitability rules
                    var allRules = wealthproFeignClient.getAllSuitabilityRules();
                    var activeRules = allRules.stream()
                            .filter(r -> "ACTIVE".equalsIgnoreCase(r.getStatus()))
                            .toList();

                    String violatedRule = null;
                    for (var rule : activeRules) {
                        if (suitabilityRuleEvaluator.evaluate(rule, ctx)) {
                            violatedRule = rule.getDescription();
                            log.info("[SUITABILITY] Rule '{}' triggered for order {}", rule.getDescription(), order.getOrderId());
                            break;
                        }
                    }

                    if (violatedRule != null) {
                        result = CheckResult.FAIL;
                        message = "Suitability check FAILED — rule violated: " + violatedRule;
                    } else {
                        result = CheckResult.PASS;
                        message = "Suitability check passed — " + activeRules.size() + " rule(s) evaluated, none triggered";
                    }
                } catch (FeignException e) {
                    log.warn("[FEIGN] Could not fetch data for suitability check on order {}: {}", order.getOrderId(), e.getMessage());
                    result = CheckResult.FAIL;
                    message = "Suitability check FAILED — rule data unavailable, order held for safety";
                }
            }
            case LIMIT -> {
                boolean passed = order.getQuantity() <= 10000;
                result = passed ? CheckResult.PASS : CheckResult.FAIL;
                message = passed
                        ? "Limit check passed — quantity within 10,000 threshold"
                        : "Limit check FAILED — quantity " + order.getQuantity() + " exceeds 10,000 limit";
            }
            case EXPOSURE -> {
                try {
                    // Check if any single holding exceeds 30% of total portfolio via PBOR
                    var accounts = pborFeignClient.getAccountsByClientId(order.getClientId());
                    if (!accounts.isEmpty()) {
                        var holdings = pborFeignClient.getHoldingsByAccountId(accounts.get(0).getAccountId());
                        boolean hasConcentration = holdings.stream()
                                .anyMatch(h -> h.getSecurityId().equals(order.getSecurityId())
                                        && h.getQuantity() != null
                                        && h.getQuantity().intValue() + order.getQuantity() > 10000);
                        result = hasConcentration ? CheckResult.FAIL : CheckResult.PASS;
                        message = hasConcentration
                                ? "Exposure check FAILED — combined holding would exceed concentration limit"
                                : "Exposure check passed — within portfolio exposure limits";
                    } else {
                        result = CheckResult.PASS;
                        message = "Exposure check passed — no existing accounts found";
                    }
                } catch (FeignException e) {
                    log.warn("[FEIGN] Could not fetch holdings for exposure check: {}", e.getMessage());
                    result = CheckResult.PASS;
                    message = "Exposure check passed — holdings data unavailable, defaulting to pass";
                }
            }
            case CASH -> {
                try {
                    var accounts = pborFeignClient.getAccountsByClientId(order.getClientId());
                    if (!accounts.isEmpty()) {
                        BigDecimal cashBalance = pborFeignClient.getCashBalanceByAccountId(
                                accounts.get(0).getAccountId());
                        double orderValue = order.getQuantity() *
                                (order.getLimitPrice() != null ? order.getLimitPrice() : 0.0);
                        boolean passed = cashBalance != null
                                && cashBalance.doubleValue() >= orderValue;
                        result = passed ? CheckResult.PASS : CheckResult.FAIL;
                        message = passed
                                ? "Cash check passed — balance " + cashBalance + " covers order value " + orderValue
                                : "Cash check FAILED — balance " + cashBalance + " insufficient for order value " + orderValue;
                    } else {
                        result = CheckResult.FAIL;
                        message = "Cash check FAILED — no account found for client " + order.getClientId();
                    }
                } catch (FeignException e) {
                    log.warn("[FEIGN] Could not fetch cash balance: {}", e.getMessage());
                    boolean passed = order.getLimitPrice() == null || order.getLimitPrice() <= 50000;
                    result = passed ? CheckResult.PASS : CheckResult.FAIL;
                    message = passed
                            ? "Cash check passed — PBOR unavailable, fallback rule applied"
                            : "Cash check FAILED — PBOR unavailable, limit price " + order.getLimitPrice() + " exceeds fallback cap";
                }
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

    /**
     * Sends an order status change notification to the client via the Notifications service.
     */
    private void sendOrderNotification(Order order, String message) {
        try {
            notificationFeignClient.sendNotification(
                    new NotificationRequestDTO(order.getClientId(), message, "Order"));
            log.info("[NOTIFICATION] Sent: {}", message);
        } catch (Exception e) {
            log.warn("[NOTIFICATION] Failed to send notification for order {}: {}",
                    order.getOrderId(), e.getMessage());
        }
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
