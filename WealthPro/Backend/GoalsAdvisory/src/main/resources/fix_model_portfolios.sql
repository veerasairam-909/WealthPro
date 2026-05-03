-- ============================================================
--  WealthPro — Model Portfolio Live-Database Patch
--  Database : wealth
--
--  Fixes Conservative portfolios that incorrectly contained
--  EQUITY (violates Suitability Rule 1) and a Balanced
--  portfolio that contained STRUCTURED (violates Rule 6 —
--  STRUCTURED is UHNI-only).
--
--  Run this against the running database.
--  No restart required — changes are immediate.
-- ============================================================

USE wealth;

-- ── Conservative Shield (ID 1) ────────────────────────────────
-- Was: BOND 55, MUTUAL_FUND 25, ETF 15, EQUITY 5  ← EQUITY not allowed
-- Fix: redistribute EQUITY 5% into BOND
UPDATE model_portfolios
SET weights_json = '{"BOND":60,"MUTUAL_FUND":25,"ETF":15}'
WHERE model_id = 1;

-- ── Retirement Cornerstone (ID 5) ────────────────────────────
-- Was: BOND 60, MUTUAL_FUND 25, ETF 10, EQUITY 5  ← EQUITY not allowed
-- Fix: redistribute EQUITY 5% into BOND
UPDATE model_portfolios
SET weights_json = '{"BOND":65,"MUTUAL_FUND":25,"ETF":10}'
WHERE model_id = 5;

-- ── Income Plus (ID 7) ────────────────────────────────────────
-- Was: BOND 65, MUTUAL_FUND 20, ETF 10, EQUITY 5  ← EQUITY not allowed
-- Fix: redistribute EQUITY 5% into BOND
UPDATE model_portfolios
SET weights_json = '{"BOND":70,"MUTUAL_FUND":20,"ETF":10}'
WHERE model_id = 7;

-- ── Multi Asset Dynamic (ID 9) ───────────────────────────────
-- Was: EQUITY 35, BOND 25, MUTUAL_FUND 25, ETF 10, STRUCTURED 5
--       ← STRUCTURED not allowed in a BALANCED portfolio
--          (Rule 6: STRUCTURED restricted to UHNI clients only;
--           Balanced portfolios are sent to non-UHNI clients too)
-- Fix: redistribute STRUCTURED 5% into EQUITY
UPDATE model_portfolios
SET weights_json = '{"EQUITY":40,"BOND":25,"MUTUAL_FUND":25,"ETF":10}'
WHERE model_id = 9;

-- ── Capital Protect Plus (ID 10) — INACTIVE ──────────────────
-- Was: BOND 70, MUTUAL_FUND 20, ETF 8, EQUITY 2  ← EQUITY not allowed
-- Fix: redistribute EQUITY 2% into BOND
UPDATE model_portfolios
SET weights_json = '{"BOND":72,"MUTUAL_FUND":20,"ETF":8}'
WHERE model_id = 10;

-- ── Fix proposal_json in recommendations that referenced ──────
-- ── the old (incorrect) Conservative portfolio allocations ────

-- Rec 6: client 6 (Conservative), used Conservative Shield (ID 1)
UPDATE recommendations
SET proposal_json = '{"targetReturn":8.0,"timeHorizon":"7 years","SIP":8000,"allocation":{"BOND":60,"MUTUAL_FUND":25,"ETF":15}}'
WHERE reco_id = 6;

-- Rec 9: client 9 (Conservative), used Income Plus (ID 7)
UPDATE recommendations
SET proposal_json = '{"targetReturn":7.5,"timeHorizon":"2 years","lumpsum":100000,"allocation":{"BOND":68,"MUTUAL_FUND":22,"ETF":10}}'
WHERE reco_id = 9;

-- Rec 10: client 10 (Balanced), used Retirement Cornerstone (ID 5)
UPDATE recommendations
SET proposal_json = '{"targetReturn":9.5,"timeHorizon":"18 years","SIP":30000,"allocation":{"BOND":65,"MUTUAL_FUND":25,"ETF":10}}'
WHERE reco_id = 10;

-- ── Verify ────────────────────────────────────────────────────
SELECT model_id, name, risk_class, weights_json, status
FROM model_portfolios
ORDER BY model_id;
-- ============================================================
