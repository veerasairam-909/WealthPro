-- ============================================================
--  WealthPro — PBOR Database Seed Data
--  Database : pbor
--  Updated  : every active client now has 3 diversified holdings
--             with avg_cost set to produce visible gains AND losses.
--
--  P&L summary per account (using updated prices from productcatalog):
--   1 Priya    HDFCBANK(+18%) INFY(-4%)      NIFTYBEES(+37%)  → +₹55,150  +13.1%
--   2 Rohan    SBIBLUECHIP(+26%) RELIANCE(-10%) ICICIBOND28(-2%)→ +₹2,700   +1.2%
--   3 Anjali   TATASTEEL(+43%) KOTAKGOLD(+34%) INFY(-8%)       → +₹21,870  +15.6%
--   4 Vikram   ICICIBOND28(+4%) HDFCBANK(-6%)  LTFIN2027(+1%)  →  -₹850    -0.1%
--   5 Deepa    NIFTYBEES(+39%) HDFCBANK(+7%)  RELIANCE(-13%)   → +₹31,650  +13.8%
--   6 Arjun    RELIANCE(+5%)   TATASTEEL(-10%) SBIBLUECHIP(+18%)→ +₹5,640   +2.7%
--   7 Sneha    TATASTEEL(+31%) INFY(+10%)      NIFTYBEES(+21%)  → +₹18,835  +18.3%
--   8 Rahul    KOTAKGOLD(+47%) ICICIBOND28(-3%) LTFIN2027(+2%)  → +₹5,950   +1.4%
--   9 (INACTIVE) NIFTYBEES(-3%)                                  →  -₹850    -3.0%
--  10 Kiran    LTFIN2027(-1%)  SBIBLUECHIP(+11%) HDFCBANK(+15%) → +₹12,785  +4.9%
-- ============================================================

USE pbor;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE cash_ledger;
TRUNCATE TABLE holding;
TRUNCATE TABLE corporate_action;
TRUNCATE TABLE account;

ALTER TABLE account          AUTO_INCREMENT = 1;
ALTER TABLE holding          AUTO_INCREMENT = 1;
ALTER TABLE cash_ledger      AUTO_INCREMENT = 1;
ALTER TABLE corporate_action AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ── 1. Account ────────────────────────────────────────────────
-- client_id matches Client.clientId from kyc database
INSERT INTO account (account_id, client_id, account_type, base_currency, status) VALUES
(1,  1,  'INDIVIDUAL', 'INR', 'ACTIVE'),
(2,  2,  'INDIVIDUAL', 'INR', 'ACTIVE'),
(3,  3,  'INDIVIDUAL', 'INR', 'ACTIVE'),
(4,  4,  'TRUST',      'INR', 'ACTIVE'),
(5,  5,  'INDIVIDUAL', 'INR', 'ACTIVE'),
(6,  6,  'INDIVIDUAL', 'INR', 'ACTIVE'),
(7,  7,  'JOINT',      'INR', 'ACTIVE'),
(8,  8,  'TRUST',      'INR', 'ACTIVE'),
(9,  9,  'INDIVIDUAL', 'INR', 'INACTIVE'),
(10, 10, 'INDIVIDUAL', 'INR', 'ACTIVE');

-- ── 2. Holdings ───────────────────────────────────────────────
-- Each active account gets 3 holdings spanning different asset classes.
-- avg_cost is deliberately set relative to current_price to create P&L.
-- Prices used:  HDFCBANK=1842.50  RELIANCE=2780  SBIBLUECHIP=61.20
--               TATASTEEL=156.80  ICICIBOND28=1015  INFY=1650
--               NIFTYBEES=271.50  KOTAKGOLD=58.90  LTFIN2027=1002.50

INSERT INTO holding (holding_id, account_id, security_id, quantity, avg_cost, valuation_currency, last_valuation_date) VALUES
-- ── Account 1: Priya (INDIVIDUAL) — mixed equity + ETF ──────
(1,  1, 1, 150.0000, 1560.0000, 'INR', '2026-04-28'),  -- HDFCBANK  150 × 1560  → +18.1%
(2,  1, 6,  80.0000, 1720.0000, 'INR', '2026-04-28'),  -- INFY       80 × 1720  →  -4.1%
(3,  1, 7, 250.0000,  198.0000, 'INR', '2026-04-28'),  -- NIFTYBEES 250 × 198   → +37.1%

-- ── Account 2: Rohan (INDIVIDUAL) — MF + equity + bond ──────
(4,  2, 3,1000.0000,   48.5000, 'INR', '2026-04-28'),  -- SBIBLUECHIP 1000×48.5 → +26.2%
(5,  2, 2,  25.0000, 3100.0000, 'INR', '2026-04-28'),  -- RELIANCE    25×3100   → -10.3%
(6,  2, 5, 100.0000, 1035.0000, 'INR', '2026-04-28'),  -- ICICIBOND28 100×1035  →  -1.9%

-- ── Account 3: Anjali (INDIVIDUAL) — steel + gold MF + IT ───
(7,  3, 4, 500.0000,  110.0000, 'INR', '2026-04-28'),  -- TATASTEEL  500 × 110  → +42.5%
(8,  3, 8, 300.0000,   44.0000, 'INR', '2026-04-28'),  -- KOTAKGOLD  300 × 44   → +33.9%
(9,  3, 6,  40.0000, 1800.0000, 'INR', '2026-04-28'),  -- INFY        40 × 1800 →  -8.3%

-- ── Account 4: Vikram (TRUST) — bond-heavy, near breakeven ──
(10, 4, 5, 500.0000,  980.0000, 'INR', '2026-04-28'),  -- ICICIBOND28 500×980   →  +3.6%
(11, 4, 1, 200.0000, 1950.0000, 'INR', '2026-04-28'),  -- HDFCBANK   200×1950   →  -5.5%
(12, 4, 9, 300.0000,  992.0000, 'INR', '2026-04-28'),  -- LTFIN2027  300×992    →  +1.1%

-- ── Account 5: Deepa (INDIVIDUAL) — ETF-heavy with loss ─────
(13, 5, 7, 400.0000,  195.0000, 'INR', '2026-04-28'),  -- NIFTYBEES  400×195    → +39.2%
(14, 5, 1,  60.0000, 1720.0000, 'INR', '2026-04-28'),  -- HDFCBANK    60×1720   →  +7.1%
(15, 5, 2,  15.0000, 3200.0000, 'INR', '2026-04-28'),  -- RELIANCE    15×3200   → -13.1%

-- ── Account 6: Arjun (INDIVIDUAL) — mixed with steel loss ───
(16, 6, 2,  50.0000, 2650.0000, 'INR', '2026-04-28'),  -- RELIANCE    50×2650   →  +4.9%
(17, 6, 4, 300.0000,  175.0000, 'INR', '2026-04-28'),  -- TATASTEEL  300×175    → -10.4%
(18, 6, 3, 500.0000,   52.0000, 'INR', '2026-04-28'),  -- SBIBLUECHIP 500×52    → +17.7%

-- ── Account 7: Sneha (JOINT) — all gains portfolio ───────────
(19, 7, 4, 200.0000,  120.0000, 'INR', '2026-04-28'),  -- TATASTEEL  200×120    → +30.7%
(20, 7, 6,  30.0000, 1500.0000, 'INR', '2026-04-28'),  -- INFY        30×1500   → +10.0%
(21, 7, 7, 150.0000,  225.0000, 'INR', '2026-04-28'),  -- NIFTYBEES  150×225    → +20.7%

-- ── Account 8: Rahul (TRUST) — gold + bonds, slight gain ────
(22, 8, 8, 500.0000,   40.0000, 'INR', '2026-04-28'),  -- KOTAKGOLD  500×40     → +47.3%
(23, 8, 5, 200.0000, 1050.0000, 'INR', '2026-04-28'),  -- ICICIBOND28 200×1050  →  -3.3%
(24, 8, 9, 200.0000,  985.0000, 'INR', '2026-04-28'),  -- LTFIN2027  200×985    →  +1.8%

-- ── Account 9: INACTIVE — minimal single holding ────────────
(25, 9, 7, 100.0000,  280.0000, 'INR', '2026-04-28'),  -- NIFTYBEES  100×280    →  -3.0%

-- ── Account 10: Kiran (INDIVIDUAL) — bond loss + MF + equity ─
(26,10, 9, 150.0000, 1015.0000, 'INR', '2026-04-28'),  -- LTFIN2027  150×1015   →  -1.2%
(27,10, 3, 800.0000,   55.0000, 'INR', '2026-04-28'),  -- SBIBLUECHIP 800×55    → +11.3%
(28,10, 1,  40.0000, 1600.0000, 'INR', '2026-04-28');  -- HDFCBANK    40×1600   → +15.2%

-- ── 3. Cash Ledger ────────────────────────────────────────────
-- Sign convention (matches CashLedgerServiceImpl):
--   SUBSCRIPTION → negative  (cash leaves the account to buy securities)
--   REDEMPTION   → positive  (cash returns to the account on sale)
--   DIVIDEND     → positive  (income received)
--   FEE          → negative  (cost charged to the account)
-- First entry per account is an initial funding (DIVIDEND) so the balance
-- is a realistic positive "uninvested cash" figure.
--
--  Expected cash balances after this seed:
--   Acct 1 Priya    :  +₹4,50,000 − ₹4,21,100 + ₹2,250   = ₹31,150
--   Acct 2 Rohan    :  +₹2,50,000 − ₹2,29,500 + ₹875     = ₹21,375
--   Acct 3 Anjali   :  +₹1,60,000 − ₹1,40,200             = ₹19,800
--   Acct 4 Vikram   : +₹12,00,000 − ₹11,77,600 − ₹2,500  = ₹19,900
--   Acct 5 Deepa    :  +₹2,50,000 − ₹2,29,200             = ₹20,800
--   Acct 6 Arjun    :  +₹2,30,000 − ₹2,11,000             = ₹19,000
--   Acct 7 Sneha    :  +₹1,25,000 − ₹1,02,750 + ₹1,500   = ₹23,750
--   Acct 8 Rahul    :  +₹4,50,000 − ₹4,27,000             = ₹23,000
--   Acct 9 Inactive :  +₹30,000   − ₹28,000               = ₹2,000
--   Acct10 Kiran    :  +₹2,80,000 − ₹2,60,250 + ₹960     = ₹20,710
INSERT INTO cash_ledger (ledger_id, account_id, txn_type, amount, currency, txn_date, narrative) VALUES
-- Account 1 — Priya  (balance = 31,150)
(1,  1, 'DIVIDEND',      450000.00, 'INR', '2024-11-01', 'Initial account funding'),
(2,  1, 'SUBSCRIPTION', -234000.00, 'INR', '2024-11-15', 'BUY HDFCBANK 150 @ 1560.00'),
(3,  1, 'SUBSCRIPTION', -137600.00, 'INR', '2024-12-02', 'BUY INFY 80 @ 1720.00'),
(4,  1, 'SUBSCRIPTION',  -49500.00, 'INR', '2024-12-10', 'BUY NIFTYBEES 250 @ 198.00'),
(5,  1, 'DIVIDEND',        2250.00, 'INR', '2025-02-28', 'HDFCBANK Interim Dividend Rs.15 x 150 shares'),
-- Account 2 — Rohan  (balance = 21,375)
(6,  2, 'DIVIDEND',      250000.00, 'INR', '2024-10-01', 'Initial account funding'),
(7,  2, 'SUBSCRIPTION',  -48500.00, 'INR', '2024-10-12', 'SUBSCRIBE SBIBLUECHIP 1000 units @ NAV 48.50'),
(8,  2, 'SUBSCRIPTION',  -77500.00, 'INR', '2024-11-20', 'BUY RELIANCE 25 @ 3100.00'),
(9,  2, 'SUBSCRIPTION', -103500.00, 'INR', '2024-12-01', 'BUY ICICIBOND28 100 @ 1035.00'),
(10, 2, 'DIVIDEND',         875.00, 'INR', '2025-02-28', 'SBIBLUECHIP Dividend payout'),
-- Account 3 — Anjali  (balance = 19,800)
(11, 3, 'DIVIDEND',      160000.00, 'INR', '2024-09-01', 'Initial account funding'),
(12, 3, 'SUBSCRIPTION',  -55000.00, 'INR', '2024-09-22', 'BUY TATASTEEL 500 @ 110.00'),
(13, 3, 'SUBSCRIPTION',  -13200.00, 'INR', '2024-10-05', 'SUBSCRIBE KOTAKGOLD 300 units @ NAV 44.00'),
(14, 3, 'SUBSCRIPTION',  -72000.00, 'INR', '2024-10-18', 'BUY INFY 40 @ 1800.00'),
-- Account 4 — Vikram (TRUST)  (balance = 19,900)
(15, 4, 'DIVIDEND',     1200000.00, 'INR', '2024-08-01', 'Initial account funding'),
(16, 4, 'SUBSCRIPTION', -490000.00, 'INR', '2024-08-10', 'BUY ICICIBOND28 500 @ 980.00'),
(17, 4, 'SUBSCRIPTION', -390000.00, 'INR', '2024-09-05', 'BUY HDFCBANK 200 @ 1950.00'),
(18, 4, 'SUBSCRIPTION', -297600.00, 'INR', '2024-09-20', 'BUY LTFIN2027 300 @ 992.00'),
(19, 4, 'FEE',            -2500.00, 'INR', '2025-03-31', 'Quarterly advisory fee Q1 FY2026'),
-- Account 5 — Deepa  (balance = 20,800)
(20, 5, 'DIVIDEND',      250000.00, 'INR', '2024-10-01', 'Initial account funding'),
(21, 5, 'SUBSCRIPTION',  -78000.00, 'INR', '2024-10-08', 'BUY NIFTYBEES 400 @ 195.00'),
(22, 5, 'SUBSCRIPTION', -103200.00, 'INR', '2024-11-12', 'BUY HDFCBANK 60 @ 1720.00'),
(23, 5, 'SUBSCRIPTION',  -48000.00, 'INR', '2024-12-05', 'BUY RELIANCE 15 @ 3200.00'),
-- Account 6 — Arjun  (balance = 19,000)
(24, 6, 'DIVIDEND',      230000.00, 'INR', '2024-11-01', 'Initial account funding'),
(25, 6, 'SUBSCRIPTION', -132500.00, 'INR', '2024-11-08', 'BUY RELIANCE 50 @ 2650.00'),
(26, 6, 'SUBSCRIPTION',  -52500.00, 'INR', '2024-11-25', 'BUY TATASTEEL 300 @ 175.00'),
(27, 6, 'SUBSCRIPTION',  -26000.00, 'INR', '2024-12-10', 'SUBSCRIBE SBIBLUECHIP 500 units @ NAV 52.00'),
-- Account 7 — Sneha (JOINT)  (balance = 23,750)
(28, 7, 'DIVIDEND',      125000.00, 'INR', '2024-09-01', 'Initial account funding'),
(29, 7, 'SUBSCRIPTION',  -24000.00, 'INR', '2024-09-10', 'BUY TATASTEEL 200 @ 120.00'),
(30, 7, 'SUBSCRIPTION',  -45000.00, 'INR', '2024-10-01', 'BUY INFY 30 @ 1500.00'),
(31, 7, 'SUBSCRIPTION',  -33750.00, 'INR', '2024-10-20', 'BUY NIFTYBEES 150 @ 225.00'),
(32, 7, 'DIVIDEND',        1500.00, 'INR', '2025-03-28', 'NIFTYBEES Quarterly Dividend'),
-- Account 8 — Rahul (TRUST)  (balance = 23,000)
(33, 8, 'DIVIDEND',      450000.00, 'INR', '2024-07-01', 'Initial account funding'),
(34, 8, 'SUBSCRIPTION',  -20000.00, 'INR', '2024-07-22', 'SUBSCRIBE KOTAKGOLD 500 units @ NAV 40.00'),
(35, 8, 'SUBSCRIPTION', -210000.00, 'INR', '2024-08-05', 'BUY ICICIBOND28 200 @ 1050.00'),
(36, 8, 'SUBSCRIPTION', -197000.00, 'INR', '2024-08-20', 'BUY LTFIN2027 200 @ 985.00'),
-- Account 9 — INACTIVE  (balance = 2,000)
(37, 9, 'DIVIDEND',       30000.00, 'INR', '2024-06-01', 'Initial account funding'),
(38, 9, 'SUBSCRIPTION',  -28000.00, 'INR', '2024-06-10', 'BUY NIFTYBEES 100 @ 280.00'),
-- Account 10 — Kiran  (balance = 20,710)
(39,10, 'DIVIDEND',      280000.00, 'INR', '2024-10-01', 'Initial account funding'),
(40,10, 'SUBSCRIPTION', -152250.00, 'INR', '2024-10-28', 'BUY LTFIN2027 150 @ 1015.00'),
(41,10, 'SUBSCRIPTION',  -44000.00, 'INR', '2024-11-10', 'SUBSCRIBE SBIBLUECHIP 800 units @ NAV 55.00'),
(42,10, 'SUBSCRIPTION',  -64000.00, 'INR', '2024-12-01', 'BUY HDFCBANK 40 @ 1600.00'),
(43,10, 'DIVIDEND',          960.00, 'INR', '2025-02-28', 'SBIBLUECHIP Dividend payout');

-- ── 4. Corporate Actions ──────────────────────────────────────
INSERT INTO corporate_action (ca_id, security_id, ca_type, record_date, ex_date, pay_date, terms_json) VALUES
(1,  1,  'DIVIDEND',   '2025-01-10', '2025-01-09', '2025-01-20', '{"dividendPerShare":15.00,"currency":"INR","frequency":"Interim"}'),
(2,  2,  'DIVIDEND',   '2025-01-20', '2025-01-19', '2025-02-01', '{"dividendPerShare":9.00,"currency":"INR","frequency":"Annual"}'),
(3,  3,  'REDEMPTION', '2025-02-01', '2025-01-31', '2025-02-05', '{"redemptionNAV":61.20,"units":"optional","minUnits":100}'),
(4,  4,  'BONUS',      '2025-02-15', '2025-02-14', '2025-02-20', '{"ratio":"1:2","faceValue":2,"newSharesPerOld":0.5}'),
(5,  5,  'COUPON',     '2025-03-31', '2025-03-30', '2025-04-05', '{"couponRate":"7.5%","couponAmount":375.00,"currency":"INR"}'),
(6,  6,  'DIVIDEND',   '2025-01-25', '2025-01-24', '2025-02-05', '{"dividendPerShare":21.00,"currency":"INR","frequency":"Annual"}'),
(7,  7,  'DIVIDEND',   '2025-03-28', '2025-03-27', '2025-04-10', '{"dividendPerUnit":2.50,"currency":"INR","frequency":"Quarterly"}'),
(8,  1,  'SPLIT',      '2025-04-01', '2025-03-31', '2025-04-01', '{"ratio":"2:1","oldFaceValue":2,"newFaceValue":1}'),
(9,  8,  'REDEMPTION', '2025-03-15', '2025-03-14', '2025-03-20', '{"redemptionNAV":58.90,"units":"optional","minUnits":100}'),
(10, 9,  'COUPON',     '2025-06-30', '2025-06-29', '2025-07-05', '{"couponRate":"8.2%","couponAmount":410.00,"currency":"INR"}');

-- ── Verify ────────────────────────────────────────────────────
SELECT 'account'          AS tbl, COUNT(*) AS row_count FROM account
UNION ALL
SELECT 'holding',         COUNT(*) FROM holding
UNION ALL
SELECT 'cash_ledger',     COUNT(*) FROM cash_ledger
UNION ALL
SELECT 'corporate_action',COUNT(*) FROM corporate_action;
-- ============================================================
