-- ============================================================
--  WealthPro — Product Catalog Database Seed Data
--  Database : productcatalog
--  Updated  : prices refreshed to show realistic P&L across all client portfolios
-- ============================================================

USE productcatalog;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE research_notes;
TRUNCATE TABLE product_terms;
TRUNCATE TABLE securities;

ALTER TABLE securities     AUTO_INCREMENT = 1;
ALTER TABLE product_terms  AUTO_INCREMENT = 1;
ALTER TABLE research_notes AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ── 1. Securities ─────────────────────────────────────────────
-- current_price is last known market price for display.
-- Mutual fund NAV is now set so frontend can compute P&L for MF holdings.
-- Prices reflect a simulated market snapshot with realistic up/down movements.
INSERT INTO securities (security_id, symbol, asset_class, currency, country, status, current_price) VALUES
(1,  'HDFCBANK',    'EQUITY',       'INR', 'India', 'ACTIVE',    1842.50),   -- was 1648.75 → +11.7%
(2,  'RELIANCE',    'EQUITY',       'INR', 'India', 'ACTIVE',    2780.00),   -- was 2923.40 → -4.9%
(3,  'SBIBLUECHIP', 'MUTUAL_FUND',  'INR', 'India', 'ACTIVE',      61.20),   -- NAV was ~52 → +16%
(4,  'TATASTEEL',   'EQUITY',       'INR', 'India', 'ACTIVE',     156.80),   -- was 142.35 → +10.1%
(5,  'ICICIBOND28', 'BOND',         'INR', 'India', 'ACTIVE',    1015.00),   -- was 1023.50 → -0.8%
(6,  'INFY',        'EQUITY',       'INR', 'India', 'ACTIVE',    1650.00),   -- was 1587.20 → +4.0%
(7,  'NIFTYBEES',   'ETF',          'INR', 'India', 'ACTIVE',     271.50),   -- was 237.80 → +14.2%
(8,  'KOTAKGOLD',   'MUTUAL_FUND',  'INR', 'India', 'ACTIVE',      58.90),   -- NAV was ~48 → +22.7%
(9,  'LTFIN2027',   'BOND',         'INR', 'India', 'ACTIVE',    1002.50),   -- was 998.00 → +0.5%
(10, 'HDFCSTRUCT',  'STRUCTURED',   'INR', 'India', 'SUSPENDED',    NULL);   -- suspended, no price

-- ── 2. Product Terms ──────────────────────────────────────────
-- Full regulatory and financial terms per asset class.
-- Matches the live productcatalog.product_terms table exactly.
INSERT INTO product_terms (term_id, security_id, term_json, effective_from, effective_to) VALUES

-- Equity terms (HDFCBANK, RELIANCE, TATASTEEL, INFY)
(1,  1,  '{"termType":"Equity","exchange":"NSE / BSE","lotSize":1,"settlementCycle":"T+2","circuitFilter":"20%","marginRequirement":"15%","dividendEligible":"Yes","votingRights":"Yes","faceValue":"INR 1","sectorClassification":"Banking & Financial Services","regulatoryBody":"SEBI"}',
 '2024-01-01', NULL),

(2,  2,  '{"termType":"Equity","exchange":"NSE / BSE","lotSize":1,"settlementCycle":"T+2","circuitFilter":"20%","marginRequirement":"15%","dividendEligible":"Yes","votingRights":"Yes","faceValue":"INR 10","sectorClassification":"Oil & Gas / Conglomerate","regulatoryBody":"SEBI"}',
 '2024-01-01', NULL),

-- Mutual Fund terms (SBIBLUECHIP)
(3,  3,  '{"termType":"Mutual Fund","category":"Large Cap Fund","minInvestmentLumpsum":"INR 5,000","minInvestmentSIP":"INR 500","exitLoad":"1% if redeemed within 1 year","expenseRatio":"0.98%","lockInPeriod":"None","navSettlement":"T+3 business days","fundHouse":"SBI Funds Management","benchmark":"S&P BSE 100 TRI","dividendOption":"Available","riskRating":"Moderately High","regulatoryBody":"SEBI / AMFI"}',
 '2024-01-01', NULL),

-- Equity terms (TATASTEEL)
(4,  4,  '{"termType":"Equity","exchange":"NSE / BSE","lotSize":1,"settlementCycle":"T+2","circuitFilter":"20%","marginRequirement":"20%","dividendEligible":"Yes","votingRights":"Yes","faceValue":"INR 1","sectorClassification":"Metals & Mining","regulatoryBody":"SEBI"}',
 '2024-01-01', NULL),

-- Bond terms (ICICIBOND28)
(5,  5,  '{"termType":"Bond","issuer":"ICICI Bank Ltd","faceValue":"INR 1,000","couponRate":"7.85% per annum","couponFrequency":"Semi-annual","maturityDate":"2028-03-15","settlementCycle":"T+1","creditRating":"AAA (CRISIL / ICRA)","yieldToMaturity":"7.92%","callOption":"None","putOption":"None","dayCountConvention":"Actual/Actual","taxability":"Interest taxable as income","regulatoryBody":"SEBI / RBI"}',
 '2024-01-01', NULL),

-- Equity terms (INFY)
(6,  6,  '{"termType":"Equity","exchange":"NSE / BSE","lotSize":1,"settlementCycle":"T+2","circuitFilter":"20%","marginRequirement":"12%","dividendEligible":"Yes","votingRights":"Yes","faceValue":"INR 5","sectorClassification":"Information Technology","regulatoryBody":"SEBI"}',
 '2024-01-01', NULL),

-- ETF terms (NIFTYBEES)
(7,  7,  '{"termType":"ETF","underlying":"NIFTY 50 Index","exchange":"NSE","lotSize":1,"expenseRatio":"0.04%","settlementCycle":"T+2","trackingError":"0.02%","dividendPolicy":"Dividend payout","replicationMethod":"Physical (Full)","benchmark":"NIFTY 50 TRI","fundHouse":"Nippon India Mutual Fund","regulatoryBody":"SEBI / AMFI"}',
 '2024-01-01', NULL),

-- Mutual Fund terms (KOTAKGOLD)
(8,  8,  '{"termType":"Mutual Fund","category":"Gold Fund of Fund","minInvestmentLumpsum":"INR 5,000","minInvestmentSIP":"INR 1,000","exitLoad":"1% if redeemed within 1 year","expenseRatio":"0.19%","lockInPeriod":"None","navSettlement":"T+3 business days","fundHouse":"Kotak Mahindra Asset Management","benchmark":"Domestic Price of Gold","dividendOption":"Not Available","riskRating":"High","regulatoryBody":"SEBI / AMFI"}',
 '2024-01-01', NULL),

-- Bond terms (LTFIN2027)
(9,  9,  '{"termType":"Bond","issuer":"L&T Finance Holdings Ltd","faceValue":"INR 1,000","couponRate":"8.10% per annum","couponFrequency":"Annual","maturityDate":"2027-09-30","settlementCycle":"T+1","creditRating":"AA+ (CRISIL)","yieldToMaturity":"8.18%","callOption":"None","putOption":"None","dayCountConvention":"Actual/365","taxability":"Interest taxable as income; no TDS for demat bonds","regulatoryBody":"SEBI / RBI"}',
 '2024-01-01', NULL),

-- Structured Product terms (HDFCSTRUCT)
(10, 10, '{"termType":"Structured Product","issuer":"HDFC Bank Ltd","minInvestment":"INR 5,00,000","tenure":"3 years","principalProtection":"100% at maturity","returnType":"NIFTY 50 linked participation","participationRate":"70%","maxReturn":"Uncapped (participation rate applies)","earlyExitPenalty":"2% of NAV if exited before maturity","settlementCycle":"T+3 business days","creditRating":"AAA (HDFC Bank)","regulatoryBody":"SEBI","targetInvestor":"Sophisticated / High Net Worth Investors only"}',
 '2024-01-01', NULL);

-- ── 3. Research Notes ─────────────────────────────────────────
INSERT INTO research_notes (note_id, security_id, title, rating, published_date, content_uri) VALUES
(1,  1,  'HDFC Bank Q3 FY25 Results — Strong NIM Expansion',           'BUY',  '2025-01-20', '/research/hdfcbank_q3fy25.pdf'),
(2,  2,  'Reliance Industries — Jio Financial Services Valuation',     'HOLD', '2025-01-25', '/research/reliance_jiofinancial.pdf'),
(3,  3,  'SBI Bluechip Fund — 5-Year CAGR Analysis',                  'BUY',  '2025-02-01', '/research/sbibluechip_5yr.pdf'),
(4,  4,  'Tata Steel — Global Steel Demand Recovery Outlook',          'HOLD', '2025-02-10', '/research/tatasteel_q3fy25.pdf'),
(5,  5,  'ICICI 2028 Bond — Credit Quality & Yield Assessment',        'BUY',  '2025-02-15', '/research/icicibond2028_credit.pdf'),
(6,  6,  'Infosys Q3 FY25 — AI Revenue Growth Acceleration',          'BUY',  '2025-02-20', '/research/infy_q3fy25.pdf'),
(7,  7,  'Nifty BeES ETF — Benchmark Tracking & Expense Ratio',       'BUY',  '2025-03-01', '/research/niftybees_analysis.pdf'),
(8,  8,  'Kotak Gold Fund — Geopolitical Hedge Strategy',              'HOLD', '2025-03-05', '/research/kotakgold_geo.pdf'),
(9,  9,  'L&T Finance 2027 Bond — Infrastructure Sector Outlook',     'BUY',  '2025-03-10', '/research/ltfin2027_outlook.pdf'),
(10, 10, 'HDFC Structured Product — Capital Protection Review',        'SELL', '2025-03-15', '/research/hdfcstruct_review.pdf');

-- ── Verify ────────────────────────────────────────────────────
SELECT 'securities'     AS tbl, COUNT(*) AS row_count FROM securities
UNION ALL
SELECT 'product_terms', COUNT(*) FROM product_terms
UNION ALL
SELECT 'research_notes',COUNT(*) FROM research_notes;
-- ============================================================
