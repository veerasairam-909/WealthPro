-- ============================================================
--  WealthPro — Full Model Portfolio Patch
--  Database : wealth
--
--  Problems fixed:
--   1. Values stored as fractions (0.6) not percentages (60)
--   2. Invalid asset classes: cash, gold, reit, international,
--      banking_equity, psu_equity, smallcap_equity, midcap_equity
--      → mapped to valid ones: EQUITY, BOND, MUTUAL_FUND, ETF
--   3. Allocations not summing to 100
--   4. EQUITY in Conservative portfolios (Suitability Rule 1)
--
--  Valid asset classes: EQUITY, BOND, MUTUAL_FUND, ETF, STRUCTURED
--  Mapping used:
--    cash             → MUTUAL_FUND
--    gold             → ETF
--    reit             → ETF
--    international    → ETF
--    banking_equity   → EQUITY  (sector equity is still equity)
--    psu_equity       → EQUITY
--    smallcap_equity  → EQUITY
--    midcap_equity    → EQUITY
--
--  Conservative portfolios (1,5,7,10,12,16,20,23,30,33):
--    NO EQUITY, NO STRUCTURED — only BOND, MUTUAL_FUND, ETF
--  Balanced portfolios (2,4,8,9,11,14,17,21,24,26,27,29,32,34):
--    EQUITY allowed — NO STRUCTURED
--  Aggressive portfolios (3,6,13,15,18,19,22,25,28,31,35):
--    EQUITY allowed — STRUCTURED only in UHNI-targeted fund (ID 6)
-- ============================================================

USE wealth;

-- ── Already correct — no change needed ──────────────────────
-- ID 1  Conservative Shield      CONSERVATIVE  BOND 60, MUTUAL_FUND 25, ETF 15
-- ID 2  Balanced Growth          BALANCED      EQUITY 40, BOND 30, MUTUAL_FUND 20, ETF 10
-- ID 3  Aggressive Alpha         AGGRESSIVE    EQUITY 70, ETF 15, MUTUAL_FUND 10, BOND 5
-- ID 4  HNI Wealth Builder       BALANCED      EQUITY 45, BOND 25, MUTUAL_FUND 20, ETF 10
-- ID 5  Retirement Cornerstone   CONSERVATIVE  BOND 65, MUTUAL_FUND 25, ETF 10
-- ID 6  UHNI Alpha Plus          AGGRESSIVE    EQUITY 65, STRUCTURED 15, ETF 12, MUTUAL_FUND 8
-- ID 7  Income Plus              CONSERVATIVE  BOND 70, MUTUAL_FUND 20, ETF 10
-- ID 8  Blue Chip Select         BALANCED      EQUITY 50, BOND 25, MUTUAL_FUND 15, ETF 10
-- ID 9  Multi Asset Dynamic      BALANCED      EQUITY 40, BOND 25, MUTUAL_FUND 25, ETF 10
-- ID 10 Capital Protect Plus     CONSERVATIVE  BOND 72, MUTUAL_FUND 20, ETF 8  (INACTIVE)

-- ── Balanced 60/40 ───────────────────────────────────────────
-- Was:  equity 0.6, bond 0.3, cash 0.1  (fractions, invalid class)
-- Fix:  EQUITY 60%, BOND 30%, MUTUAL_FUND 10%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":60,"BOND":30,"MUTUAL_FUND":10}'
WHERE name = 'Balanced 60/40';

-- ── Conservative Income ──────────────────────────────────────
-- Already fixed by previous patch — verify only
-- BOND 85%, MUTUAL_FUND 15%  ✓

-- ── Aggressive Growth ────────────────────────────────────────
-- Was:  equity 0.85, bond 0.1, cash 0.05  (fractions, invalid class)
-- Fix:  EQUITY 85%, BOND 10%, MUTUAL_FUND 5%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":85,"BOND":10,"MUTUAL_FUND":5}'
WHERE name = 'Aggressive Growth';

-- ── Dividend Yield ───────────────────────────────────────────
-- Was:  equity 0.55, bond 0.4, cash 0.05  (fractions, invalid class)
-- Fix:  EQUITY 55%, BOND 40%, MUTUAL_FUND 5%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":55,"BOND":40,"MUTUAL_FUND":5}'
WHERE name = 'Dividend Yield';

-- ── Tax Saver ELSS ───────────────────────────────────────────
-- Was:  equity 0.95, bond 0%, cash 0.05  (fractions, invalid class)
-- Fix:  EQUITY 95%, MUTUAL_FUND 5%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":95,"MUTUAL_FUND":5}'
WHERE name = 'Tax Saver ELSS';

-- ── Senior Citizen Plan ──────────────────────────────────────
-- Already fixed — BOND 85%, MUTUAL_FUND 15%  ✓

-- ── Multi Asset Plus ─────────────────────────────────────────
-- Was:  equity 0.5, bond 0.3, gold 0.1, cash 0.1  (fractions, invalid classes)
-- Fix:  EQUITY 50%, BOND 30%, ETF 10%, MUTUAL_FUND 10%
--       (gold → ETF, cash → MUTUAL_FUND)
UPDATE model_portfolios
SET weights_json = '{"EQUITY":50,"BOND":30,"ETF":10,"MUTUAL_FUND":10}'
WHERE name = 'Multi Asset Plus';

-- ── Global Tech Tilt ─────────────────────────────────────────
-- Was:  equity 0.9, international 0.3, cash 0.1  (fractions, invalid, sums to 1.3!)
-- Fix:  EQUITY 60%, ETF 30%, MUTUAL_FUND 10%
--       (international → ETF as closest match, corrected sum)
UPDATE model_portfolios
SET weights_json = '{"EQUITY":60,"ETF":30,"MUTUAL_FUND":10}'
WHERE name = 'Global Tech Tilt';

-- ── Indian Banks Focus ───────────────────────────────────────
-- Was:  banking_equity 0.7, bond 0.2, cash 0.1  (fractions, invalid class)
-- Fix:  EQUITY 70%, BOND 20%, MUTUAL_FUND 10%
--       (banking_equity → EQUITY, cash → MUTUAL_FUND)
UPDATE model_portfolios
SET weights_json = '{"EQUITY":70,"BOND":20,"MUTUAL_FUND":10}'
WHERE name = 'Indian Banks Focus';

-- ── ELSS Conservative ────────────────────────────────────────
-- Already fixed — BOND 75%, MUTUAL_FUND 25%  ✓

-- ── PSU Dividend ─────────────────────────────────────────────
-- Was:  psu_equity 0.45, bond 0.45, cash 0.1  (fractions, invalid class)
-- Fix:  EQUITY 45%, BOND 45%, MUTUAL_FUND 10%
--       (psu_equity → EQUITY, cash → MUTUAL_FUND)
UPDATE model_portfolios
SET weights_json = '{"EQUITY":45,"BOND":45,"MUTUAL_FUND":10}'
WHERE name = 'PSU Dividend';

-- ── Smallcap Aggressive ──────────────────────────────────────
-- Was:  smallcap_equity 0.8, cash 0.2  (fractions, invalid class)
-- Fix:  EQUITY 80%, MUTUAL_FUND 20%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":80,"MUTUAL_FUND":20}'
WHERE name = 'Smallcap Aggressive';

-- ── Retirement Income ────────────────────────────────────────
-- Already fixed — BOND 85%, MUTUAL_FUND 15%  ✓

-- ── Balanced Hybrid ──────────────────────────────────────────
-- Was:  equity 0.55, bond 0.35, cash 0.1  (fractions, invalid class)
-- Fix:  EQUITY 55%, BOND 35%, MUTUAL_FUND 10%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":55,"BOND":35,"MUTUAL_FUND":10}'
WHERE name = 'Balanced Hybrid';

-- ── Mid+SmallCap Growth ──────────────────────────────────────
-- Was:  midcap_equity 0.5, smallcap_equity 0.4, cash 0.1  (fractions, invalid classes)
-- Fix:  EQUITY 90%, MUTUAL_FUND 10%
--       (midcap_equity + smallcap_equity → EQUITY combined, cash → MUTUAL_FUND)
UPDATE model_portfolios
SET weights_json = '{"EQUITY":90,"MUTUAL_FUND":10}'
WHERE name = 'Mid+SmallCap Growth';

-- ── Gold Hedge ───────────────────────────────────────────────
-- Was:  equity 0.4, bond 0.3, gold 0.2, cash 0.1  (fractions, invalid classes)
-- Fix:  EQUITY 40%, BOND 30%, ETF 20%, MUTUAL_FUND 10%
--       (gold → ETF, cash → MUTUAL_FUND)
UPDATE model_portfolios
SET weights_json = '{"EQUITY":40,"BOND":30,"ETF":20,"MUTUAL_FUND":10}'
WHERE name = 'Gold Hedge';

-- ── NRI Friendly ─────────────────────────────────────────────
-- Was:  equity 0.45, bond 0.4, cash 0.15  (fractions, invalid class)
-- Fix:  EQUITY 45%, BOND 40%, MUTUAL_FUND 15%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":45,"BOND":40,"MUTUAL_FUND":15}'
WHERE name = 'NRI Friendly';

-- ── Inflation Beat ───────────────────────────────────────────
-- Was:  equity 0.75, reit 0.1, gold 0.1, cash 0.05  (fractions, invalid classes)
-- Fix:  EQUITY 75%, ETF 20%, MUTUAL_FUND 5%
--       (reit + gold → ETF combined, cash → MUTUAL_FUND)
UPDATE model_portfolios
SET weights_json = '{"EQUITY":75,"ETF":20,"MUTUAL_FUND":5}'
WHERE name = 'Inflation Beat';

-- ── Stable Growth ────────────────────────────────────────────
-- Was:  equity 0.5, bond 0.4, cash 0.1  (fractions, invalid class)
-- Fix:  EQUITY 50%, BOND 40%, MUTUAL_FUND 10%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":50,"BOND":40,"MUTUAL_FUND":10}'
WHERE name = 'Stable Growth';

-- ── Retired Pensioner ────────────────────────────────────────
-- Already fixed — BOND 85%, MUTUAL_FUND 15%  ✓

-- ── Sector Rotator ───────────────────────────────────────────
-- Was:  equity 0.85, cash 0.15  (fractions, invalid class)
-- Fix:  EQUITY 85%, MUTUAL_FUND 15%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":85,"MUTUAL_FUND":15}'
WHERE name = 'Sector Rotator';

-- ── Global Diversified ───────────────────────────────────────
-- Was:  equity 0.55, international 0.2, bond 0.2, cash 0.05  (fractions, invalid classes)
-- Fix:  EQUITY 55%, ETF 20%, BOND 20%, MUTUAL_FUND 5%
--       (international → ETF, cash → MUTUAL_FUND)
UPDATE model_portfolios
SET weights_json = '{"EQUITY":55,"ETF":20,"BOND":20,"MUTUAL_FUND":5}'
WHERE name = 'Global Diversified';

-- ── Wealth Preserver ─────────────────────────────────────────
-- Already fixed — BOND 80%, MUTUAL_FUND 15%, ETF 5%  ✓

-- ── Legacy 2018 (INACTIVE) ───────────────────────────────────
-- Was:  equity 0.5, bond 0.45, cash 0.05  (fractions, invalid class)
-- Fix:  EQUITY 50%, BOND 45%, MUTUAL_FUND 5%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":50,"BOND":45,"MUTUAL_FUND":5}'
WHERE name = 'Legacy 2018';

-- ── Aggressive Tech 2025 ─────────────────────────────────────
-- Was:  equity 0.92, cash 0.08  (fractions, invalid class)
-- Fix:  EQUITY 92%, MUTUAL_FUND 8%
UPDATE model_portfolios
SET weights_json = '{"EQUITY":92,"MUTUAL_FUND":8}'
WHERE name = 'Aggressive Tech 2025';

-- ── Verify all 35 portfolios ─────────────────────────────────
SELECT model_id, name, risk_class, weights_json, status
FROM model_portfolios
ORDER BY
  CASE risk_class
    WHEN 'CONSERVATIVE' THEN 1
    WHEN 'BALANCED'     THEN 2
    WHEN 'AGGRESSIVE'   THEN 3
  END,
  name;
-- ============================================================
