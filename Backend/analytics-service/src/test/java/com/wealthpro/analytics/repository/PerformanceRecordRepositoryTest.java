package com.wealthpro.analytics.repository;

import com.wealthpro.analytics.entities.PerformanceRecord;
import com.wealthpro.analytics.enums.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PerformanceRecordRepository} covering CRUD and custom finder methods.
 *
 * @author WealthPro Team
 */
@DataJpaTest
@ActiveProfiles("test")
class PerformanceRecordRepositoryTest {

    @Autowired
    private PerformanceRecordRepository performanceRecordRepository;

    private PerformanceRecord record1;
    private PerformanceRecord record2;
    private PerformanceRecord record3;

    @BeforeEach
    void setUp() {
        performanceRecordRepository.deleteAll();

        record1 = PerformanceRecord.builder()
                .accountId(1001L).portfolioId(1L).period(Period.DAILY)
                .startDate(LocalDate.of(2026, 3, 25)).endDate(LocalDate.of(2026, 3, 25))
                .returnPercentage(2.5).benchmarkReturnPercentage(1.8)
                .calculatedAt(LocalDateTime.now())
                .build();

        record2 = PerformanceRecord.builder()
                .accountId(1001L).portfolioId(1L).period(Period.MONTHLY)
                .startDate(LocalDate.of(2026, 3, 1)).endDate(LocalDate.of(2026, 3, 31))
                .returnPercentage(8.3).benchmarkReturnPercentage(6.5)
                .calculatedAt(LocalDateTime.now())
                .build();

        record3 = PerformanceRecord.builder()
                .accountId(2002L).portfolioId(2L).period(Period.DAILY)
                .startDate(LocalDate.of(2026, 3, 25)).endDate(LocalDate.of(2026, 3, 25))
                .returnPercentage(-1.2).benchmarkReturnPercentage(0.5)
                .calculatedAt(LocalDateTime.now())
                .build();

        performanceRecordRepository.saveAll(List.of(record1, record2, record3));
    }

    // ==================== save() ====================

    @Test
    void testSave_positive() {
        PerformanceRecord newRecord = PerformanceRecord.builder()
                .accountId(3003L).portfolioId(3L).period(Period.DAILY)
                .startDate(LocalDate.of(2026, 3, 24)).endDate(LocalDate.of(2026, 3, 24))
                .returnPercentage(1.0).benchmarkReturnPercentage(0.8)
                .calculatedAt(LocalDateTime.now())
                .build();

        PerformanceRecord saved = performanceRecordRepository.save(newRecord);

        assertNotNull(saved.getRecordId());
        assertEquals(3003L, saved.getAccountId());
    }

    @Test
    void testSave_nullRequiredField_exception() {
        PerformanceRecord invalidRecord = PerformanceRecord.builder()
                .accountId(1001L).portfolioId(null)  // nullable = false
                .period(Period.DAILY)
                .startDate(LocalDate.of(2026, 3, 25)).endDate(LocalDate.of(2026, 3, 25))
                .returnPercentage(2.5).benchmarkReturnPercentage(1.8)
                .calculatedAt(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            performanceRecordRepository.saveAndFlush(invalidRecord);
        });
    }

    // ==================== findById() ====================

    @Test
    void testFindById_positive() {
        Optional<PerformanceRecord> found = performanceRecordRepository.findById(record1.getRecordId());

        assertTrue(found.isPresent());
        assertEquals(2.5, found.get().getReturnPercentage());
    }

    @Test
    void testFindById_notFound_negative() {
        Optional<PerformanceRecord> found = performanceRecordRepository.findById(9999L);

        assertFalse(found.isPresent());
    }

    // ==================== findByAccountId() ====================

    @Test
    void testFindByAccountId_positive() {
        List<PerformanceRecord> records = performanceRecordRepository.findByAccountId(1001L);

        assertEquals(2, records.size());
        assertTrue(records.stream().allMatch(r -> r.getAccountId().equals(1001L)));
    }

    @Test
    void testFindByAccountId_singleResult_positive() {
        List<PerformanceRecord> records = performanceRecordRepository.findByAccountId(2002L);

        assertEquals(1, records.size());
        assertEquals(-1.2, records.get(0).getReturnPercentage());
    }

    @Test
    void testFindByAccountId_noMatch_negative() {
        List<PerformanceRecord> records = performanceRecordRepository.findByAccountId(9999L);

        assertTrue(records.isEmpty());
    }

    // ==================== findByAccountIdAndPeriod() ====================

    @Test
    void testFindByAccountIdAndPeriod_daily_positive() {
        List<PerformanceRecord> records = performanceRecordRepository
                .findByAccountIdAndPeriod(1001L, Period.DAILY);

        assertEquals(1, records.size());
        assertEquals(2.5, records.get(0).getReturnPercentage());
    }

    @Test
    void testFindByAccountIdAndPeriod_monthly_positive() {
        List<PerformanceRecord> records = performanceRecordRepository
                .findByAccountIdAndPeriod(1001L, Period.MONTHLY);

        assertEquals(1, records.size());
        assertEquals(8.3, records.get(0).getReturnPercentage());
    }

    @Test
    void testFindByAccountIdAndPeriod_wrongPeriod_negative() {
        List<PerformanceRecord> records = performanceRecordRepository
                .findByAccountIdAndPeriod(2002L, Period.MONTHLY);

        assertTrue(records.isEmpty());
    }

    @Test
    void testFindByAccountIdAndPeriod_wrongAccountId_negative() {
        List<PerformanceRecord> records = performanceRecordRepository
                .findByAccountIdAndPeriod(9999L, Period.DAILY);

        assertTrue(records.isEmpty());
    }

    // ==================== deleteById() ====================

    @Test
    void testDeleteById_positive() {
        Long id = record1.getRecordId();
        performanceRecordRepository.deleteById(id);

        Optional<PerformanceRecord> found = performanceRecordRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ==================== findAll() ====================

    @Test
    void testFindAll_positive() {
        List<PerformanceRecord> records = performanceRecordRepository.findAll();

        assertEquals(3, records.size());
    }

    @Test
    void testFindAll_emptyTable_negative() {
        performanceRecordRepository.deleteAll();

        List<PerformanceRecord> records = performanceRecordRepository.findAll();

        assertTrue(records.isEmpty());
    }
}
