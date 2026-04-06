package com.wealthpro.orderexecution.repository;

import com.wealthpro.orderexecution.entities.Allocation;
import com.wealthpro.orderexecution.entities.Order;
import com.wealthpro.orderexecution.enums.OrderStatus;
import com.wealthpro.orderexecution.enums.PriceType;
import com.wealthpro.orderexecution.enums.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AllocationRepository} covering CRUD and custom finder methods.
 *
 * @author WealthPro Team
 */
@DataJpaTest
@ActiveProfiles("test")
class AllocationRepositoryTest {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;
    private Allocation allocation1;
    private Allocation allocation2;
    private Allocation allocation3;

    @BeforeEach
    void setUp() {
        allocationRepository.deleteAll();
        orderRepository.deleteAll();

        order1 = orderRepository.save(Order.builder()
                .clientId(10L).securityId(100L).side(Side.BUY)
                .quantity(100).priceType(PriceType.MARKET)
                .status(OrderStatus.FILLED).orderDate(LocalDateTime.now())
                .build());

        order2 = orderRepository.save(Order.builder()
                .clientId(20L).securityId(200L).side(Side.SELL)
                .quantity(50).priceType(PriceType.LIMIT).limitPrice(155.0)
                .status(OrderStatus.FILLED).orderDate(LocalDateTime.now())
                .build());

        allocation1 = Allocation.builder()
                .order(order1).accountId(1001L).allocQuantity(60)
                .allocPrice(150.0).allocDate(LocalDateTime.now())
                .build();

        allocation2 = Allocation.builder()
                .order(order1).accountId(1002L).allocQuantity(40)
                .allocPrice(150.0).allocDate(LocalDateTime.now())
                .build();

        allocation3 = Allocation.builder()
                .order(order2).accountId(1001L).allocQuantity(50)
                .allocPrice(155.0).allocDate(LocalDateTime.now())
                .build();

        allocationRepository.saveAll(List.of(allocation1, allocation2, allocation3));
    }

    @Test
    void testSave_positive() {
        Allocation newAllocation = Allocation.builder()
                .order(order2).accountId(1003L).allocQuantity(25)
                .allocPrice(155.0).allocDate(LocalDateTime.now())
                .build();

        Allocation saved = allocationRepository.save(newAllocation);

        assertNotNull(saved.getAllocationId());
        assertEquals(1003L, saved.getAccountId());
    }

    @Test
    void testSave_nullRequiredField_exception() {
        Allocation invalidAllocation = Allocation.builder()
                .order(order1).accountId(null)  // nullable = false
                .allocQuantity(10).allocPrice(100.0)
                .allocDate(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            allocationRepository.saveAndFlush(invalidAllocation);
        });
    }

    // ==================== findById() ====================

    @Test
    void testFindById_positive() {
        Optional<Allocation> found = allocationRepository.findById(allocation1.getAllocationId());

        assertTrue(found.isPresent());
        assertEquals(1001L, found.get().getAccountId());
    }

    @Test
    void testFindById_notFound_negative() {
        Optional<Allocation> found = allocationRepository.findById(9999L);

        assertFalse(found.isPresent());
    }

    // ==================== findByOrder_OrderId() ====================

    @Test
    void testFindByOrder_OrderId_positive() {
        List<Allocation> allocations = allocationRepository.findByOrder_OrderId(order1.getOrderId());

        assertEquals(2, allocations.size());
    }

    @Test
    void testFindByOrder_OrderId_noMatch_negative() {
        List<Allocation> allocations = allocationRepository.findByOrder_OrderId(9999L);

        assertTrue(allocations.isEmpty());
    }

    // ==================== findByAccountId() ====================

    @Test
    void testFindByAccountId_positive() {
        List<Allocation> allocations = allocationRepository.findByAccountId(1001L);

        assertEquals(2, allocations.size());
        assertTrue(allocations.stream().allMatch(a -> a.getAccountId().equals(1001L)));
    }

    @Test
    void testFindByAccountId_noMatch_negative() {
        List<Allocation> allocations = allocationRepository.findByAccountId(9999L);

        assertTrue(allocations.isEmpty());
    }

    // ==================== deleteById() ====================

    @Test
    void testDeleteById_positive() {
        Long id = allocation1.getAllocationId();
        allocationRepository.deleteById(id);

        Optional<Allocation> found = allocationRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ==================== findAll() ====================

    @Test
    void testFindAll_positive() {
        List<Allocation> allocations = allocationRepository.findAll();

        assertEquals(3, allocations.size());
    }

    @Test
    void testFindAll_emptyTable_negative() {
        allocationRepository.deleteAll();

        List<Allocation> allocations = allocationRepository.findAll();

        assertTrue(allocations.isEmpty());
    }
}
