package com.wealthpro.orderexecution.repository;

import com.wealthpro.orderexecution.entities.Order;
import com.wealthpro.orderexecution.entities.PreTradeCheck;
import com.wealthpro.orderexecution.enums.*;
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
 * Unit tests for {@link PreTradeCheckRepository} covering CRUD and custom finder methods.
 *
 * @author WealthPro Team
 */
@DataJpaTest
@ActiveProfiles("test")
class PreTradeCheckRepositoryTest {

    @Autowired
    private PreTradeCheckRepository preTradeCheckRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;
    private PreTradeCheck check1;
    private PreTradeCheck check2;
    private PreTradeCheck check3;

    @BeforeEach
    void setUp() {
        preTradeCheckRepository.deleteAll();
        orderRepository.deleteAll();

        order1 = orderRepository.save(Order.builder()
                .clientId(10L).securityId(100L).side(Side.BUY)
                .quantity(50).priceType(PriceType.MARKET)
                .status(OrderStatus.PLACED).orderDate(LocalDateTime.now())
                .build());

        order2 = orderRepository.save(Order.builder()
                .clientId(20L).securityId(200L).side(Side.SELL)
                .quantity(30).priceType(PriceType.LIMIT).limitPrice(155.0)
                .status(OrderStatus.PLACED).orderDate(LocalDateTime.now())
                .build());

        check1 = PreTradeCheck.builder()
                .order(order1).checkType(CheckType.SUITABILITY)
                .result(CheckResult.PASS).message("Client risk profile matches")
                .checkedDate(LocalDateTime.now())
                .build();

        check2 = PreTradeCheck.builder()
                .order(order1).checkType(CheckType.CASH)
                .result(CheckResult.PASS).message("Sufficient cash balance")
                .checkedDate(LocalDateTime.now())
                .build();

        check3 = PreTradeCheck.builder()
                .order(order2).checkType(CheckType.EXPOSURE)
                .result(CheckResult.FAIL).message("Exposure limit exceeded")
                .checkedDate(LocalDateTime.now())
                .build();

        preTradeCheckRepository.saveAll(List.of(check1, check2, check3));
    }

    // ==================== save() ====================

    @Test
    void testSave_positive() {
        PreTradeCheck newCheck = PreTradeCheck.builder()
                .order(order2).checkType(CheckType.LIMIT)
                .result(CheckResult.PASS).message("Within limit")
                .checkedDate(LocalDateTime.now())
                .build();

        PreTradeCheck saved = preTradeCheckRepository.save(newCheck);

        assertNotNull(saved.getCheckId());
        assertEquals(CheckType.LIMIT, saved.getCheckType());
    }

    @Test
    void testSave_nullRequiredField_exception() {
        PreTradeCheck invalidCheck = PreTradeCheck.builder()
                .order(order1).checkType(null)  // nullable = false
                .result(CheckResult.PASS).message("Test")
                .checkedDate(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            preTradeCheckRepository.saveAndFlush(invalidCheck);
        });
    }

    // ==================== findById() ====================

    @Test
    void testFindById_positive() {
        Optional<PreTradeCheck> found = preTradeCheckRepository.findById(check1.getCheckId());

        assertTrue(found.isPresent());
        assertEquals(CheckType.SUITABILITY, found.get().getCheckType());
    }

    @Test
    void testFindById_notFound_negative() {
        Optional<PreTradeCheck> found = preTradeCheckRepository.findById(9999L);

        assertFalse(found.isPresent());
    }

    // ==================== findByOrder_OrderId() ====================

    @Test
    void testFindByOrder_OrderId_positive() {
        List<PreTradeCheck> checks = preTradeCheckRepository.findByOrder_OrderId(order1.getOrderId());

        assertEquals(2, checks.size());
    }

    @Test
    void testFindByOrder_OrderId_singleResult_positive() {
        List<PreTradeCheck> checks = preTradeCheckRepository.findByOrder_OrderId(order2.getOrderId());

        assertEquals(1, checks.size());
        assertEquals(CheckResult.FAIL, checks.get(0).getResult());
    }

    @Test
    void testFindByOrder_OrderId_noMatch_negative() {
        List<PreTradeCheck> checks = preTradeCheckRepository.findByOrder_OrderId(9999L);

        assertTrue(checks.isEmpty());
    }

    // ==================== deleteById() ====================

    @Test
    void testDeleteById_positive() {
        Long id = check1.getCheckId();
        preTradeCheckRepository.deleteById(id);

        Optional<PreTradeCheck> found = preTradeCheckRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ==================== findAll() ====================

    @Test
    void testFindAll_positive() {
        List<PreTradeCheck> checks = preTradeCheckRepository.findAll();

        assertEquals(3, checks.size());
    }

    @Test
    void testFindAll_emptyTable_negative() {
        preTradeCheckRepository.deleteAll();

        List<PreTradeCheck> checks = preTradeCheckRepository.findAll();

        assertTrue(checks.isEmpty());
    }
}
