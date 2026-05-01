package com.wealthpro.analytics.repository;

import com.wealthpro.analytics.entities.ComplianceBreach;
import com.wealthpro.analytics.enums.BreachStatus;
import com.wealthpro.analytics.enums.Severity;
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
 * Unit tests for {@link ComplianceBreachRepository} covering CRUD and custom finder methods.
 *
 * @author WealthPro Team
 */
@DataJpaTest
@ActiveProfiles("test")
class ComplianceBreachRepositoryTest {

    @Autowired
    private ComplianceBreachRepository complianceBreachRepository;

    private ComplianceBreach breach1;
    private ComplianceBreach breach2;
    private ComplianceBreach breach3;

    @BeforeEach
    void setUp() {
        complianceBreachRepository.deleteAll();

        breach1 = ComplianceBreach.builder()
                .accountId(1001L).ruleId(1L)
                .severity(Severity.HIGH).description("Single stock exceeds 15% of portfolio")
                .status(BreachStatus.OPEN).detectedAt(LocalDateTime.now())
                .build();

        breach2 = ComplianceBreach.builder()
                .accountId(1001L).ruleId(2L)
                .severity(Severity.MEDIUM).description("Cash reserve below 5%")
                .status(BreachStatus.ACKNOWLEDGED).detectedAt(LocalDateTime.now())
                .build();

        breach3 = ComplianceBreach.builder()
                .accountId(2002L).ruleId(3L)
                .severity(Severity.CRITICAL).description("IT sector exposure exceeds 30%")
                .status(BreachStatus.OPEN).detectedAt(LocalDateTime.now())
                .build();

        complianceBreachRepository.saveAll(List.of(breach1, breach2, breach3));
    }

    // ==================== save() ====================

    @Test
    void testSave_positive() {
        ComplianceBreach newBreach = ComplianceBreach.builder()
                .accountId(3003L).ruleId(4L)
                .severity(Severity.LOW).description("Leverage ratio slightly elevated")
                .status(BreachStatus.OPEN).detectedAt(LocalDateTime.now())
                .build();

        ComplianceBreach saved = complianceBreachRepository.save(newBreach);

        assertNotNull(saved.getBreachId());
        assertEquals(3003L, saved.getAccountId());
    }

    @Test
    void testSave_nullRequiredField_exception() {
        ComplianceBreach invalidBreach = ComplianceBreach.builder()
                .accountId(1001L).ruleId(null)  // nullable = false
                .severity(Severity.HIGH).description("Test breach")
                .status(BreachStatus.OPEN).detectedAt(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            complianceBreachRepository.saveAndFlush(invalidBreach);
        });
    }

    // ==================== findById() ====================

    @Test
    void testFindById_positive() {
        Optional<ComplianceBreach> found = complianceBreachRepository.findById(breach1.getBreachId());

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getRuleId());
    }

    @Test
    void testFindById_notFound_negative() {
        Optional<ComplianceBreach> found = complianceBreachRepository.findById(9999L);

        assertFalse(found.isPresent());
    }

    // ==================== findByAccountId() ====================

    @Test
    void testFindByAccountId_positive() {
        List<ComplianceBreach> breaches = complianceBreachRepository.findByAccountId(1001L);

        assertEquals(2, breaches.size());
        assertTrue(breaches.stream().allMatch(b -> b.getAccountId().equals(1001L)));
    }

    @Test
    void testFindByAccountId_singleResult_positive() {
        List<ComplianceBreach> breaches = complianceBreachRepository.findByAccountId(2002L);

        assertEquals(1, breaches.size());
        assertEquals(3L, breaches.get(0).getRuleId());
    }

    @Test
    void testFindByAccountId_noMatch_negative() {
        List<ComplianceBreach> breaches = complianceBreachRepository.findByAccountId(9999L);

        assertTrue(breaches.isEmpty());
    }

    // ==================== findByAccountIdAndStatus() ====================

    @Test
    void testFindByAccountIdAndStatus_positive() {
        List<ComplianceBreach> breaches = complianceBreachRepository
                .findByAccountIdAndStatus(1001L, BreachStatus.OPEN);

        assertEquals(1, breaches.size());
        assertEquals(1L, breaches.get(0).getRuleId());
    }

    @Test
    void testFindByAccountIdAndStatus_acknowledged_positive() {
        List<ComplianceBreach> breaches = complianceBreachRepository
                .findByAccountIdAndStatus(1001L, BreachStatus.ACKNOWLEDGED);

        assertEquals(1, breaches.size());
        assertEquals(2L, breaches.get(0).getRuleId());
    }

    @Test
    void testFindByAccountIdAndStatus_wrongStatus_negative() {
        List<ComplianceBreach> breaches = complianceBreachRepository
                .findByAccountIdAndStatus(1001L, BreachStatus.CLOSED);

        assertTrue(breaches.isEmpty());
    }

    @Test
    void testFindByAccountIdAndStatus_wrongAccountId_negative() {
        List<ComplianceBreach> breaches = complianceBreachRepository
                .findByAccountIdAndStatus(9999L, BreachStatus.OPEN);

        assertTrue(breaches.isEmpty());
    }

    // ==================== deleteById() ====================

    @Test
    void testDeleteById_positive() {
        Long id = breach1.getBreachId();
        complianceBreachRepository.deleteById(id);

        Optional<ComplianceBreach> found = complianceBreachRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ==================== findAll() ====================

    @Test
    void testFindAll_positive() {
        List<ComplianceBreach> breaches = complianceBreachRepository.findAll();

        assertEquals(3, breaches.size());
    }

    @Test
    void testFindAll_emptyTable_negative() {
        complianceBreachRepository.deleteAll();

        List<ComplianceBreach> breaches = complianceBreachRepository.findAll();

        assertTrue(breaches.isEmpty());
    }
}
