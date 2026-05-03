package com.wealthpro.orderexecution.service;

import com.wealthpro.orderexecution.feign.dto.SuitabilityRuleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Expression evaluator for suitability rules.
 *
 * <p>Supported context variables:
 * <ul>
 *   <li>{@code riskClass}    — client risk class (CONSERVATIVE / BALANCED / AGGRESSIVE)</li>
 *   <li>{@code assetClass}   — security asset class (EQUITY / BOND / MUTUAL_FUND / ETF / STRUCTURED)</li>
 *   <li>{@code side}         — order side (BUY / SELL)</li>
 *   <li>{@code priceType}    — order price type (MARKET / LIMIT / NAV)</li>
 *   <li>{@code quantity}     — order quantity (numeric)</li>
 *   <li>{@code segment}      — client segment (Retail / HNI / UHNI)</li>
 *   <li>{@code status}       — client status (Active / Inactive)</li>
 *   <li>{@code orderValue}   — quantity × price (numeric, 0 for MARKET orders)</li>
 *   <li>{@code currency}     — security currency (INR / USD …)</li>
 * </ul>
 *
 * <p>Supported operators: {@code ==}, {@code !=}, {@code >=}, {@code <=}, {@code >}, {@code <}
 * <p>Supported connectors: {@code AND} or {@code &&} — ALL conditions must hold for rule to trigger
 * <p>OR within a condition: {@code ||} — ANY sub-condition may hold
 * <p>Parentheses {@code ()} are stripped before parsing.
 */
@Slf4j
@Component
public class SuitabilityRuleEvaluator {

    /**
     * Evaluates a rule expression against the given order context.
     *
     * @param rule    the suitability rule to evaluate
     * @param context map of variable names → string values
     * @return {@code true} if the rule is triggered (order violates this rule)
     */
    public boolean evaluate(SuitabilityRuleDTO rule, Map<String, String> context) {
        if (rule == null || rule.getExpression() == null || rule.getExpression().isBlank()) {
            return false;
        }

        // Normalize: replace && with AND, strip parentheses, collapse whitespace
        String expression = normalize(rule.getExpression());

        // Split on AND (case-insensitive)
        String[] conditions = expression.split("(?i)\\s+AND\\s+");

        for (String rawCondition : conditions) {
            String condition = rawCondition.trim();
            if (condition.isBlank()) continue;

            // Handle OR within a single condition segment  e.g.  priceType == LIMIT || priceType == MARKET
            if (condition.contains("||")) {
                String[] orParts = condition.split("\\|\\|");
                boolean anyOrMatched = false;
                for (String orPart : orParts) {
                    if (evaluateCondition(orPart.trim(), context, rule.getRuleId())) {
                        anyOrMatched = true;
                        break;
                    }
                }
                if (!anyOrMatched) return false; // this AND-clause failed
            } else {
                if (!evaluateCondition(condition, context, rule.getRuleId())) {
                    return false; // AND short-circuit
                }
            }
        }

        // All AND-clauses matched — rule is triggered
        return true;
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Normalises the raw expression string so the parser can handle it uniformly.
     * <ul>
     *   <li>{@code &&} → {@code  AND }</li>
     *   <li>{@code (} / {@code )} → removed</li>
     *   <li>Multiple spaces collapsed to one</li>
     * </ul>
     */
    private String normalize(String raw) {
        return raw
                .replace("&&", " AND ")
                .replace("(", " ")
                .replace(")", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Evaluates a single atomic condition such as
     * {@code riskClass == CONSERVATIVE} or {@code quantity > 5000}.
     */
    private boolean evaluateCondition(String condition, Map<String, String> context, Long ruleId) {
        // Operators tried longest-first to avoid e.g. ">" matching inside ">="
        String[] operators = { ">=", "<=", "!=", "==", ">", "<" };

        for (String op : operators) {
            int idx = condition.indexOf(op);
            if (idx < 0) continue;

            String variable = condition.substring(0, idx).trim();
            String expected = condition.substring(idx + op.length()).trim();

            String actual = context.get(variable);
            if (actual == null) {
                log.warn("[SUITABILITY] Rule {} references unknown variable '{}' — condition skipped",
                        ruleId, variable);
                return false;
            }

            return compare(actual, op, expected, ruleId);
        }

        log.warn("[SUITABILITY] Rule {} unparseable condition '{}' — treated as not matched",
                ruleId, condition);
        return false;
    }

    /**
     * Compares {@code actual} to {@code expected} using {@code op}.
     * Tries numeric comparison first; falls back to case-insensitive string comparison.
     */
    private boolean compare(String actual, String op, String expected, Long ruleId) {
        // Numeric path
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
        } catch (NumberFormatException ignored) { /* fall through */ }

        // String path (case-insensitive)
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
