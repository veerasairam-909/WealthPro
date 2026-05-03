-- ============================================================
--  WealthPro — Review Database Seed Data
--  Database : wealthpro_review
-- ============================================================

USE wealthpro_review;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE statements;
TRUNCATE TABLE reviews;

ALTER TABLE reviews    AUTO_INCREMENT = 1;
ALTER TABLE statements AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ── 1. Reviews ────────────────────────────────────────────────
-- account_id → pbor.account.account_id
INSERT INTO reviews (review_id, account_id, period_start, period_end, period_type, highlights_json, reviewed_by, review_date, status) VALUES
(1,  1, '2025-01-01', '2025-03-31', 'QUARTERLY', '{"portfolioReturn":8.5,"benchmarkReturn":7.2,"topHolding":"HDFCBANK","totalValue":185000,"risksIdentified":1,"actionItems":["Rebalance equity exposure","Review cash allocation"]}',                         'rm1',       '2025-04-05', 'COMPLETED'),
(2,  2, '2025-01-01', '2025-03-31', 'QUARTERLY', '{"portfolioReturn":6.75,"benchmarkReturn":7.2,"topHolding":"SBIBLUECHIP","totalValue":320000,"risksIdentified":0,"actionItems":["Increase equity allocation","Explore NPS option"]}',                        'rm1',       '2025-04-06', 'COMPLETED'),
(3,  3, '2025-01-01', '2025-03-31', 'QUARTERLY', '{"portfolioReturn":-1.2,"benchmarkReturn":7.2,"topHolding":"INFY","totalValue":90000,"risksIdentified":2,"actionItems":["Review conservative allocation","Address suitability breach","Consider SIP"]}',     'rm1',       '2025-04-07', 'COMPLETED'),
(4,  4, '2025-01-01', '2025-03-31', 'QUARTERLY', '{"portfolioReturn":10.3,"benchmarkReturn":7.2,"topHolding":"HDFCBANK","totalValue":850000,"risksIdentified":1,"actionItems":["Monitor UHNI concentration","Review structured exposure"]}',                   'rm1',       '2025-04-08', 'IN_PROGRESS'),
(5,  5, '2025-01-01', '2025-03-31', 'QUARTERLY', '{"portfolioReturn":5.9,"benchmarkReturn":7.2,"topHolding":"NIFTYBEES","totalValue":125000,"risksIdentified":0,"actionItems":["Add debt allocation","Review ETF tracking error"]}',                           'rm2',       '2025-04-09', 'COMPLETED'),
(6,  6, '2025-01-01', '2025-03-31', 'QUARTERLY', '{"portfolioReturn":7.8,"benchmarkReturn":7.2,"topHolding":"RELIANCE","totalValue":95000,"risksIdentified":0,"actionItems":["Diversify into MF","Consider goal-based investment"]}',                          'rm2',       '2025-04-10', 'SCHEDULED'),
(7,  7, '2024-10-01', '2024-12-31', 'QUARTERLY', '{"portfolioReturn":9.1,"benchmarkReturn":8.0,"topHolding":"TATASTEEL","totalValue":210000,"risksIdentified":1,"actionItems":["Joint account rebalancing","Reduce steel sector exposure"]}',                   'rm2',       '2025-01-10', 'COMPLETED'),
(8,  8, '2024-10-01', '2024-12-31', 'QUARTERLY', '{"portfolioReturn":11.5,"benchmarkReturn":8.0,"topHolding":"KOTAKGOLD","totalValue":1200000,"risksIdentified":2,"actionItems":["Reduce bond concentration","Add global diversification","Review compliance"]}','rm2',       '2025-01-12', 'COMPLETED'),
(9,  1, '2024-07-01', '2024-09-30', 'QUARTERLY', '{"portfolioReturn":4.2,"benchmarkReturn":5.5,"topHolding":"HDFCBANK","totalValue":165000,"risksIdentified":0,"actionItems":["Increase SIP amount","Review goal timeline"]}',                                  'rm1',       '2024-10-05', 'COMPLETED'),
(10, 2, '2025-01-01', '2025-01-31', 'MONTHLY',   '{"portfolioReturn":1.95,"benchmarkReturn":2.10,"topHolding":"SBIBLUECHIP","totalValue":305000,"risksIdentified":0,"actionItems":["Monthly tracking on track","No action required"]}',                        'rm1',       '2025-02-03', 'COMPLETED');

-- ── 2. Statements ─────────────────────────────────────────────
INSERT INTO statements (statement_id, account_id, period_start, period_end, period_type, generated_date, summary_json, status) VALUES
(1,  1, '2025-01-01', '2025-03-31', 'QUARTERLY', '2025-04-01', '{"openingBalance":165000,"closingBalance":187500,"totalGain":22500,"dividendReceived":1500,"feesPaid":0,"holdings":[{"security":"HDFCBANK","qty":100,"value":185000}]}',         'DELIVERED'),
(2,  2, '2025-01-01', '2025-03-31', 'QUARTERLY', '2025-04-01', '{"openingBalance":26375,"closingBalance":28100,"totalGain":1725,"dividendReceived":875,"feesPaid":0,"holdings":[{"security":"SBIBLUECHIP","qty":500,"value":27225}]}',           'DELIVERED'),
(3,  3, '2025-01-01', '2025-03-31', 'QUARTERLY', '2025-04-01', '{"openingBalance":95900,"closingBalance":94800,"totalGain":-1100,"dividendReceived":0,"feesPaid":0,"holdings":[{"security":"INFY","qty":50,"value":94800}]}',                   'DELIVERED'),
(4,  4, '2025-01-01', '2025-03-31', 'QUARTERLY', '2025-04-01', '{"openingBalance":379300,"closingBalance":419000,"totalGain":39700,"dividendReceived":0,"feesPaid":2500,"holdings":[{"security":"ICICIBOND28","qty":200,"value":191000},{"security":"HDFCBANK","qty":250,"value":231000}]}','GENERATED'),
(5,  5, '2025-01-01', '2025-03-31', 'QUARTERLY', '2025-04-01', '{"openingBalance":63975,"closingBalance":67800,"totalGain":3825,"dividendReceived":0,"feesPaid":0,"holdings":[{"security":"NIFTYBEES","qty":300,"value":67800}]}',              'DELIVERED'),
(6,  6, '2025-01-01', '2025-03-31', 'QUARTERLY', '2025-04-01', '{"openingBalance":86250,"closingBalance":93000,"totalGain":6750,"dividendReceived":0,"feesPaid":0,"holdings":[{"security":"RELIANCE","qty":30,"value":93000}]}',               'GENERATED'),
(7,  7, '2024-10-01', '2024-12-31', 'QUARTERLY', '2025-01-02', '{"openingBalance":18500,"closingBalance":20175,"totalGain":1675,"dividendReceived":0,"feesPaid":0,"holdings":[{"security":"TATASTEEL","qty":150,"value":20175}]}',              'DELIVERED'),
(8,  8, '2024-10-01', '2024-12-31', 'QUARTERLY', '2025-01-02', '{"openingBalance":9500,"closingBalance":10400,"totalGain":900,"dividendReceived":0,"feesPaid":0,"holdings":[{"security":"KOTAKGOLD","qty":200,"value":10400}]}',               'DELIVERED'),
(9,  1, '2025-01-01', '2025-01-31', 'MONTHLY',   '2025-02-01', '{"openingBalance":165000,"closingBalance":168200,"totalGain":3200,"dividendReceived":1500,"feesPaid":0,"holdings":[{"security":"HDFCBANK","qty":100,"value":168200}]}',         'DELIVERED'),
(10, 4, '2025-01-01', '2025-01-31', 'MONTHLY',   '2025-02-01', '{"openingBalance":379300,"closingBalance":385000,"totalGain":5700,"dividendReceived":0,"feesPaid":0,"holdings":[{"security":"ICICIBOND28","qty":200},{"security":"HDFCBANK","qty":250}]}','PENDING');

-- ── Verify ────────────────────────────────────────────────────
SELECT 'reviews'    AS tbl, COUNT(*) AS row_count FROM reviews
UNION ALL
SELECT 'statements',COUNT(*) FROM statements;
-- ============================================================
