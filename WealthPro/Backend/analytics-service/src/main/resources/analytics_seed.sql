-- ============================================================
--  WealthPro — Analytics Database Seed Data
--  Database : analytics_db
-- ============================================================

USE analytics_db;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE performance_records;
TRUNCATE TABLE risk_measures;
TRUNCATE TABLE compliance_breaches;

ALTER TABLE performance_records AUTO_INCREMENT = 1;
ALTER TABLE risk_measures       AUTO_INCREMENT = 1;
ALTER TABLE compliance_breaches AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ── 1. Performance Records ────────────────────────────────────
-- account_id → pbor.account.account_id
-- portfolio_id → same as account_id (no separate portfolio entity)
INSERT INTO performance_records (record_id, account_id, portfolio_id, period, start_date, end_date, return_percentage, benchmark_return_percentage, calculated_at) VALUES
(1,  1, 1, 'DAILY',   '2025-03-31', '2025-03-31',  2.82,  2.10, '2025-03-31 18:00:00'),
(2,  2, 2, 'DAILY',   '2025-03-31', '2025-03-31',  1.95,  2.10, '2025-03-31 18:00:00'),
(3,  3, 3, 'DAILY',   '2025-03-31', '2025-03-31', -0.45,  2.10, '2025-03-31 18:00:00'),
(4,  4, 4, 'DAILY',   '2025-03-31', '2025-03-31',  3.15,  2.10, '2025-03-31 18:00:00'),
(5,  5, 5, 'DAILY',   '2025-03-31', '2025-03-31',  1.60,  2.10, '2025-03-31 18:00:00'),
(6,  1, 1, 'MONTHLY', '2025-03-01', '2025-03-31',  8.50,  7.20, '2025-03-31 18:30:00'),
(7,  2, 2, 'MONTHLY', '2025-03-01', '2025-03-31',  6.75,  7.20, '2025-03-31 18:30:00'),
(8,  3, 3, 'MONTHLY', '2025-03-01', '2025-03-31', -1.20,  7.20, '2025-03-31 18:30:00'),
(9,  4, 4, 'MONTHLY', '2025-03-01', '2025-03-31', 10.30,  7.20, '2025-03-31 18:30:00'),
(10, 5, 5, 'MONTHLY', '2025-03-01', '2025-03-31',  5.90,  7.20, '2025-03-31 18:30:00');

-- ── 2. Risk Measures ──────────────────────────────────────────
INSERT INTO risk_measures (measure_id, account_id, measure_type, measure_value, description, calculated_at) VALUES
(1,  1, 'VOLATILITY',      18.55, 'Annualised portfolio volatility based on 30-day daily returns',        '2025-03-31 18:00:00'),
(2,  1, 'MAX_DRAWDOWN',   -12.30, 'Maximum peak-to-trough drawdown over last 12 months',                 '2025-03-31 18:00:00'),
(3,  1, 'VAR_95',           2.85, '95% Value-at-Risk — 1-day loss not exceeding this at 95% confidence', '2025-03-31 18:00:00'),
(4,  1, 'TRACKING_ERROR',   1.42, 'Tracking error vs Nifty 50 benchmark over 6 months',                  '2025-03-31 18:00:00'),
(5,  2, 'VOLATILITY',      22.10, 'Higher volatility due to aggressive equity exposure',                  '2025-03-31 18:00:00'),
(6,  2, 'MAX_DRAWDOWN',   -18.75, 'Max drawdown for aggressive profile over 12 months',                  '2025-03-31 18:00:00'),
(7,  3, 'VOLATILITY',       8.90, 'Low volatility — conservative client with bond-heavy portfolio',       '2025-03-31 18:00:00'),
(8,  3, 'VAR_95',           1.20, '95% VaR for conservative portfolio',                                  '2025-03-31 18:00:00'),
(9,  4, 'VOLATILITY',      25.40, 'UHNI aggressive trust account — high volatility',                     '2025-03-31 18:00:00'),
(10, 5, 'TRACKING_ERROR',   0.85, 'Low tracking error — ETF-heavy balanced portfolio',                   '2025-03-31 18:00:00');

-- ── 3. Compliance Breaches ────────────────────────────────────
-- rule_id → kyc.SuitabilityRule.RuleID
INSERT INTO compliance_breaches (breach_id, account_id, rule_id, severity, description, status, detected_at, resolved_at) VALUES
(1,  3, 1, 'HIGH',     'Conservative client (Anjali Singh) placed BUY order on EQUITY — suitability breach',          'CLOSED',       '2025-01-21 10:00:00', '2025-01-22 09:00:00'),
(2,  4, 6, 'MEDIUM',   'UHNI client (Vikram Patel) single asset exposure exceeds 50% threshold in HDFCBANK',         'ACKNOWLEDGED', '2025-02-15 14:00:00', NULL),
(3,  2, 5, 'LOW',      'HNI client (Rohan Verma) investment value below 1 lakh minimum for Tata Steel',             'CLOSED',       '2025-03-01 10:00:00', '2025-03-02 11:00:00'),
(4,  1, 4, 'MEDIUM',   'Cash balance dropped below required buffer after HDFCBANK purchase',                         'OPEN',         '2025-01-15 10:30:00', NULL),
(5,  5, 3, 'LOW',      'ETF exposure for Deepa Nambiar approaching 10000 unit concentration limit',                  'OPEN',         '2025-02-08 11:00:00', NULL),
(6,  8, 6, 'HIGH',     'UHNI client (Rahul Gupta) bond concentration exceeds 60% of total portfolio value',          'ACKNOWLEDGED', '2025-02-20 09:00:00', NULL),
(7,  6, 5, 'LOW',      'Retail client (Arjun Kapoor) minimum HNI investment rule triggered in error — rule mismatch','CLOSED',       '2025-03-05 15:00:00', '2025-03-06 10:00:00'),
(8,  7, 3, 'CRITICAL', 'Joint account (Sneha Reddy) total TATASTEEL exposure exceeds 10000 unit hard limit',         'OPEN',         '2025-03-10 13:00:00', NULL),
(9,  4, 10,'HIGH',     'Trust account (Vikram Patel) holds STRUCTURED product — restricted security alert',          'ACKNOWLEDGED', '2025-03-15 09:00:00', NULL),
(10, 9, 1, 'MEDIUM',   'Inactive account (Meera Joshi) attempted order on Conservative-restricted EQUITY',           'CLOSED',       '2025-03-20 10:00:00', '2025-03-20 14:00:00');

-- ── Verify ────────────────────────────────────────────────────
SELECT 'performance_records'  AS tbl, COUNT(*) AS row_count FROM performance_records
UNION ALL
SELECT 'risk_measures',       COUNT(*) FROM risk_measures
UNION ALL
SELECT 'compliance_breaches', COUNT(*) FROM compliance_breaches;
-- ============================================================
