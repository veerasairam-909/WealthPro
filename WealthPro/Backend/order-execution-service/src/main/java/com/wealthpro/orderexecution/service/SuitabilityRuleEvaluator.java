package com.wealthpro.orderexecution.service;

import com.wealthpro.orderexecution.feign.dto.SuitabilityRuleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Simple expression evaluator for suitability rules.
 * <p>
 * Supports expressions like:
 * {@code riskClass == CONSERVATIVE AND assetClass != EQUITY}
 * {@code side == BUY AND riskClass == CONSERVATIVE}
 * {@code quantity > 5000}
 * </p>
 *
 * <p>Supported context variables:
 * <ul>
 *   <li>{@code riskClass} — client risk class (e.g. CONSERVATIVE, BALANCED, AGGRESSIVE)</li>
 *   <li>{@code assetClass} — security asset class (e.g. EQUITY, BOND, CASH, MF)</li>
 *   <li>{@code side} — order side (e.g. BUY, SELL, SUBSCRIBE, REDEEM)</li>
 *   <li>{@code priceType} — order price type (e.g. MARKET, LIMIT, NAV)</li>
 *   <li>{@code quantity} — order quantity (numeric)</li>
 * </ul>
 * </p>
 *
 * <p>Supported operators: {@code ==}, {@code !=}, {@code >=}, {@code <=}, {@code >}, {@code <}</p>
 * <p>Supported connectors: {@code AND} (all conditions must be true for rule to trigger/fail)</p>
 */
@Slf4j
@Component
public class SuitabilityRuleEvaluator {

    /**
     * Evaluates a rule expression against the given order context.
     *
     * @param rule    the suitability rule to evaluate
     * @param context map of variable names to their string values (e.g. riskClass → CONSERVATIVE)
     * @return true if the rule expression evaluates to true (i.e. the rule is violated/triggered)
     */
    public boolean evaluate(SuitabilityRuleDTO rule, Map<String, String> context) {
        if (rule == null || rule.getExpression() == null || rule.getExpression().isBlank()) {
            return false;
        }

        String expression = rule.getExpression().trim();

        // Split on AND (case-insensitive)
        String[] conditions = expression.split("(?i)\\s+AND\\s+");

        for (String condition : conditions) {
            if (!evaluateCondition(condition.trim(), context, rule.getRuleId())) {
                // All conditions must match for the rule to trigger; if any fails, rule is not violated
                return false;
            }
        }

        // All conditions matched — rule is triggered (order violates this rule)
        return true;
    }

    /**
     * Evaluates a single condition like {@code riskClass == CONSERVATIVE} or {@code quantity > 5000}.
     *
     * @return true if the condition holds for the given context
     */
    private boolean evaluateCondition(String condition, Map<String, String> context, Long ruleId) {
        // Try each operator in order (longer operators first to avoid partial matches)
        String[] operators = { ">=", "<=", "!=", "==", ">", "<" };

        for (String op : operators) {
            int idx = condition.indexOf(op);
            if (idx < 0) continue;

            String variable = condition.substring(0, idx).trim();
            String expected  = condition.substring(idx + op.length()).trim();

            String actual = context.get(variable);
            if (actual == null) {
                log.warn("[SUITABILITY] Rule {} references unknown variable '{}' — skipping condition",
                        ruleId, variable);
                return false;
            }

            return compare(actual, op, expected);
        }

        log.warn("[SUITABILITY] Rule {} has unparseable condition '{}' — treating as not matched", ruleId, condition);
        return false;
    }

    /**
     * Compares actualValue to expectedValue using the given operator.
     * Tries numeric comparison first; falls back to case-insensitive string comparison.
     */
    private boolean compare(String actual, String op, String expected) {
        // Try numeric comparison
        try {
            double a = Double.parseDouble(actual);
            double e = Double.parseDouble(expected);
            return switch (op) {
                case "==" -> a == e;
                case "!=" -> a != e;
                case ">"  -> a >  e;
                case "<"  -> a <  e;
                case ">=" -> a >= e;
                case "<=" -> a <= e;
                default   -> false;
            };
        } catch (NumberFormatException ignored) {
            // Fall through to string comparison
        }

        // String comparison (case-insensitive)
        int cmp = actual.equalsIgnoreCase(expected) ? 0 : actual.compareToIgnoreCase(expected);
        return switch (op) {
            case "==" -> cmp == 0;
            case "!=" -> cmp != 0;
            case ">"  -> cmp >  0;
            case "<"  -> cmp <  0;
            case ">=" -> cmp >= 0;
            case "<=" -> cmp <= 0;
            default   -> false;
        };
    }
}
