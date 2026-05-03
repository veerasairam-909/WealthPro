-- ============================================================
--  WealthPro — Suitability Rules Live-Database Patch
--  Database : kyc
--
--  Run this script against the running database to replace
--  all existing suitability rules with just the 6 clean rules.
--
--  After running, restart order-execution-service.
-- ============================================================

USE kyc;

-- Disable safe update mode temporarily
SET SQL_SAFE_UPDATES = 0;

-- Remove everything that's currently in the table
DELETE FROM suitability_rule;

-- Re-enable safe update mode
SET SQL_SAFE_UPDATES = 1;

-- Reset auto-increment so IDs start cleanly from 1
ALTER TABLE suitability_rule AUTO_INCREMENT = 1;

-- Insert only the 6 rules that work correctly
-- Expression TRUE = rule TRIGGERED = order REJECTED
INSERT INTO suitability_rule (ruleid, description, expression, status) VALUES
(1, 'Conservative clients cannot buy equity securities',
    'riskClass == CONSERVATIVE AND side == BUY AND assetClass == EQUITY',
    'Active'),
(2, 'Order quantity must not exceed 10000 units',
    'quantity > 10000',
    'Active'),
(3, 'HNI clients minimum investment must be above 10000',
    'segment == HNI AND orderValue < 10000',
    'Active'),
(5, 'ETF order must use LIMIT or MARKET price type only',
    'assetClass == ETF AND priceType != LIMIT AND priceType != MARKET',
    'Active'),
(6, 'Structured product restricted to UHNI clients only',
    'assetClass == STRUCTURED AND segment != UHNI',
    'Active'),
(7, 'UHNI clients minimum investment must be above 1 lakh',
    'segment == UHNI AND orderValue < 100000',
    'Active');

-- Verify
SELECT ruleid, description, status FROM suitability_rule ORDER BY ruleid;
-- ============================================================
