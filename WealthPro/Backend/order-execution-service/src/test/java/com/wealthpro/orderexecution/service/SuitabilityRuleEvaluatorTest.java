package com.wealthpro.orderexecution.service;

import com.wealthpro.orderexecution.feign.dto.SuitabilityRuleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SuitabilityRuleEvaluator.
 * Tests all supported operators and logic combinators.
 */
public class SuitabilityRuleEvaluatorTest {

    private SuitabilityRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new SuitabilityRuleEvaluator();
    }

    // ─── helper to build a SuitabilityRuleDTO ─────────────────────────────────

    private SuitabilityRuleDTO buildRule(Long id, String expression) {
        SuitabilityRuleDTO rule = new SuitabilityRuleDTO();
        rule.setRuleId(id);
        rule.setDescription("Test rule " + id);
        rule.setExpression(expression);
        rule.setStatus("ACTIVE");
        return rule;
    }

    private Map<String, String> ctx(String... keyValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    // ─── null / blank guards ──────────────────────────────────────────────────

    @Test
    void testEvaluate_NullRule_ReturnsFalse() {
        assertFalse(evaluator.evaluate(null, ctx()));
    }

    @Test
    void testEvaluate_NullExpression_ReturnsFalse() {
        SuitabilityRuleDTO rule = new SuitabilityRuleDTO();
        rule.setRuleId(1L);
        rule.setExpression(null);
        assertFalse(evaluator.evaluate(rule, ctx()));
    }

    @Test
    void testEvaluate_BlankExpression_ReturnsFalse() {
        SuitabilityRuleDTO rule = buildRule(1L, "   ");
        assertFalse(evaluator.evaluate(rule, ctx()));
    }

    // ─── == operator (string) ─────────────────────────────────────────────────

    @Test
    void testEvaluate_StringEquals_Match_ReturnsTrue() {
        SuitabilityRuleDTO rule = buildRule(1L, "riskClass == CONSERVATIVE");
        Map<String, String> context = ctx("riskClass", "CONSERVATIVE");
        assertTrue(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_StringEquals_NoMatch_ReturnsFalse() {
        SuitabilityRuleDTO rule = buildRule(1L, "riskClass == CONSERVATIVE");
        Map<String, String> context = ctx("riskClass", "AGGRESSIVE");
        assertFalse(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_StringEquals_CaseInsensitive_Match() {
        // evaluator normalises to uppercase via context, but compare is case-insensitive
        SuitabilityRuleDTO rule = buildRule(1L, "riskClass == conservative");
        Map<String, String> context = ctx("riskClass", "CONSERVATIVE");
        assertTrue(evaluator.evaluate(rule, context));
    }

    // ─── != operator ──────────────────────────────────────────────────────────

    @Test
    void testEvaluate_NotEquals_Match_ReturnsTrue() {
        SuitabilityRuleDTO rule = buildRule(2L, "riskClass != CONSERVATIVE");
        Map<String, String> context = ctx("riskClass", "AGGRESSIVE");
        assertTrue(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_NotEquals_SameValue_ReturnsFalse() {
        SuitabilityRuleDTO rule = buildRule(2L, "riskClass != CONSERVATIVE");
        Map<String, String> context = ctx("riskClass", "CONSERVATIVE");
        assertFalse(evaluator.evaluate(rule, context));
    }

    // ─── numeric operators ────────────────────────────────────────────────────

    @Test
    void testEvaluate_NumericGreaterThan_Match_ReturnsTrue() {
        SuitabilityRuleDTO rule = buildRule(3L, "quantity > 5000");
        Map<String, String> context = ctx("quantity", "10000");
        assertTrue(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_NumericGreaterThan_NoMatch_ReturnsFalse() {
        SuitabilityRuleDTO rule = buildRule(3L, "quantity > 5000");
        Map<String, String> context = ctx("quantity", "1000");
        assertFalse(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_NumericGreaterThanOrEqual_Match_ReturnsTrue() {
        SuitabilityRuleDTO rule = buildRule(4L, "quantity >= 5000");
        Map<String, String> context = ctx("quantity", "5000");
        assertTrue(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_NumericLessThan_Match_ReturnsTrue() {
        SuitabilityRuleDTO rule = buildRule(5L, "quantity < 100");
        Map<String, String> context = ctx("quantity", "50");
        assertTrue(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_NumericLessThanOrEqual_Match_ReturnsTrue() {
        SuitabilityRuleDTO rule = buildRule(5L, "quantity <= 100");
        Map<String, String> context = ctx("quantity", "100");
        assertTrue(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_NumericEquals_Match_ReturnsTrue() {
        SuitabilityRuleDTO rule = buildRule(6L, "quantity == 500");
        Map<String, String> context = ctx("quantity", "500");
        assertTrue(evaluator.evaluate(rule, context));
    }

    // ─── AND combinator ───────────────────────────────────────────────────────

    @Test
    void testEvaluate_AndCondition_BothPass_ReturnsTrue() {
        SuitabilityRuleDTO rule = buildRule(7L, "riskClass == CONSERVATIVE AND assetClass == EQUITY");
        Map<String, String> context = ctx("riskClass", "CONSERVATIVE", "assetClass", "EQUITY");
        assertTrue(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_AndCondition_OneFails_ReturnsFalse() {
        SuitabilityRuleDTO rule = buildRule(7L, "riskClass == CONSERVATIVE AND assetClass == EQUITY");
        // riskClass matches but assetClass does not
        Map<String, String> context = ctx("riskClass", "CONSERVATIVE", "assetClass", "BOND");
        assertFalse(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_AndConditionUsingAmpersand_BothPass_ReturnsTrue() {
        // && should be normalized to AND
        SuitabilityRuleDTO rule = buildRule(8L, "riskClass == CONSERVATIVE && quantity > 100");
        Map<String, String> context = ctx("riskClass", "CONSERVATIVE", "quantity", "500");
        assertTrue(evaluator.evaluate(rule, context));
    }

    // ─── OR combinator ────────────────────────────────────────────────────────

    @Test
    void testEvaluate_OrCondition_FirstMatches_ReturnsTrue() {
        SuitabilityRuleDTO rule = buildRule(9L, "side == BUY || side == SUBSCRIBE");
        Map<String, String> context = ctx("side", "BUY");
        assertTrue(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_OrCondition_SecondMatches_ReturnsTrue() {
        SuitabilityRuleDTO rule = buildRule(9L, "side == BUY || side == SUBSCRIBE");
        Map<String, String> context = ctx("side", "SUBSCRIBE");
        assertTrue(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_OrCondition_NoneMatches_ReturnsFalse() {
        SuitabilityRuleDTO rule = buildRule(9L, "side == BUY || side == SUBSCRIBE");
        Map<String, String> context = ctx("side", "SELL");
        assertFalse(evaluator.evaluate(rule, context));
    }

    // ─── unknown variable ─────────────────────────────────────────────────────

    @Test
    void testEvaluate_UnknownVariable_ReturnsFalse() {
        SuitabilityRuleDTO rule = buildRule(10L, "unknownVar == SOMETHING");
        Map<String, String> context = ctx("riskClass", "CONSERVATIVE");
        // unknownVar not in context → should return false
        assertFalse(evaluator.evaluate(rule, context));
    }

    // ─── parentheses stripped ─────────────────────────────────────────────────

    @Test
    void testEvaluate_ParenthesesStripped_WorksCorrectly() {
        // parentheses should be stripped by normalize()
        SuitabilityRuleDTO rule = buildRule(11L, "(riskClass == CONSERVATIVE)");
        Map<String, String> context = ctx("riskClass", "CONSERVATIVE");
        assertTrue(evaluator.evaluate(rule, context));
    }

    // ─── complex multi-condition rule ─────────────────────────────────────────

    @Test
    void testEvaluate_ComplexRule_AllConditionsMatch() {
        SuitabilityRuleDTO rule = buildRule(12L,
                "riskClass == CONSERVATIVE AND assetClass == EQUITY AND quantity > 1000");
        Map<String, String> context = ctx(
                "riskClass",  "CONSERVATIVE",
                "assetClass", "EQUITY",
                "quantity",   "5000");
        assertTrue(evaluator.evaluate(rule, context));
    }

    @Test
    void testEvaluate_ComplexRule_LastConditionFails_ReturnsFalse() {
        SuitabilityRuleDTO rule = buildRule(12L,
                "riskClass == CONSERVATIVE AND assetClass == EQUITY AND quantity > 1000");
        Map<String, String> context = ctx(
                "riskClass",  "CONSERVATIVE",
                "assetClass", "EQUITY",
                "quantity",   "500");  // fails
        assertFalse(evaluator.evaluate(rule, context));
    }
}
