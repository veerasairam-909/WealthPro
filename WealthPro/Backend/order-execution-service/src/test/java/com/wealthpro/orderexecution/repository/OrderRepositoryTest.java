package com.wealthpro.orderexecution.repository;

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
 * Unit tests for {@link OrderRepository} covering CRUD and custom finder methods.
 *
 * @author WealthPro Team
 */
@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;
    private Order order3;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        order1 = Order.builder()
                .clientId(10L).securityId(100L).side(Side.BUY)
                .quantity(50).priceType(PriceType.MARKET)
                .status(OrderStatus.PLACED).orderDate(LocalDateTime.now())
                .build();

        order2 = Order.builder()
                .clientId(10L).securityId(200L).side(Side.SELL)
                .quantity(30).priceType(PriceType.LIMIT).limitPrice(150.0)
                .status(OrderStatus.FILLED).orderDate(LocalDateTime.now())
                .build();

        order3 = Order.builder()
                .clientId(20L).securityId(100L).side(Side.BUY)
                .quantity(100).priceType(PriceType.MARKET)
                .status(OrderStatus.PLACED).orderDate(LocalDateTime.now())
                .build();

        orderRepository.saveAll(List.of(order1, order2, order3));
    }

    // ==================== save() ====================

    @Test
    void testSave_positive() {
        Order newOrder = Order.builder()
                .clientId(30L).securityId(300L).side(Side.SUBSCRIBE)
                .quantity(10).priceType(PriceType.NAV)
                .status(OrderStatus.PLACED).orderDate(LocalDateTime.now())
                .build();

        Order saved = orderRepository.save(newOrder);

        assertNotNull(saved.getOrderId());
        assertEquals(30L, saved.getClientId());
        assertEquals(OrderStatus.PLACED, saved.getStatus());
    }

    @Test
    void testSave_nullRequiredField_exception() {
        Order invalidOrder = Order.builder()
                .clientId(null)  // nullable = false
                .securityId(100L).side(Side.BUY)
                .quantity(50).priceType(PriceType.MARKET)
                .status(OrderStatus.PLACED).orderDate(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            orderRepository.saveAndFlush(invalidOrder);
        });
    }

    // ==================== findById() ====================

    @Test
    void testFindById_positive() {
        Optional<Order> found = orderRepository.findById(order1.getOrderId());

        assertTrue(found.isPresent());
        assertEquals(order1.getClientId(), found.get().getClientId());
    }

    @Test
    void testFindById_notFound_negative() {
        Optional<Order> found = orderRepository.findById(9999L);

        assertFalse(found.isPresent());
    }

    // ==================== findByClientId() ====================

    @Test
    void testFindByClientId_positive() {
        List<Order> orders = orderRepository.findByClientId(10L);

        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getClientId().equals(10L)));
    }

    @Test
    void testFindByClientId_noMatch_negative() {
        List<Order> orders = orderRepository.findByClientId(9999L);

        assertTrue(orders.isEmpty());
    }

    // ==================== findByStatus() ====================

    @Test
    void testFindByStatus_positive() {
        List<Order> placedOrders = orderRepository.findByStatus(OrderStatus.PLACED);

        assertEquals(2, placedOrders.size());
        assertTrue(placedOrders.stream().allMatch(o -> o.getStatus() == OrderStatus.PLACED));
    }

    @Test
    void testFindByStatus_noMatch_negative() {
        List<Order> cancelledOrders = orderRepository.findByStatus(OrderStatus.CANCELLED);

        assertTrue(cancelledOrders.isEmpty());
    }

    // ==================== findBySecurityId() ====================

    @Test
    void testFindBySecurityId_positive() {
        List<Order> orders = orderRepository.findBySecurityId(100L);

        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getSecurityId().equals(100L)));
    }

    @Test
    void testFindBySecurityId_noMatch_negative() {
        List<Order> orders = orderRepository.findBySecurityId(9999L);

        assertTrue(orders.isEmpty());
    }

    // ==================== deleteById() ====================

    @Test
    void testDeleteById_positive() {
        Long id = order1.getOrderId();
        orderRepository.deleteById(id);

        Optional<Order> found = orderRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ==================== findAll() ====================

    @Test
    void testFindAll_positive() {
        List<Order> orders = orderRepository.findAll();

        assertEquals(3, orders.size());
    }

    @Test
    void testFindAll_emptyTable_negative() {
        orderRepository.deleteAll();

        List<Order> orders = orderRepository.findAll();

        assertTrue(orders.isEmpty());
    }
}
