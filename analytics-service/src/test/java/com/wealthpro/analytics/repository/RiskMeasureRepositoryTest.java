package com.wealthpro.analytics.repository;

import com.wealthpro.analytics.entities.RiskMeasure;
import com.wealthpro.analytics.enums.MeasureType;
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
 * Unit tests for {@link RiskMeasureRepository} covering CRUD and custom finder methods.
 *
 * @author WealthPro Team
 */
@DataJpaTest
@ActiveProfiles("test")
class RiskMeasureRepositoryTest {

    @Autowired
    private RiskMeasureRepository riskMeasureRepository;

    private RiskMeasure measure1;
    private RiskMeasure measure2;
    private RiskMeasure measure3;

    @BeforeEach
    void setUp() {
        riskMeasureRepository.deleteAll();

        measure1 = RiskMeasure.builder()
                .accountId(1001L).measureType(MeasureType.VOLATILITY)
                .measureValue(12.5).description("Annual portfolio volatility")
                .calculatedAt(LocalDateTime.now())
                .build();

        measure2 = RiskMeasure.builder()
                .accountId(1001L).measureType(MeasureType.VAR_95)
                .measureValue(3.2).description("95% Value at Risk")
                .calculatedAt(LocalDateTime.now())
                .build();

        measure3 = RiskMeasure.builder()
                .accountId(2002L).measureType(MeasureType.MAX_DRAWDOWN)
                .measureValue(8.7).description("Maximum drawdown from peak")
                .calculatedAt(LocalDateTime.now())
                .build();

        riskMeasureRepository.saveAll(List.of(measure1, measure2, measure3));
    }

    // ==================== save() ====================

    @Test
    void testSave_positive() {
        RiskMeasure newMeasure = RiskMeasure.builder()
                .accountId(3003L).measureType(MeasureType.TRACKING_ERROR)
                .measureValue(1.5).description("Tracking error vs benchmark")
                .calculatedAt(LocalDateTime.now())
                .build();

        RiskMeasure saved = riskMeasureRepository.save(newMeasure);

        assertNotNull(saved.getMeasureId());
        assertEquals(3003L, saved.getAccountId());
        assertEquals(MeasureType.TRACKING_ERROR, saved.getMeasureType());
    }

    @Test
    void testSave_nullRequiredField_exception() {
        RiskMeasure invalidMeasure = RiskMeasure.builder()
                .accountId(1001L).measureType(null)  // nullable = false
                .measureValue(5.0).description("Test measure")
                .calculatedAt(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            riskMeasureRepository.saveAndFlush(invalidMeasure);
        });
    }

    // ==================== findById() ====================

    @Test
    void testFindById_positive() {
        Optional<RiskMeasure> found = riskMeasureRepository.findById(measure1.getMeasureId());

        assertTrue(found.isPresent());
        assertEquals(MeasureType.VOLATILITY, found.get().getMeasureType());
        assertEquals(12.5, found.get().getMeasureValue());
    }

    @Test
    void testFindById_notFound_negative() {
        Optional<RiskMeasure> found = riskMeasureRepository.findById(9999L);

        assertFalse(found.isPresent());
    }

    // ==================== findByAccountId() ====================

    @Test
    void testFindByAccountId_positive() {
        List<RiskMeasure> measures = riskMeasureRepository.findByAccountId(1001L);

        assertEquals(2, measures.size());
        assertTrue(measures.stream().allMatch(m -> m.getAccountId().equals(1001L)));
    }

    @Test
    void testFindByAccountId_singleResult_positive() {
        List<RiskMeasure> measures = riskMeasureRepository.findByAccountId(2002L);

        assertEquals(1, measures.size());
        assertEquals(MeasureType.MAX_DRAWDOWN, measures.get(0).getMeasureType());
    }

    @Test
    void testFindByAccountId_noMatch_negative() {
        List<RiskMeasure> measures = riskMeasureRepository.findByAccountId(9999L);

        assertTrue(measures.isEmpty());
    }

    // ==================== deleteById() ====================

    @Test
    void testDeleteById_positive() {
        Long id = measure1.getMeasureId();
        riskMeasureRepository.deleteById(id);

        Optional<RiskMeasure> found = riskMeasureRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ==================== findAll() ====================

    @Test
    void testFindAll_positive() {
        List<RiskMeasure> measures = riskMeasureRepository.findAll();

        assertEquals(3, measures.size());
    }

    @Test
    void testFindAll_emptyTable_negative() {
        riskMeasureRepository.deleteAll();

        List<RiskMeasure> measures = riskMeasureRepository.findAll();

        assertTrue(measures.isEmpty());
    }
}
