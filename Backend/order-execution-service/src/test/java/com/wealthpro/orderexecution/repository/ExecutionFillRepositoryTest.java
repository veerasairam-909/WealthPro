package com.wealthpro.orderexecution.repository;

import com.wealthpro.orderexecution.entities.ExecutionFill;
import com.wealthpro.orderexecution.entities.Order;
import com.wealthpro.orderexecution.enums.FillStatus;
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
 * Unit tests for {@link ExecutionFillRepository} covering CRUD and custom finder methods.
 *
 * @author WealthPro Team
 */
@DataJpaTest
@ActiveProfiles("test")
class ExecutionFillRepositoryTest {

    @Autowired
    private ExecutionFillRepository executionFillRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;
    private ExecutionFill fill1;
    private ExecutionFill fill2;
    private ExecutionFill fill3;

    @BeforeEach
    void setUp() {
        executionFillRepository.deleteAll();
        orderRepository.deleteAll();

        order1 = orderRepository.save(Order.builder()
                .clientId(10L).securityId(100L).side(Side.BUY)
                .quantity(100).priceType(PriceType.MARKET)
                .status(OrderStatus.ROUTED).orderDate(LocalDateTime.now())
                .routedVenue("SIMULATED_NSE")
                .build());

        order2 = orderRepository.save(Order.builder()
                .clientId(20L).securityId(200L).side(Side.SELL)
                .quantity(50).priceType(PriceType.LIMIT).limitPrice(155.0)
                .status(OrderStatus.ROUTED).orderDate(LocalDateTime.now())
                .routedVenue("SIMULATED_BSE")
                .build());

        fill1 = ExecutionFill.builder()
                .order(order1).fillQuantity(60).fillPrice(150.25)
                .fillDate(LocalDateTime.now()).venue("SIMULATED_NSE")
                .status(FillStatus.COMPLETED)
                .build();

        fill2 = ExecutionFill.builder()
                .order(order1).fillQuantity(40).fillPrice(150.50)
                .fillDate(LocalDateTime.now()).venue("SIMULATED_NSE")
                .status(FillStatus.COMPLETED)
                .build();

        fill3 = ExecutionFill.builder()
                .order(order2).fillQuantity(50).fillPrice(155.0)
                .fillDate(LocalDateTime.now()).venue("SIMULATED_BSE")
                .status(FillStatus.COMPLETED)
                .build();

        executionFillRepository.saveAll(List.of(fill1, fill2, fill3));
    }

    // ==================== save() ====================

    @Test
    void testSave_positive() {
        ExecutionFill newFill = ExecutionFill.builder()
                .order(order2).fillQuantity(25).fillPrice(155.0)
                .fillDate(LocalDateTime.now()).venue("SIMULATED_BSE")
                .status(FillStatus.PENDING)
                .build();

        ExecutionFill saved = executionFillRepository.save(newFill);

        assertNotNull(saved.getFillId());
        assertEquals(25, saved.getFillQuantity());
    }

    @Test
    void testSave_nullRequiredField_exception() {
        ExecutionFill invalidFill = ExecutionFill.builder()
                .order(order1).fillQuantity(null)
                .fillPrice(100.0).fillDate(LocalDateTime.now())
                .status(FillStatus.PENDING)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            executionFillRepository.saveAndFlush(invalidFill);
        });
    }

    // ==================== findById() ====================

    @Test
    void testFindById_positive() {
        Optional<ExecutionFill> found = executionFillRepository.findById(fill1.getFillId());

        assertTrue(found.isPresent());
        assertEquals(60, found.get().getFillQuantity());
    }

    @Test
    void testFindById_notFound_negative() {
        Optional<ExecutionFill> found = executionFillRepository.findById(9999L);

        assertFalse(found.isPresent());
    }

    // ==================== findByOrder_OrderId() ====================

    @Test
    void testFindByOrder_OrderId_positive() {
        List<ExecutionFill> fills = executionFillRepository.findByOrder_OrderId(order1.getOrderId());

        assertEquals(2, fills.size());
    }

    @Test
    void testFindByOrder_OrderId_singleResult_positive() {
        List<ExecutionFill> fills = executionFillRepository.findByOrder_OrderId(order2.getOrderId());

        assertEquals(1, fills.size());
        assertEquals(50, fills.get(0).getFillQuantity());
    }

    @Test
    void testFindByOrder_OrderId_noMatch_negative() {
        List<ExecutionFill> fills = executionFillRepository.findByOrder_OrderId(9999L);

        assertTrue(fills.isEmpty());
    }

    // ==================== deleteById() ====================

    @Test
    void testDeleteById_positive() {
        Long id = fill1.getFillId();
        executionFillRepository.deleteById(id);

        Optional<ExecutionFill> found = executionFillRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ==================== findAll() ====================

    @Test
    void testFindAll_positive() {
        List<ExecutionFill> fills = executionFillRepository.findAll();

        assertEquals(3, fills.size());
    }

    @Test
    void testFindAll_emptyTable_negative() {
        executionFillRepository.deleteAll();

        List<ExecutionFill> fills = executionFillRepository.findAll();

        assertTrue(fills.isEmpty());
    }
}
