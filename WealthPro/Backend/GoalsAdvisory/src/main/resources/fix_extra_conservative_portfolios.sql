-- ============================================================
--  WealthPro — Fix manually-created Conservative portfolios
--  Database : wealth
--
--  Problems found:
--   1. All contain EQUITY (violates Suitability Rule 1)
--   2. Values stored as fractions (0.65) not percentages (65)
--      — they sum to 1.0 instead of 100
--   3. Use "cash" and "gold" which are not valid asset classes
--      in the product catalog (no securities exist for them)
--
--  Fix: remove EQUITY, convert fractions to percentages,
--       replace cash → MUTUAL_FUND, gold → ETF
-- ============================================================

USE wealth;

-- Conservative Income
-- Was: equity 0.2, bond 0.65, cash 0.15  (fractions, sums to 1.0)
-- Fix: BOND 85%, MUTUAL_FUND 15%
UPDATE model_portfolios
SET weights_json = '{"BOND":85,"MUTUAL_FUND":15}'
WHERE name = 'Conservative Income' AND risk_class = 'CONSERVATIVE';

-- Senior Citizen Plan
-- Was: equity 0.1, bond 0.75, cash 0.15  (fractions, sums to 1.0)
-- Fix: BOND 85%, MUTUAL_FUND 15%
UPDATE model_portfolios
SET weights_json = '{"BOND":85,"MUTUAL_FUND":15}'
WHERE name = 'Senior Citizen Plan' AND risk_class = 'CONSERVATIVE';

-- ELSS Conservative
-- Was: equity 0.3, bond 0.55, cash 0.15  (fractions, sums to 1.0)
-- Fix: BOND 75%, MUTUAL_FUND 25%
UPDATE model_portfolios
SET weights_json = '{"BOND":75,"MUTUAL_FUND":25}'
WHERE name = 'ELSS Conservative' AND risk_class = 'CONSERVATIVE';

-- Retirement Income
-- Was: equity 0.15, bond 0.7, cash 0.15  (fractions, sums to 1.0)
-- Fix: BOND 85%, MUTUAL_FUND 15%
UPDATE model_portfolios
SET weights_json = '{"BOND":85,"MUTUAL_FUND":15}'
WHERE name = 'Retirement Income' AND risk_class = 'CONSERVATIVE';

-- Retired Pensioner
-- Was: equity 0.1, bond 0.8, cash 0.1  (fractions, sums to 1.0)
-- Fix: BOND 85%, MUTUAL_FUND 15%
UPDATE model_portfolios
SET weights_json = '{"BOND":85,"MUTUAL_FUND":15}'
WHERE name = 'Retired Pensioner' AND risk_class = 'CONSERVATIVE';

-- Wealth Preserver
-- Was: equity 0.2, bond 0.7, gold 0.05, cash 0.05  (fractions, sums to 1.0)
-- Fix: BOND 80%, MUTUAL_FUND 15%, ETF 5%  (gold → ETF)
UPDATE model_portfolios
SET weights_json = '{"BOND":80,"MUTUAL_FUND":15,"ETF":5}'
WHERE name = 'Wealth Preserver' AND risk_class = 'CONSERVATIVE';

-- Verify all Conservative portfolios are now clean
SELECT model_id, name, risk_class, weights_json, status
FROM model_portfolios
WHERE risk_class = 'CONSERVATIVE'
ORDER BY model_id;
-- ============================================================
