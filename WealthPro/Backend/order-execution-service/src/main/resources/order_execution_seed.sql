-- ============================================================
--  WealthPro — Order Execution Database Seed Data
--  Database : order_execution_db
-- ============================================================

USE order_execution_db;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE allocations;
TRUNCATE TABLE execution_fills;
TRUNCATE TABLE pre_trade_checks;
TRUNCATE TABLE orders;

ALTER TABLE orders          AUTO_INCREMENT = 1;
ALTER TABLE pre_trade_checks AUTO_INCREMENT = 1;
ALTER TABLE execution_fills AUTO_INCREMENT = 1;
ALTER TABLE allocations     AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ── 1. Orders ─────────────────────────────────────────────────
-- client_id → kyc.Client.ClientID
-- security_id → productcatalog.securities.security_id
INSERT INTO orders (order_id, client_id, security_id, side, quantity, price_type, limit_price, order_date, status, routed_venue) VALUES
(1,  1, 1,  'BUY',       100, 'LIMIT',  1650.00, '2025-01-14 10:15:00', 'FILLED',           'SIMULATED_BSE'),
(2,  2, 3,  'SUBSCRIBE', 500, 'NAV',    NULL,    '2025-01-17 11:00:00', 'FILLED',           'SIMULATED_MF_PLATFORM'),
(3,  3, 6,  'BUY',        50, 'MARKET', NULL,    '2025-01-21 09:45:00', 'FILLED',           'SIMULATED_NSE'),
(4,  4, 5,  'BUY',       200, 'LIMIT',   950.00, '2025-01-31 14:30:00', 'FILLED',           'SIMULATED_BSE'),
(5,  5, 7,  'BUY',       300, 'LIMIT',   215.00, '2025-02-08 10:00:00', 'FILLED',           'SIMULATED_BSE'),
(6,  1, 2,  'BUY',        75, 'LIMIT',  2905.00, '2025-02-20 11:30:00', 'VALIDATED',        NULL),
(7,  2, 4,  'SELL',      100, 'MARKET', NULL,    '2025-03-01 09:30:00', 'ROUTED',           'SIMULATED_NSE'),
(8,  3, 8,  'SUBSCRIBE',1000, 'NAV',    NULL,    '2025-03-10 10:00:00', 'PLACED',           NULL),
(9,  4, 9,  'BUY',       150, 'LIMIT',   145.00, '2025-03-15 13:00:00', 'REJECTED',         NULL),
(10, 5, 1,  'BUY',        50, 'LIMIT',  1710.00, '2025-03-20 15:00:00', 'CANCELLED',        NULL);

-- ── 2. Pre-Trade Checks ───────────────────────────────────────
-- Orders 1-5: All PASS (FILLED)
-- Order 6: All PASS (VALIDATED)
-- Order 7: All PASS (ROUTED)
-- Order 9: CASH FAIL (REJECTED)
INSERT INTO pre_trade_checks (check_id, order_id, check_type, result, message, checked_date) VALUES
-- Order 1 (FILLED)
(1,  1, 'SUITABILITY', 'PASS', 'Client risk class BALANCED — EQUITY BUY allowed',            '2025-01-14 10:15:05'),
(2,  1, 'LIMIT',       'PASS', 'Quantity 100 within limit of 10000',                         '2025-01-14 10:15:05'),
(3,  1, 'EXPOSURE',    'PASS', 'Total exposure 100 within max 10000',                        '2025-01-14 10:15:05'),
(4,  1, 'CASH',        'PASS', 'Cash balance sufficient: 200000 >= 165000',                  '2025-01-14 10:15:05'),
-- Order 2 (FILLED)
(5,  2, 'SUITABILITY', 'PASS', 'Client risk class AGGRESSIVE — MUTUAL_FUND SUBSCRIBE allowed','2025-01-17 11:00:05'),
(6,  2, 'LIMIT',       'PASS', 'Quantity 500 within limit of 10000',                         '2025-01-17 11:00:05'),
(7,  2, 'EXPOSURE',    'PASS', 'Total exposure 500 within max 10000',                        '2025-01-17 11:00:05'),
(8,  2, 'CASH',        'PASS', 'Cash balance sufficient for NAV-based subscription',         '2025-01-17 11:00:05'),
-- Order 3 (FILLED)
(9,  3, 'SUITABILITY', 'PASS', 'Client risk class CONSERVATIVE — MARKET order EQUITY',       '2025-01-21 09:45:05'),
(10, 3, 'LIMIT',       'PASS', 'Quantity 50 within limit of 10000',                          '2025-01-21 09:45:05'),
(11, 3, 'EXPOSURE',    'PASS', 'Total exposure 50 within max 10000',                         '2025-01-21 09:45:05'),
(12, 3, 'CASH',        'PASS', 'Cash balance 150000 >= estimated 96000',                     '2025-01-21 09:45:05'),
-- Order 4 (FILLED)
(13, 4, 'SUITABILITY', 'PASS', 'Client risk class AGGRESSIVE — BOND BUY allowed',            '2025-01-31 14:30:05'),
(14, 4, 'LIMIT',       'PASS', 'Quantity 200 within limit of 10000',                         '2025-01-31 14:30:05'),
(15, 4, 'EXPOSURE',    'PASS', 'Total exposure 200 within max 10000',                        '2025-01-31 14:30:05'),
(16, 4, 'CASH',        'PASS', 'Cash balance 500000 >= 190000',                              '2025-01-31 14:30:05'),
-- Order 5 (FILLED)
(17, 5, 'SUITABILITY', 'PASS', 'Client risk class BALANCED — ETF BUY allowed',               '2025-02-08 10:00:05'),
(18, 5, 'LIMIT',       'PASS', 'Quantity 300 within limit of 10000',                         '2025-02-08 10:00:05'),
(19, 5, 'EXPOSURE',    'PASS', 'Total exposure 300 within max 10000',                        '2025-02-08 10:00:05'),
(20, 5, 'CASH',        'PASS', 'Cash balance 100000 >= 64500',                               '2025-02-08 10:00:05'),
-- Order 6 (VALIDATED)
(21, 6, 'SUITABILITY', 'PASS', 'Client risk class BALANCED — EQUITY BUY allowed',            '2025-02-20 11:30:05'),
(22, 6, 'LIMIT',       'PASS', 'Quantity 75 within limit of 10000',                          '2025-02-20 11:30:05'),
(23, 6, 'EXPOSURE',    'PASS', 'Existing 0 + new 75 = 75 within max 10000',                  '2025-02-20 11:30:05'),
(24, 6, 'CASH',        'PASS', 'Cash balance 50000 >= 217875',                               '2025-02-20 11:30:05'),
-- Order 7 (ROUTED)
(25, 7, 'SUITABILITY', 'PASS', 'Client risk class AGGRESSIVE — SELL allowed',                '2025-03-01 09:30:05'),
(26, 7, 'LIMIT',       'PASS', 'Quantity 100 within limit of 10000',                         '2025-03-01 09:30:05'),
(27, 7, 'EXPOSURE',    'PASS', 'Sell order — exposure check not applicable',                 '2025-03-01 09:30:05'),
(28, 7, 'CASH',        'PASS', 'Sell order — cash check not applicable',                     '2025-03-01 09:30:05'),
-- Order 9 (REJECTED — CASH FAIL)
(29, 9, 'SUITABILITY', 'PASS', 'Client risk class AGGRESSIVE — BOND BUY allowed',            '2025-03-15 13:00:05'),
(30, 9, 'LIMIT',       'PASS', 'Quantity 150 within limit of 10000',                         '2025-03-15 13:00:05'),
(31, 9, 'EXPOSURE',    'PASS', 'Total exposure 150 within max 10000',                        '2025-03-15 13:00:05'),
(32, 9, 'CASH',        'FAIL', 'Insufficient cash: balance 5000 < required 21750',           '2025-03-15 13:00:05');

-- ── 3. Execution Fills ────────────────────────────────────────
-- Only for FILLED orders (1-5) and ROUTED (7 - partial fill example)
INSERT INTO execution_fills (fill_id, order_id, fill_quantity, fill_price, fill_date, venue, status) VALUES
(1, 1, 100, 1648.50, '2025-01-14 10:20:00', 'SIMULATED_BSE',         'COMPLETED'),
(2, 2, 500,   52.75, '2025-01-17 15:00:00', 'SIMULATED_MF_PLATFORM', 'COMPLETED'),
(3, 3,  50, 1918.00, '2025-01-21 09:50:00', 'SIMULATED_NSE',         'COMPLETED'),
(4, 4, 200,  948.50, '2025-01-31 14:35:00', 'SIMULATED_BSE',         'COMPLETED'),
(5, 5, 300,  213.25, '2025-02-08 10:05:00', 'SIMULATED_BSE',         'COMPLETED'),
(6, 7,  60, 2910.00, '2025-03-01 09:35:00', 'SIMULATED_NSE',         'COMPLETED'),
(7, 7,  40, 2912.50, '2025-03-01 09:38:00', 'SIMULATED_NSE',         'COMPLETED');

-- ── 4. Allocations ────────────────────────────────────────────
-- account_id → pbor.account.account_id
INSERT INTO allocations (allocation_id, order_id, account_id, alloc_quantity, alloc_price, alloc_date) VALUES
(1, 1, 1, 100, 1648.50, '2025-01-14 10:25:00'),
(2, 2, 2, 500,   52.75, '2025-01-17 15:05:00'),
(3, 3, 3,  50, 1918.00, '2025-01-21 09:55:00'),
(4, 4, 4, 200,  948.50, '2025-01-31 14:40:00'),
(5, 5, 5, 300,  213.25, '2025-02-08 10:10:00'),
(6, 7, 2, 100, 2911.00, '2025-03-01 09:40:00');

-- ── Verify ────────────────────────────────────────────────────
SELECT 'orders'           AS tbl, COUNT(*) AS row_count FROM orders
UNION ALL
SELECT 'pre_trade_checks',COUNT(*) FROM pre_trade_checks
UNION ALL
SELECT 'execution_fills', COUNT(*) FROM execution_fills
UNION ALL
SELECT 'allocations',     COUNT(*) FROM allocations;
-- ============================================================
