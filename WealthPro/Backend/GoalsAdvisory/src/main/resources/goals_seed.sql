-- ============================================================
--  WealthPro — Goals Advisory Database Seed Data
--  Database : wealth
-- ============================================================

USE wealth;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE recommendations;
TRUNCATE TABLE goals;
TRUNCATE TABLE model_portfolios;

ALTER TABLE goals            AUTO_INCREMENT = 1;
ALTER TABLE model_portfolios AUTO_INCREMENT = 1;
ALTER TABLE recommendations  AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ── 1. Model Portfolios ───────────────────────────────────────
-- Conservative portfolios: BOND, MUTUAL_FUND, ETF only — NO EQUITY, NO STRUCTURED
--   (Suitability Rule 1: Conservative clients cannot buy EQUITY)
-- Balanced portfolios: EQUITY allowed — NO STRUCTURED
--   (Suitability Rule 6: STRUCTURED restricted to UHNI segment only)
-- Aggressive portfolios: EQUITY allowed — STRUCTURED only in UHNI-targeted funds
INSERT INTO model_portfolios (model_id, name, risk_class, weights_json, status) VALUES
(1,  'Conservative Shield',   'CONSERVATIVE', '{"BOND":60,"MUTUAL_FUND":25,"ETF":15}',                         'ACTIVE'),
(2,  'Balanced Growth',       'BALANCED',     '{"EQUITY":40,"BOND":30,"MUTUAL_FUND":20,"ETF":10}',             'ACTIVE'),
(3,  'Aggressive Alpha',      'AGGRESSIVE',   '{"EQUITY":70,"ETF":15,"MUTUAL_FUND":10,"BOND":5}',              'ACTIVE'),
(4,  'HNI Wealth Builder',    'BALANCED',     '{"EQUITY":45,"BOND":25,"MUTUAL_FUND":20,"ETF":10}',             'ACTIVE'),
(5,  'Retirement Cornerstone','CONSERVATIVE', '{"BOND":65,"MUTUAL_FUND":25,"ETF":10}',                         'ACTIVE'),
(6,  'UHNI Alpha Plus',       'AGGRESSIVE',   '{"EQUITY":65,"STRUCTURED":15,"ETF":12,"MUTUAL_FUND":8}',        'ACTIVE'),
(7,  'Income Plus',           'CONSERVATIVE', '{"BOND":70,"MUTUAL_FUND":20,"ETF":10}',                         'ACTIVE'),
(8,  'Blue Chip Select',      'BALANCED',     '{"EQUITY":50,"BOND":25,"MUTUAL_FUND":15,"ETF":10}',             'ACTIVE'),
(9,  'Multi Asset Dynamic',   'BALANCED',     '{"EQUITY":40,"BOND":25,"MUTUAL_FUND":25,"ETF":10}',             'ACTIVE'),
(10, 'Capital Protect Plus',  'CONSERVATIVE', '{"BOND":72,"MUTUAL_FUND":20,"ETF":8}',                          'INACTIVE');

-- ── 2. Goals ──────────────────────────────────────────────────
-- client_id → kyc.Client.ClientID
INSERT INTO goals (goal_id, client_id, goal_type, target_amount, target_date, priority, status) VALUES
(1,  1,  'RETIREMENT',  5000000.00, '2045-01-01', 1, 'ACTIVE'),
(2,  2,  'EDUCATION',   2000000.00, '2030-06-01', 1, 'IN_PROGRESS'),
(3,  3,  'WEALTH',      3000000.00, '2035-12-01', 2, 'ACTIVE'),
(4,  4,  'RETIREMENT', 10000000.00, '2040-01-01', 1, 'ACTIVE'),
(5,  5,  'CUSTOM',      1500000.00, '2028-03-01', 3, 'IN_PROGRESS'),
(6,  6,  'EDUCATION',   1800000.00, '2032-07-01', 1, 'ACTIVE'),
(7,  7,  'RETIREMENT',  8000000.00, '2042-01-01', 1, 'ACTIVE'),
(8,  8,  'WEALTH',     15000000.00, '2038-01-01', 2, 'IN_PROGRESS'),
(9,  9,  'CUSTOM',       500000.00, '2026-12-01', 3, 'CANCELLED'),
(10, 10, 'RETIREMENT',  6000000.00, '2043-01-01', 1, 'ACTIVE');

-- ── 3. Recommendations ───────────────────────────────────────
-- goal_id → goals.goal_id (nullable)
-- model_id → model_portfolios.model_id
INSERT INTO recommendations (reco_id, client_id, goal_id, model_id, proposal_json, proposed_date, status) VALUES
(1,  1,  1,  2,  '{"targetReturn":10.5,"timeHorizon":"20 years","SIP":25000,"allocation":{"EQUITY":40,"BOND":30,"MUTUAL_FUND":20,"ETF":10}}',                           '2025-01-15', 'APPROVED'),
(2,  2,  2,  4,  '{"targetReturn":12.0,"timeHorizon":"5 years","lumpsum":500000,"SIP":15000,"allocation":{"EQUITY":45,"BOND":25,"MUTUAL_FUND":20,"ETF":10}}',            '2025-01-20', 'APPROVED'),
(3,  3,  3,  2,  '{"targetReturn":9.0,"timeHorizon":"10 years","SIP":10000,"allocation":{"EQUITY":40,"BOND":30,"MUTUAL_FUND":20,"ETF":10}}',                             '2025-02-01', 'SUBMITTED'),
(4,  4,  4,  6,  '{"targetReturn":15.0,"timeHorizon":"15 years","lumpsum":2000000,"SIP":50000,"allocation":{"EQUITY":65,"STRUCTURED":15,"ETF":12,"MUTUAL_FUND":8}}',    '2025-02-05', 'APPROVED'),
(5,  5,  5,  2,  '{"targetReturn":11.0,"timeHorizon":"3 years","SIP":20000,"allocation":{"EQUITY":40,"BOND":30,"MUTUAL_FUND":20,"ETF":10}}',                             '2025-02-10', 'APPROVED'),
(6,  6,  6,  1,  '{"targetReturn":8.0,"timeHorizon":"7 years","SIP":8000,"allocation":{"BOND":60,"MUTUAL_FUND":25,"ETF":15}}',                                         '2025-02-15', 'SUBMITTED'),
(7,  7,  7,  4,  '{"targetReturn":11.5,"timeHorizon":"18 years","SIP":35000,"allocation":{"EQUITY":45,"BOND":25,"MUTUAL_FUND":20,"ETF":10}}',                            '2025-03-01', 'DRAFT'),
(8,  8,  8,  6,  '{"targetReturn":16.0,"timeHorizon":"13 years","lumpsum":5000000,"SIP":100000,"allocation":{"EQUITY":65,"STRUCTURED":15,"ETF":12,"MUTUAL_FUND":8}}',   '2025-03-05', 'APPROVED'),
(9,  9,  NULL,7, '{"targetReturn":7.5,"timeHorizon":"2 years","lumpsum":100000,"allocation":{"BOND":68,"MUTUAL_FUND":22,"ETF":10}}',                                    '2025-03-10', 'REJECTED'),
(10, 10, 10, 5,  '{"targetReturn":9.5,"timeHorizon":"18 years","SIP":30000,"allocation":{"BOND":65,"MUTUAL_FUND":25,"ETF":10}}',                                         '2025-03-15', 'DRAFT');

-- ── Verify ────────────────────────────────────────────────────
SELECT 'model_portfolios'  AS tbl, COUNT(*) AS row_count FROM model_portfolios
UNION ALL
SELECT 'goals',            COUNT(*) FROM goals
UNION ALL
SELECT 'recommendations',  COUNT(*) FROM recommendations;
-- ============================================================
