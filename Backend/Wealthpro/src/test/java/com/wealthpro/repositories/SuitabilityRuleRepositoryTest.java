package com.wealthpro.repositories;

import com.wealthpro.entities.SuitabilityRule;
import com.wealthpro.enums.RuleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SuitabilityRuleRepositoryTest {

    @Autowired
    private SuitabilityRuleRepository suitabilityRuleRepository;

    private SuitabilityRule rule;

    @BeforeEach
    void setUp() {
        rule = new SuitabilityRule();
        rule.setDescription("Conservative clients allowed only in low risk assets");
        rule.setExpression("IF riskClass == Conservative THEN allowedAssets = [Bond, FD]");
        rule.setStatus(RuleStatus.Active);
    }

    // ─────────────────────────────────────────
    // TEST 1: Save SuitabilityRule
    // ─────────────────────────────────────────
    @Test
    void testSaveRule_Success() {
        SuitabilityRule saved = suitabilityRuleRepository.save(rule);

        assertNotNull(saved);
        assertNotNull(saved.getRuleId());
        assertEquals(RuleStatus.Active, saved.getStatus());
        assertEquals("Conservative clients allowed only in low risk assets",
                saved.getDescription());
    }

    // ─────────────────────────────────────────
    // TEST 2: Find rule by ID — exists
    // ─────────────────────────────────────────
    @Test
    void testFindById_WhenRuleExists_ReturnsRule() {
        SuitabilityRule saved = suitabilityRuleRepository.save(rule);

        Optional<SuitabilityRule> found = suitabilityRuleRepository
                .findById(saved.getRuleId());

        assertTrue(found.isPresent());
        assertEquals("IF riskClass == Conservative THEN allowedAssets = [Bond, FD]",
                found.get().getExpression());
    }

    // ─────────────────────────────────────────
    // TEST 3: Find rule by ID — not found
    // ─────────────────────────────────────────
    @Test
    void testFindById_WhenRuleNotExists_ReturnsEmpty() {
        Optional<SuitabilityRule> found = suitabilityRuleRepository.findById(999L);

        assertFalse(found.isPresent());
    }

    // ─────────────────────────────────────────
    // TEST 4: Find all rules
    // ─────────────────────────────────────────
    @Test
    void testFindAllRules_ReturnsAllRules() {
        suitabilityRuleRepository.save(rule);

        SuitabilityRule rule2 = new SuitabilityRule();
        rule2.setDescription("Aggressive clients excluded from unregulated products");
        rule2.setExpression("IF riskClass == Aggressive THEN excludedAssets = [PennyStocks]");
        rule2.setStatus(RuleStatus.Active);
        suitabilityRuleRepository.save(rule2);

        List<SuitabilityRule> rules = suitabilityRuleRepository.findAll();

        assertEquals(2, rules.size());
    }

    // ─────────────────────────────────────────
    // TEST 5: Update rule status to Inactive
    // ─────────────────────────────────────────
    @Test
    void testUpdateRuleStatus_ToInactive() {
        SuitabilityRule saved = suitabilityRuleRepository.save(rule);

        saved.setStatus(RuleStatus.Inactive);
        SuitabilityRule updated = suitabilityRuleRepository.save(saved);

        assertEquals(RuleStatus.Inactive, updated.getStatus());
    }

    // ─────────────────────────────────────────
    // TEST 6: Delete rule
    // ─────────────────────────────────────────
    @Test
    void testDeleteRule_Success() {
        SuitabilityRule saved = suitabilityRuleRepository.save(rule);

        suitabilityRuleRepository.deleteById(saved.getRuleId());

        Optional<SuitabilityRule> found = suitabilityRuleRepository
                .findById(saved.getRuleId());
        assertFalse(found.isPresent());
    }
}