-- ============================================================
--  WealthPro — KYC Database Seed Data
--  Database : kyc
--  Run this ONCE after starting the Wealthpro service
--  (Hibernate creates tables on startup via ddl-auto)
-- ============================================================

USE kyc;

-- Disable FK checks for clean reset
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE risk_profile;
TRUNCATE TABLE kycdocument;
TRUNCATE TABLE suitability_rule;
TRUNCATE TABLE client;

-- Reset auto-increment
ALTER TABLE client           AUTO_INCREMENT = 1;
ALTER TABLE kycdocument      AUTO_INCREMENT = 1;
ALTER TABLE risk_profile     AUTO_INCREMENT = 1;
ALTER TABLE suitability_rule AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ── 1. client ────────────────────────────────────────────────
INSERT INTO client (clientid, username, name, dob, contact_info, segment, status) VALUES
(1,  'client1',  'Priya Sharma',   '1990-05-15', '{"email":"priya.sharma@gmail.com","phone":"9123456781","address":"Andheri West, Mumbai"}',          'Retail', 'Active'),
(2,  'client2',  'Rohan Verma',    '1985-03-22', '{"email":"rohan.verma@gmail.com","phone":"9123456782","address":"Bandra, Mumbai"}',                  'HNI',    'Active'),
(3,  'client3',  'Anjali Singh',   '1992-07-10', '{"email":"anjali.singh@gmail.com","phone":"9123456783","address":"Koramangala, Bengaluru"}',          'Retail', 'Active'),
(4,  'client4',  'Vikram Patel',   '1978-11-30', '{"email":"vikram.patel@gmail.com","phone":"9123456784","address":"Juhu, Mumbai"}',                   'UHNI',   'Active'),
(5,  'client5',  'Deepa Nambiar',  '1988-09-05', '{"email":"deepa.nambiar@gmail.com","phone":"9123456785","address":"Indiranagar, Bengaluru"}',         'HNI',    'Active'),
(6,  'client6',  'Arjun Kapoor',   '1995-01-18', '{"email":"arjun.kapoor@gmail.com","phone":"9123456786","address":"Powai, Mumbai"}',                  'Retail', 'Active'),
(7,  'client7',  'Sneha Reddy',    '1983-06-25', '{"email":"sneha.reddy@gmail.com","phone":"9123456787","address":"Jubilee Hills, Hyderabad"}',         'HNI',    'Active'),
(8,  'client8',  'Rahul Gupta',    '1975-12-08', '{"email":"rahul.gupta@gmail.com","phone":"9123456788","address":"Defence Colony, New Delhi"}',        'UHNI',   'Active'),
(9,  'client9',  'Meera Joshi',    '1993-04-14', '{"email":"meera.joshi@gmail.com","phone":"9123456789","address":"Viman Nagar, Pune"}',               'Retail', 'Inactive'),
(10, 'client10', 'Kiran Bhat',     '1980-08-20', '{"email":"kiran.bhat@gmail.com","phone":"9123456790","address":"Whitefield, Bengaluru"}',             'HNI',    'Active');

-- ── 2. kycdocument ───────────────────────────────────────────
INSERT INTO kycdocument (kycid, client_id, document_type, document_ref, document_ref_number, verified_date, status) VALUES
(1,  1,  'AADHAAR',    '/uploads/kyc/1_aadhaar.pdf',   '1234-5678-9001', '2024-01-10', 'Verified'),
(2,  2,  'PAN',        '/uploads/kyc/2_pan.pdf',        'ABCDE1234F',     '2024-01-15', 'Verified'),
(3,  3,  'AADHAAR',    '/uploads/kyc/3_aadhaar.pdf',   '2345-6789-0012', '2024-02-05', 'Verified'),
(4,  4,  'PASSPORT',   '/uploads/kyc/4_passport.pdf',  'J8901234',       '2024-02-20', 'Verified'),
(5,  5,  'PAN',        '/uploads/kyc/5_pan.pdf',        'FGHIJ5678K',     '2024-03-01', 'Verified'),
(6,  6,  'AADHAAR',    '/uploads/kyc/6_aadhaar.pdf',   '3456-7890-1123', '2024-03-15', 'Verified'),
(7,  7,  'PAN',        '/uploads/kyc/7_pan.pdf',        'KLMNO9012P',     '2024-04-10', 'Verified'),
(8,  8,  'PASSPORT',   '/uploads/kyc/8_passport.pdf',  'K1234567',       '2024-04-25', 'Verified'),
(9,  9,  'AADHAAR',    '/uploads/kyc/9_aadhaar.pdf',   '4567-8901-2234', NULL,         'Pending'),
(10, 10, 'PAN',        '/uploads/kyc/10_pan.pdf',       'PQRST3456Q',     '2024-05-20', 'Verified');

-- ── 3. risk_profile ──────────────────────────────────────────
INSERT INTO risk_profile (riskid, client_id, questionnairejson, risk_score, risk_class, assessed_date) VALUES
(1,  1,  '{"q1":"B","q2":"B","q3":"C","q4":"B","q5":"A"}', 50.00, 'Balanced',     '2024-01-12'),
(2,  2,  '{"q1":"C","q2":"C","q3":"D","q4":"C","q5":"B"}', 72.00, 'Aggressive',   '2024-01-18'),
(3,  3,  '{"q1":"A","q2":"B","q3":"A","q4":"A","q5":"A"}', 30.00, 'Conservative', '2024-02-08'),
(4,  4,  '{"q1":"D","q2":"D","q3":"D","q4":"C","q5":"D"}', 88.00, 'Aggressive',   '2024-02-22'),
(5,  5,  '{"q1":"B","q2":"C","q3":"B","q4":"B","q5":"B"}', 58.00, 'Balanced',     '2024-03-05'),
(6,  6,  '{"q1":"A","q2":"A","q3":"B","q4":"A","q5":"A"}', 22.00, 'Conservative', '2024-03-18'),
(7,  7,  '{"q1":"C","q2":"B","q3":"C","q4":"C","q5":"B"}', 64.00, 'Balanced',     '2024-04-12'),
(8,  8,  '{"q1":"D","q2":"D","q3":"C","q4":"D","q5":"C"}', 82.00, 'Aggressive',   '2024-04-28'),
(9,  9,  '{"q1":"A","q2":"A","q3":"A","q4":"B","q5":"A"}', 18.00, 'Conservative', '2024-05-02'),
(10, 10, '{"q1":"B","q2":"C","q3":"B","q4":"C","q5":"B"}', 62.00, 'Balanced',     '2024-05-22');

-- ── 4. suitability_rule ──────────────────────────────────────
-- Only the 6 rules that have all required context variables available.
-- Expression returns TRUE = rule TRIGGERED = order REJECTED.
-- Available context: riskClass, assetClass, side, priceType,
--                    quantity, segment, status, orderValue, currency
INSERT INTO suitability_rule (ruleid, description, expression, status) VALUES
(1, 'Conservative clients cannot buy equity securities',
    'riskClass == CONSERVATIVE AND side == BUY AND assetClass == EQUITY',
    'Active'),
(2, 'Order quantity must not exceed 10000 units',
    'quantity > 10000',
    'Active'),
(3, 'HNI clients minimum investment must be above 1 lakh',
    'segment == HNI AND orderValue < 100000',
    'Active'),
(7, 'UHNI clients minimum investment must be above 10 lakhs',
    'segment == UHNI AND orderValue < 1000000',
    'Active'),
(4, 'Mutual fund subscription minimum 1000 units',
    'assetClass == MUTUAL_FUND AND side == BUY AND quantity < 1000',
    'Active'),
(5, 'ETF order must use LIMIT or MARKET price type only',
    'assetClass == ETF AND priceType != LIMIT AND priceType != MARKET',
    'Active'),
(6, 'Structured product restricted to UHNI clients only',
    'assetClass == STRUCTURED AND segment != UHNI',
    'Active');

-- ── Verify ───────────────────────────────────────────────────
SELECT 'client'           AS tbl, COUNT(*) AS row_count FROM client
UNION ALL
SELECT 'kycdocument',     COUNT(*) FROM kycdocument
UNION ALL
SELECT 'risk_profile',    COUNT(*) FROM risk_profile
UNION ALL
SELECT 'suitability_rule',COUNT(*) FROM suitability_rule;
-- ============================================================
