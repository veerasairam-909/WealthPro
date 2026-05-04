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
INSERT INTO securities (security_id, symbol, name, exchange, isin, asset_class, currency, country, status, current_price) VALUES
(1,  'HDFCBANK',    'HDFC Bank Ltd.',                              'NSE/BSE', 'INE040A01034', 'EQUITY',       'INR', 'India', 'ACTIVE',    1842.50),
(2,  'RELIANCE',    'Reliance Industries Ltd.',                    'NSE/BSE', 'INE002A01018', 'EQUITY',       'INR', 'India', 'ACTIVE',    2780.00),
(3,  'SBIBLUECHIP', 'SBI Blue Chip Fund',                          'AMFI',    'INF200K01RD2', 'MUTUAL_FUND',  'INR', 'India', 'ACTIVE',      61.20),
(4,  'TATASTEEL',   'Tata Steel Ltd.',                             'NSE/BSE', 'INE081A01020', 'EQUITY',       'INR', 'India', 'ACTIVE',     156.80),
(5,  'ICICIBOND28', 'ICICI Bank 7.85% Bond 2028',                  'BSE',     'INE090A08UB4', 'BOND',         'INR', 'India', 'ACTIVE',    1015.00),
(6,  'INFY',        'Infosys Ltd.',                                'NSE/BSE', 'INE009A01021', 'EQUITY',       'INR', 'India', 'ACTIVE',    1650.00),
(7,  'NIFTYBEES',   'Nippon India ETF Nifty BeES',                 'NSE',     'INF204KB13I2', 'ETF',          'INR', 'India', 'ACTIVE',     271.50),
(8,  'KOTAKGOLD',   'Kotak Gold Fund',                             'AMFI',    'INF174K01LS2', 'MUTUAL_FUND',  'INR', 'India', 'ACTIVE',      58.90),
(9,  'LTFIN2027',   'L&T Finance Holdings 8.10% Bond 2027',        'BSE',     'INE476A07HC2', 'BOND',         'INR', 'India', 'ACTIVE',    1002.50),
(10, 'HDFCSTRUCT',  'HDFC Bank Nifty-Linked Structured Note 2027', 'OTC',     'INE040A08536', 'STRUCTURED',   'INR', 'India', 'SUSPENDED',    NULL);

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
INSERT INTO research_notes (note_id, security_id, title, rating, published_date, content_uri, analyst, content) VALUES
(1,  1,  'HDFC Bank Q3 FY25 Results — Strong NIM Expansion',        'BUY',  '2025-01-20', '/research/hdfcbank_q3fy25.pdf',    'Priya Sharma',
 'HDFC Bank is one of the largest private sector banks in India. The bank reported strong results in Q3 FY25 with growth in its net interest income. It has a large branch network and a loyal customer base. The bank consistently maintains low bad loans compared to its peers. We recommend buying this stock for steady long-term growth.'),

(2,  2,  'Reliance Industries — Jio Financial Services Valuation',  'HOLD', '2025-01-25', '/research/reliance_jiofinancial.pdf', 'Arjun Mehta',
 'Reliance Industries is the largest company in India by market value. It operates in three main areas: oil refining, retail stores, and telecom through Jio. The stock is currently trading at fair value. Investors already holding this stock can continue to hold. New investors may wait for a lower price before buying.'),

(3,  3,  'SBI Bluechip Fund — 5-Year CAGR Analysis',               'BUY',  '2025-02-01', '/research/sbibluechip_5yr.pdf',    'Neha Kulkarni',
 'SBI Blue Chip Fund invests in large and well-known companies listed on Indian stock exchanges. Over the past 5 years it has delivered consistent returns. This fund is managed by SBI Funds Management and is regulated by SEBI. It is suitable for investors who want to grow their money steadily over 3 to 5 years. Investing through a monthly SIP is recommended.'),

(4,  4,  'Tata Steel — Global Steel Demand Recovery Outlook',       'HOLD', '2025-02-10', '/research/tatasteel_q3fy25.pdf',  'Rohit Verma',
 'Tata Steel is a major steel producer in India with operations in Europe as well. The company performs well when global steel prices are high. Steel demand is currently recovering but slowly. There is some uncertainty due to global trade conditions. Existing investors should hold their position and monitor steel price trends before making new investments.'),

(5,  5,  'ICICI 2028 Bond — Credit Quality & Yield Assessment',     'BUY',  '2025-02-15', '/research/icicibond2028_credit.pdf', 'Sneha Iyer',
 'This is a bond issued by ICICI Bank that matures in 2028. It pays a fixed interest of 7.85% per year in two installments. Bonds are safer than stocks because they give a fixed return. ICICI Bank is a very reliable issuer with the highest credit rating of AAA. This bond is suitable for conservative investors who want regular income with low risk.'),

(6,  6,  'Infosys Q3 FY25 — AI Revenue Growth Acceleration',       'BUY',  '2025-02-20', '/research/infy_q3fy25.pdf',        'Karan Patel',
 'Infosys is one of the top IT services companies in India. It earns most of its revenue by providing software and technology services to clients in the US and Europe. In recent quarters the company has won new projects in artificial intelligence. The management team is strong and the company regularly pays dividends. We recommend buying for long-term technology sector growth.'),

(7,  7,  'Nifty BeES ETF — Benchmark Tracking & Expense Ratio',    'BUY',  '2025-03-01', '/research/niftybees_analysis.pdf', 'Divya Nair',
 'Nifty BeES is an Exchange Traded Fund that tracks the Nifty 50 index. This means it holds shares of the top 50 companies on NSE like Reliance, TCS, and HDFC Bank. It is a very low-cost way to invest in the Indian stock market with an expense ratio of only 0.04%. It is ideal for beginners and investors who want simple broad market exposure without picking individual stocks.'),

(8,  8,  'Kotak Gold Fund — Geopolitical Hedge Strategy',           'HOLD', '2025-03-05', '/research/kotakgold_geo.pdf',      'Amit Desai',
 'Kotak Gold Fund is a mutual fund that invests in gold. Gold is traditionally considered a safe asset during uncertain global times. The fund has done well recently due to rising gold prices globally. It is suitable to hold a small portion of your portfolio in this fund as a safety buffer. We do not recommend increasing the allocation beyond 10% of total investments.'),

(9,  9,  'L&T Finance 2027 Bond — Infrastructure Sector Outlook',  'BUY',  '2025-03-10', '/research/ltfin2027_outlook.pdf',  'Ritu Bansal',
 'This is a bond issued by L&T Finance Holdings that matures in 2027. It offers a fixed annual interest of 8.10% which is higher than most bank fixed deposits. L&T Finance is backed by the well-known Larsen and Toubro Group and has a strong credit rating of AA+ from CRISIL. This bond is a good option for investors looking for better fixed income returns than a savings account or FD.'),

(10, 10, 'HDFC Structured Product — Capital Protection Review',     'SELL', '2025-03-15', '/research/hdfcstruct_review.pdf',  'Vikram Joshi',
 'This is a structured product issued by HDFC Bank linked to the Nifty 50 index. It is currently in suspended status and no new investments are accepted. Structured products are complex instruments generally suitable only for experienced or high net worth investors. Due to the suspension and complexity of this product we recommend that existing holders exit their position when an opportunity arises.');

-- ── Verify ────────────────────────────────────────────────────
SELECT 'securities'     AS tbl, COUNT(*) AS row_count FROM securities
UNION ALL
SELECT 'product_terms', COUNT(*) FROM product_terms
UNION ALL
SELECT 'research_notes',COUNT(*) FROM research_notes;
-- ============================================================
