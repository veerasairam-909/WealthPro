-- ============================================================
--  WealthPro — DUMMY DATA POPULATION SCRIPT
--  Adds ~30 new rows per table while preserving existing data.
--  Run with:  mysql -u root -proot < populate_dummy_data.sql
--  Idempotent-ish — uses explicit IDs above existing MAX values.
-- ============================================================

-- ─────────────────────────────────────────────────
-- 1. KYC.CLIENT — 30 new clients (IDs 13-42)
-- ─────────────────────────────────────────────────
USE kyc;

INSERT INTO client (clientid, name, dob, segment, status, contact_info, username) VALUES
(13, 'Suresh Iyer',       '1968-04-12', 'HNI',    'Active',      '{"email":"suresh.iyer@gmail.com","phone":"9123450013"}',     'client13'),
(14, 'Pooja Sharma',      '1985-11-22', 'Retail', 'Active',      '{"email":"pooja.sharma@gmail.com","phone":"9123450014"}',    'client14'),
(15, 'Karan Mehta',       '1972-07-08', 'UHNI',   'Active',      '{"email":"karan.mehta@gmail.com","phone":"9123450015"}',     'client15'),
(16, 'Nisha Agarwal',     '1990-02-14', 'HNI',    'Active',      '{"email":"nisha.agarwal@gmail.com","phone":"9123450016"}',   'client16'),
(17, 'Manoj Saxena',      '1979-09-30', 'Retail', 'Active',      '{"email":"manoj.saxena@gmail.com","phone":"9123450017"}',    'client17'),
(18, 'Lakshmi Pillai',    '1965-03-18', 'UHNI',   'Active',      '{"email":"lakshmi.pillai@gmail.com","phone":"9123450018"}',  'client18'),
(19, 'Rajesh Kumar',      '1982-12-05', 'HNI',    'Active',      '{"email":"rajesh.kumar@gmail.com","phone":"9123450019"}',    'client19'),
(20, 'Aarti Khanna',      '1988-06-25', 'Retail', 'Active',      '{"email":"aarti.khanna@gmail.com","phone":"9123450020"}',    'client20'),
(21, 'Vivek Bansal',      '1970-10-11', 'UHNI',   'Active',      '{"email":"vivek.bansal@gmail.com","phone":"9123450021"}',    'client21'),
(22, 'Shilpa Menon',      '1992-01-03', 'HNI',    'Active',      '{"email":"shilpa.menon@gmail.com","phone":"9123450022"}',    'client22'),
(23, 'Ashok Rao',         '1975-08-19', 'Retail', 'Active',      '{"email":"ashok.rao@gmail.com","phone":"9123450023"}',       'client23'),
(24, 'Divya Choudhary',   '1986-05-27', 'HNI',    'Active',      '{"email":"divya.choudhary@gmail.com","phone":"9123450024"}', 'client24'),
(25, 'Sunil Joshi',       '1969-11-14', 'UHNI',   'Active',      '{"email":"sunil.joshi@gmail.com","phone":"9123450025"}',     'client25'),
(26, 'Ritu Malhotra',     '1991-04-08', 'Retail', 'Active',      '{"email":"ritu.malhotra@gmail.com","phone":"9123450026"}',   'client26'),
(27, 'Amit Trivedi',      '1980-07-22', 'HNI',    'Active',      '{"email":"amit.trivedi@gmail.com","phone":"9123450027"}',    'client27'),
(28, 'Geeta Rangan',      '1973-02-16', 'UHNI',   'Active',      '{"email":"geeta.rangan@gmail.com","phone":"9123450028"}',    'client28'),
(29, 'Prakash Naidu',     '1984-09-09', 'Retail', 'Active',      '{"email":"prakash.naidu@gmail.com","phone":"9123450029"}',   'client29'),
(30, 'Kavita Sinha',      '1987-12-21', 'HNI',    'Active',      '{"email":"kavita.sinha@gmail.com","phone":"9123450030"}',    'client30'),
(31, 'Harshad Desai',     '1971-06-04', 'UHNI',   'Active',      '{"email":"harshad.desai@gmail.com","phone":"9123450031"}',   'client31'),
(32, 'Neha Bhattacharya', '1989-03-26', 'Retail', 'Active',      '{"email":"neha.bhatta@gmail.com","phone":"9123450032"}',     'client32'),
(33, 'Sandeep Goyal',     '1976-10-13', 'HNI',    'Active',      '{"email":"sandeep.goyal@gmail.com","phone":"9123450033"}',   'client33'),
(34, 'Mansi Kapur',       '1993-08-29', 'Retail', 'Active',      '{"email":"mansi.kapur@gmail.com","phone":"9123450034"}',     'client34'),
(35, 'Devraj Singh',      '1967-01-20', 'UHNI',   'Active',      '{"email":"devraj.singh@gmail.com","phone":"9123450035"}',    'client35'),
(36, 'Anita Krishnan',    '1981-05-17', 'HNI',    'Active',      '{"email":"anita.krishnan@gmail.com","phone":"9123450036"}',  'client36'),
(37, 'Bhavesh Shah',      '1974-11-02', 'Retail', 'Inactive',    '{"email":"bhavesh.shah@gmail.com","phone":"9123450037"}',    'client37'),
(38, 'Tara Mishra',       '1990-07-15', 'HNI',    'Active',      '{"email":"tara.mishra@gmail.com","phone":"9123450038"}',     'client38'),
(39, 'Rohit Aggarwal',    '1983-04-09', 'UHNI',   'Active',      '{"email":"rohit.aggarwal@gmail.com","phone":"9123450039"}',  'client39'),
(40, 'Sapna Verma',       '1986-09-23', 'Retail', 'PENDING_KYC', '{"email":"sapna.verma@gmail.com","phone":"9123450040"}',     'client40'),
(41, 'Dinesh Pandey',     '1972-12-31', 'HNI',    'Active',      '{"email":"dinesh.pandey@gmail.com","phone":"9123450041"}',   'client41'),
(42, 'Komal Sethi',       '1991-02-07', 'Retail', 'Active',      '{"email":"komal.sethi@gmail.com","phone":"9123450042"}',     'client42');

-- ─────────────────────────────────────────────────
-- 2. KYC.KYCDOCUMENT — 30 new docs for new clients
-- ─────────────────────────────────────────────────
INSERT INTO kycdocument (kycid, document_type, document_ref, document_ref_number, status, verified_date, client_id) VALUES
(11, 'AADHAAR',  '/uploads/client_13/aadhaar.pdf',  'XXXX-XXXX-1313', 'Verified', '2025-04-10', 13),
(12, 'PAN',      '/uploads/client_13/pan.pdf',      'ABCDE1313F',     'Verified', '2025-04-10', 13),
(13, 'AADHAAR',  '/uploads/client_14/aadhaar.pdf',  'XXXX-XXXX-1414', 'Verified', '2025-05-02', 14),
(14, 'PASSPORT', '/uploads/client_15/passport.pdf', 'M1234567',       'Verified', '2025-04-22', 15),
(15, 'PAN',      '/uploads/client_15/pan.pdf',      'ABCDE1515G',     'Verified', '2025-04-22', 15),
(16, 'AADHAAR',  '/uploads/client_16/aadhaar.pdf',  'XXXX-XXXX-1616', 'Verified', '2025-05-15', 16),
(17, 'PAN',      '/uploads/client_17/pan.pdf',      'ABCDE1717H',     'Verified', '2025-06-01', 17),
(18, 'AADHAAR',  '/uploads/client_18/aadhaar.pdf',  'XXXX-XXXX-1818', 'Verified', '2025-06-08', 18),
(19, 'PAN',      '/uploads/client_19/pan.pdf',      'ABCDE1919J',     'Verified', '2025-06-20', 19),
(20, 'AADHAAR',  '/uploads/client_20/aadhaar.pdf',  'XXXX-XXXX-2020', 'Verified', '2025-07-03', 20),
(21, 'PAN',      '/uploads/client_21/pan.pdf',      'ABCDE2121K',     'Verified', '2025-07-15', 21),
(22, 'AADHAAR',  '/uploads/client_22/aadhaar.pdf',  'XXXX-XXXX-2222', 'Verified', '2025-08-01', 22),
(23, 'PAN',      '/uploads/client_23/pan.pdf',      'ABCDE2323L',     'Verified', '2025-08-12', 23),
(24, 'AADHAAR',  '/uploads/client_24/aadhaar.pdf',  'XXXX-XXXX-2424', 'Verified', '2025-08-25', 24),
(25, 'PAN',      '/uploads/client_25/pan.pdf',      'ABCDE2525M',     'Verified', '2025-09-05', 25),
(26, 'AADHAAR',  '/uploads/client_26/aadhaar.pdf',  'XXXX-XXXX-2626', 'Verified', '2025-09-18', 26),
(27, 'PAN',      '/uploads/client_27/pan.pdf',      'ABCDE2727N',     'Verified', '2025-10-02', 27),
(28, 'AADHAAR',  '/uploads/client_28/aadhaar.pdf',  'XXXX-XXXX-2828', 'Verified', '2025-10-15', 28),
(29, 'PAN',      '/uploads/client_29/pan.pdf',      'ABCDE2929P',     'Verified', '2025-11-01', 29),
(30, 'AADHAAR',  '/uploads/client_30/aadhaar.pdf',  'XXXX-XXXX-3030', 'Verified', '2025-11-12', 30),
(31, 'PAN',      '/uploads/client_31/pan.pdf',      'ABCDE3131Q',     'Verified', '2025-12-01', 31),
(32, 'AADHAAR',  '/uploads/client_32/aadhaar.pdf',  'XXXX-XXXX-3232', 'Verified', '2025-12-15', 32),
(33, 'PAN',      '/uploads/client_33/pan.pdf',      'ABCDE3333R',     'Verified', '2026-01-05', 33),
(34, 'AADHAAR',  '/uploads/client_34/aadhaar.pdf',  'XXXX-XXXX-3434', 'Verified', '2026-01-18', 34),
(35, 'PAN',      '/uploads/client_35/pan.pdf',      'ABCDE3535S',     'Verified', '2026-02-02', 35),
(36, 'AADHAAR',  '/uploads/client_36/aadhaar.pdf',  'XXXX-XXXX-3636', 'Verified', '2026-02-15', 36),
(37, 'PAN',      '/uploads/client_38/pan.pdf',      'ABCDE3838T',     'Verified', '2026-03-01', 38),
(38, 'AADHAAR',  '/uploads/client_39/aadhaar.pdf',  'XXXX-XXXX-3939', 'Verified', '2026-03-15', 39),
(39, 'PAN',      '/uploads/client_40/pan.pdf',      'ABCDE4040U',     'Pending',  NULL,         40),
(40, 'AADHAAR',  '/uploads/client_41/aadhaar.pdf',  'XXXX-XXXX-4141', 'Verified', '2026-04-01', 41);

-- ─────────────────────────────────────────────────
-- 3. KYC.RISK_PROFILE — 30 new (clients 13-42, UNIQUE per client)
-- ─────────────────────────────────────────────────
INSERT INTO risk_profile (riskid, client_id, risk_class, risk_score, assessed_date, questionnairejson) VALUES
(11, 13, 'Balanced',     58.50, '2025-04-12', '{"q1":"B","q2":"C","q3":"A","q4":"B","q5":"C"}'),
(12, 14, 'Conservative', 32.00, '2025-05-04', '{"q1":"A","q2":"A","q3":"B","q4":"A","q5":"A"}'),
(13, 15, 'Aggressive',   85.00, '2025-04-25', '{"q1":"D","q2":"D","q3":"D","q4":"C","q5":"D"}'),
(14, 16, 'Balanced',     62.50, '2025-05-18', '{"q1":"C","q2":"B","q3":"C","q4":"B","q5":"C"}'),
(15, 17, 'Conservative', 38.00, '2025-06-03', '{"q1":"A","q2":"B","q3":"A","q4":"A","q5":"B"}'),
(16, 18, 'Aggressive',   88.50, '2025-06-10', '{"q1":"D","q2":"D","q3":"D","q4":"D","q5":"D"}'),
(17, 19, 'Balanced',     55.00, '2025-06-22', '{"q1":"B","q2":"C","q3":"B","q4":"C","q5":"B"}'),
(18, 20, 'Conservative', 28.50, '2025-07-05', '{"q1":"A","q2":"A","q3":"A","q4":"B","q5":"A"}'),
(19, 21, 'Aggressive',   91.00, '2025-07-17', '{"q1":"D","q2":"D","q3":"C","q4":"D","q5":"D"}'),
(20, 22, 'Balanced',     65.00, '2025-08-03', '{"q1":"C","q2":"C","q3":"B","q4":"C","q5":"C"}'),
(21, 23, 'Conservative', 42.00, '2025-08-14', '{"q1":"B","q2":"A","q3":"B","q4":"A","q5":"B"}'),
(22, 24, 'Balanced',     60.00, '2025-08-27', '{"q1":"B","q2":"C","q3":"C","q4":"B","q5":"C"}'),
(23, 25, 'Aggressive',   82.00, '2025-09-07', '{"q1":"D","q2":"C","q3":"D","q4":"D","q5":"C"}'),
(24, 26, 'Conservative', 35.00, '2025-09-20', '{"q1":"A","q2":"B","q3":"A","q4":"A","q5":"A"}'),
(25, 27, 'Balanced',     57.00, '2025-10-04', '{"q1":"C","q2":"B","q3":"B","q4":"C","q5":"B"}'),
(26, 28, 'Aggressive',   89.00, '2025-10-17', '{"q1":"D","q2":"D","q3":"D","q4":"C","q5":"D"}'),
(27, 29, 'Conservative', 30.00, '2025-11-03', '{"q1":"A","q2":"A","q3":"A","q4":"A","q5":"B"}'),
(28, 30, 'Balanced',     63.00, '2025-11-14', '{"q1":"B","q2":"C","q3":"C","q4":"B","q5":"C"}'),
(29, 31, 'Aggressive',   86.50, '2025-12-03', '{"q1":"D","q2":"D","q3":"C","q4":"D","q5":"C"}'),
(30, 32, 'Conservative', 36.00, '2025-12-17', '{"q1":"A","q2":"B","q3":"A","q4":"B","q5":"A"}'),
(31, 33, 'Balanced',     59.00, '2026-01-07', '{"q1":"B","q2":"C","q3":"B","q4":"C","q5":"B"}'),
(32, 34, 'Conservative', 33.50, '2026-01-20', '{"q1":"A","q2":"A","q3":"B","q4":"A","q5":"A"}'),
(33, 35, 'Aggressive',   90.00, '2026-02-04', '{"q1":"D","q2":"D","q3":"D","q4":"D","q5":"D"}'),
(34, 36, 'Balanced',     61.50, '2026-02-17', '{"q1":"C","q2":"B","q3":"C","q4":"B","q5":"C"}'),
(35, 37, 'Conservative', 40.00, '2026-03-03', '{"q1":"A","q2":"B","q3":"A","q4":"B","q5":"A"}'),
(36, 38, 'Balanced',     56.00, '2026-03-17', '{"q1":"B","q2":"C","q3":"B","q4":"C","q5":"B"}'),
(37, 39, 'Aggressive',   84.00, '2026-04-01', '{"q1":"C","q2":"D","q3":"D","q4":"C","q5":"D"}'),
(38, 41, 'Balanced',     64.00, '2026-04-03', '{"q1":"C","q2":"C","q3":"B","q4":"C","q5":"C"}'),
(39, 42, 'Conservative', 31.00, '2026-04-12', '{"q1":"A","q2":"A","q3":"A","q4":"B","q5":"A"}');

-- ─────────────────────────────────────────────────
-- 4. KYC.SUITABILITY_RULE — 25 new rules
-- ─────────────────────────────────────────────────
INSERT INTO suitability_rule (ruleid, description, expression, status) VALUES
(11, 'Concentration: Single equity not more than 25% for Balanced',  'asset_class==EQUITY && weight<=0.25 && risk_class==Balanced',     'Active'),
(12, 'Concentration: Single equity not more than 15% for Conservative','asset_class==EQUITY && weight<=0.15 && risk_class==Conservative', 'Active'),
(13, 'Restricted: STRUCTURED only for UHNI',                          'asset_class==STRUCTURED && segment==UHNI',                        'Active'),
(14, 'Bond floor: Conservative must have >= 40% bonds',               'asset_class==BOND && weight>=0.40 && risk_class==Conservative',   'Active'),
(15, 'Equity ceiling: Conservative must have <= 30% equity',          'asset_class==EQUITY && weight<=0.30 && risk_class==Conservative', 'Active'),
(16, 'Liquidity: At least 5% cash buffer',                            'cash_balance>=0.05*total_value',                                  'Active'),
(17, 'Single MF cap: not more than 35% in one fund',                  'asset_class==MUTUAL_FUND && weight<=0.35',                        'Active'),
(18, 'ETF cap for Retail',                                            'asset_class==ETF && weight<=0.20 && segment==Retail',             'Active'),
(19, 'Sector concentration: Banking <= 30%',                          'sector==Banking && weight<=0.30',                                 'Active'),
(20, 'Inactive client: no orders',                                    'status==Inactive => no_new_orders',                               'Active'),
(21, 'KYC required before order',                                     'kyc_status==Verified',                                            'Active'),
(22, 'Minimum SIP for Retail: 500',                                   'segment==Retail && order_amount>=500',                            'Active'),
(23, 'Bond rating: minimum AA for Conservative',                      'asset_class==BOND && rating>=AA && risk_class==Conservative',     'Active'),
(24, 'Foreign currency cap: 10%',                                     'currency!=INR && weight<=0.10',                                   'Active'),
(25, 'Aggressive may hold up to 80% equity',                          'asset_class==EQUITY && weight<=0.80 && risk_class==Aggressive',   'Active'),
(26, 'Trust account: no STRUCTURED',                                  'account_type==TRUST && asset_class!=STRUCTURED',                  'Active'),
(27, 'Joint account: requires both signatures',                       'account_type==JOINT => requires_dual_auth',                       'Active'),
(28, 'Retail: max 5 lakh single trade',                               'segment==Retail && order_amount<=500000',                         'Active'),
(29, 'HNI: min 1 lakh single trade',                                  'segment==HNI && order_amount>=100000',                            'Active'),
(30, 'UHNI: min 10 lakh single trade',                                'segment==UHNI && order_amount>=1000000',                          'Active'),
(31, 'Holding period: ELSS minimum 3 years',                          'product==ELSS => holding_days>=1095',                             'Active'),
(32, 'Tax saver: cap 1.5L per FY',                                    'category==TAX_SAVER && fy_amount<=150000',                        'Active'),
(33, 'Risk score required',                                           'has_risk_profile==true',                                          'Active'),
(34, 'Inactive rule (deprecated)',                                    'old_rule_no_longer_used',                                         'Inactive'),
(35, 'Maximum 50 orders per client per day',                          'daily_order_count<=50',                                           'Active');

-- ─────────────────────────────────────────────────
-- 5. PRODUCTCATALOG.SECURITIES — 30 new (IDs 11-40)
-- ─────────────────────────────────────────────────
USE productcatalog;

INSERT INTO securities (security_id, symbol, asset_class, country, currency, status) VALUES
(11, 'TCS',         'EQUITY',      'India', 'INR', 'ACTIVE'),
(12, 'WIPRO',       'EQUITY',      'India', 'INR', 'ACTIVE'),
(13, 'BAJFINANCE',  'EQUITY',      'India', 'INR', 'ACTIVE'),
(14, 'ASIANPAINT',  'EQUITY',      'India', 'INR', 'ACTIVE'),
(15, 'MARUTI',      'EQUITY',      'India', 'INR', 'ACTIVE'),
(16, 'ITC',         'EQUITY',      'India', 'INR', 'ACTIVE'),
(17, 'BHARTIARTL',  'EQUITY',      'India', 'INR', 'ACTIVE'),
(18, 'AXISBANK',    'EQUITY',      'India', 'INR', 'ACTIVE'),
(19, 'KOTAKBANK',   'EQUITY',      'India', 'INR', 'ACTIVE'),
(20, 'ULTRATECH',   'EQUITY',      'India', 'INR', 'ACTIVE'),
(21, 'NESTLEIND',   'EQUITY',      'India', 'INR', 'ACTIVE'),
(22, 'TITAN',       'EQUITY',      'India', 'INR', 'ACTIVE'),
(23, 'LT',          'EQUITY',      'India', 'INR', 'ACTIVE'),
(24, 'M&M',         'EQUITY',      'India', 'INR', 'ACTIVE'),
(25, 'SUNPHARMA',   'EQUITY',      'India', 'INR', 'ACTIVE'),
(26, 'AXISMIDCAP',  'MUTUAL_FUND', 'India', 'INR', 'ACTIVE'),
(27, 'PARAGFLEX',   'MUTUAL_FUND', 'India', 'INR', 'ACTIVE'),
(28, 'MIRAEELCAP',  'MUTUAL_FUND', 'India', 'INR', 'ACTIVE'),
(29, 'GOLDBEES',    'ETF',         'India', 'INR', 'ACTIVE'),
(30, 'BANKBEES',    'ETF',         'India', 'INR', 'ACTIVE'),
(31, 'JUNIORBEES',  'ETF',         'India', 'INR', 'ACTIVE'),
(32, 'NHAIBOND30',  'BOND',        'India', 'INR', 'ACTIVE'),
(33, 'PFCBOND26',   'BOND',        'India', 'INR', 'ACTIVE'),
(34, 'IRFC2031',    'BOND',        'India', 'INR', 'ACTIVE'),
(35, 'HDFCNIFTY',   'STRUCTURED',  'India', 'INR', 'ACTIVE'),
(36, 'KOTAKEQ',     'STRUCTURED',  'India', 'INR', 'ACTIVE'),
(37, 'COALINDIA',   'EQUITY',      'India', 'INR', 'ACTIVE'),
(38, 'NTPC',        'EQUITY',      'India', 'INR', 'SUSPENDED'),
(39, 'POWERGRID',   'EQUITY',      'India', 'INR', 'ACTIVE'),
(40, 'LEGACY2018',  'BOND',        'India', 'INR', 'INACTIVE');

-- ─────────────────────────────────────────────────
-- 6. PRODUCTCATALOG.PRODUCT_TERMS — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO product_terms (term_id, security_id, term_json, effective_from, effective_to) VALUES
(11, 11, '{"isin":"INE467B01029","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(12, 12, '{"isin":"INE075A01022","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(13, 13, '{"isin":"INE296A01024","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(14, 14, '{"isin":"INE021A01026","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(15, 15, '{"isin":"INE585B01010","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(16, 16, '{"isin":"INE154A01025","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(17, 17, '{"isin":"INE397D01024","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(18, 18, '{"isin":"INE238A01034","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(19, 19, '{"isin":"INE237A01028","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(20, 20, '{"isin":"INE481G01011","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(21, 21, '{"isin":"INE239A01016","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(22, 22, '{"isin":"INE280A01028","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(23, 23, '{"isin":"INE018A01030","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(24, 24, '{"isin":"INE101A01026","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(25, 25, '{"isin":"INE044A01036","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(26, 26, '{"category":"midcap","exit_load":1.0,"min_sip":500}','2024-04-01', NULL),
(27, 27, '{"category":"flexicap","exit_load":1.0,"min_sip":1000}', '2024-04-01', NULL),
(28, 28, '{"category":"largecap","exit_load":0.5,"min_sip":500}',  '2024-04-01', NULL),
(29, 29, '{"underlying":"GOLD","tracking_error":0.05}',       '2024-06-01', NULL),
(30, 30, '{"underlying":"NIFTYBANK","tracking_error":0.07}',  '2024-06-01', NULL),
(31, 31, '{"underlying":"NIFTYNEXT50","tracking_error":0.10}','2024-06-01', NULL),
(32, 32, '{"coupon":7.45,"maturity":"2030-08-15","rating":"AAA"}', '2024-08-15', NULL),
(33, 33, '{"coupon":7.20,"maturity":"2026-12-30","rating":"AAA"}', '2024-12-30', NULL),
(34, 34, '{"coupon":7.85,"maturity":"2031-03-20","rating":"AAA"}', '2025-03-20', NULL),
(35, 35, '{"underlying":"NIFTY50","cap":15.0,"floor":-5.0,"tenor":36}','2024-09-01', NULL),
(36, 36, '{"underlying":"NIFTYBANK","cap":12.0,"floor":-4.0,"tenor":24}','2024-10-01', NULL),
(37, 37, '{"isin":"INE522F01014","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(38, 38, '{"isin":"INE733E01010","lot_size":1,"tick":0.05}',   '2024-01-01', '2026-03-31'),
(39, 39, '{"isin":"INE752E01010","lot_size":1,"tick":0.05}',   '2024-01-01', NULL),
(40, 40, '{"coupon":6.80,"maturity":"2018-12-31","rating":"AA"}','2014-12-31','2018-12-31');

-- ─────────────────────────────────────────────────
-- 7. PRODUCTCATALOG.RESEARCH_NOTES — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO research_notes (note_id, security_id, title, content_uri, rating, published_date) VALUES
(11, 11, 'TCS: Strong Q4 results',                  '/research/tcs-q4-2025.pdf',          'BUY',  '2025-04-22'),
(12, 12, 'WIPRO: Margin pressure persists',         '/research/wipro-margin-2025.pdf',    'HOLD', '2025-05-08'),
(13, 13, 'Bajaj Finance: AUM growth solid',         '/research/baj-aum-2025.pdf',         'BUY',  '2025-05-30'),
(14, 14, 'Asian Paints: Volume recovery',           '/research/asian-vol-2025.pdf',       'BUY',  '2025-06-12'),
(15, 15, 'Maruti Suzuki: SUV mix improves',         '/research/maruti-suv-2025.pdf',      'BUY',  '2025-07-04'),
(16, 16, 'ITC: FMCG margin expansion',              '/research/itc-fmcg-2025.pdf',        'BUY',  '2025-07-22'),
(17, 17, 'Bharti Airtel: 5G monetisation',          '/research/airtel-5g-2025.pdf',       'BUY',  '2025-08-05'),
(18, 18, 'Axis Bank: NIM stable',                   '/research/axis-nim-2025.pdf',        'HOLD', '2025-08-19'),
(19, 19, 'Kotak Bank: Strong asset quality',        '/research/kotak-asset-2025.pdf',     'BUY',  '2025-09-02'),
(20, 20, 'UltraTech: Capacity addition',            '/research/ultratech-capex-2025.pdf', 'BUY',  '2025-09-16'),
(21, 21, 'Nestle: Premiumisation play',             '/research/nestle-prem-2025.pdf',     'HOLD', '2025-10-01'),
(22, 22, 'Titan: Jewellery momentum',               '/research/titan-jewel-2025.pdf',     'BUY',  '2025-10-14'),
(23, 23, 'L&T: Order book record high',             '/research/lt-order-2025.pdf',        'BUY',  '2025-11-03'),
(24, 24, 'M&M: Tractor recovery',                   '/research/mm-tractor-2025.pdf',      'HOLD', '2025-11-18'),
(25, 25, 'Sun Pharma: Specialty growth',            '/research/sun-spec-2025.pdf',        'BUY',  '2025-12-02'),
(26, 26, 'Axis Midcap Fund: review',                '/research/axismidcap-rev-2026.pdf',  'BUY',  '2026-01-08'),
(27, 27, 'Parag Flexi: top quartile',               '/research/paragflex-rev-2026.pdf',   'BUY',  '2026-01-22'),
(28, 28, 'Mirae Largecap: index hugger',            '/research/mirael-rev-2026.pdf',      'HOLD', '2026-02-05'),
(29, 29, 'Gold ETF: hedge against inflation',       '/research/gold-etf-2026.pdf',        'BUY',  '2026-02-19'),
(30, 30, 'Bank ETF: financial sector tailwind',     '/research/bank-etf-2026.pdf',        'BUY',  '2026-03-04'),
(31, 31, 'Junior Bees: small-mid cap basket',       '/research/jr-bees-2026.pdf',         'HOLD', '2026-03-18'),
(32, 32, 'NHAI Bond: AAA + sovereign-link',         '/research/nhai-2026.pdf',            'BUY',  '2026-04-01'),
(33, 33, 'PFC Bond: short tenor + high yield',      '/research/pfc-2026.pdf',             'BUY',  '2026-04-08'),
(34, 34, 'IRFC 2031: long duration play',           '/research/irfc-2026.pdf',            'HOLD', '2026-04-15'),
(35, 35, 'HDFC Nifty Structured: capital protection','/research/hdfc-struct-2026.pdf',    'HOLD', '2026-04-18'),
(36, 36, 'Kotak Equity Structured: review',         '/research/kotak-struct-2026.pdf',    'HOLD', '2026-04-20'),
(37, 37, 'Coal India: dividend yield play',         '/research/coalindia-2026.pdf',       'BUY',  '2026-04-21'),
(38, 38, 'NTPC: regulator suspension impact',       '/research/ntpc-2026.pdf',            'SELL', '2026-04-22'),
(39, 39, 'PowerGrid: stable cash flows',            '/research/powergrid-2026.pdf',       'BUY',  '2026-04-22'),
(40, 11, 'TCS: Q1 26 preview',                      '/research/tcs-q1-26.pdf',            'BUY',  '2026-04-23');

-- ─────────────────────────────────────────────────
-- 8. PBOR.ACCOUNT — 30 new (IDs 11-40, one per new client)
-- ─────────────────────────────────────────────────
USE pbor;

INSERT INTO account (account_id, client_id, account_type, base_currency, status) VALUES
(11, 13, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(12, 14, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(13, 15, 'TRUST',      'INR', 'ACTIVE'),
(14, 16, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(15, 17, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(16, 18, 'TRUST',      'INR', 'ACTIVE'),
(17, 19, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(18, 20, 'JOINT',      'INR', 'ACTIVE'),
(19, 21, 'TRUST',      'INR', 'ACTIVE'),
(20, 22, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(21, 23, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(22, 24, 'JOINT',      'INR', 'ACTIVE'),
(23, 25, 'TRUST',      'INR', 'ACTIVE'),
(24, 26, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(25, 27, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(26, 28, 'TRUST',      'INR', 'ACTIVE'),
(27, 29, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(28, 30, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(29, 31, 'TRUST',      'INR', 'ACTIVE'),
(30, 32, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(31, 33, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(32, 34, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(33, 35, 'TRUST',      'INR', 'ACTIVE'),
(34, 36, 'JOINT',      'INR', 'ACTIVE'),
(35, 37, 'INDIVIDUAL', 'INR', 'INACTIVE'),
(36, 38, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(37, 39, 'TRUST',      'INR', 'ACTIVE'),
(38, 40, 'INDIVIDUAL', 'INR', 'INACTIVE'),
(39, 41, 'INDIVIDUAL', 'INR', 'ACTIVE'),
(40, 42, 'INDIVIDUAL', 'INR', 'ACTIVE');

-- ─────────────────────────────────────────────────
-- 9. PBOR.HOLDING — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO holding (holding_id, account_id, security_id, quantity, avg_cost, valuation_currency, last_valuation_date) VALUES
(11, 11, 11, 100,  3450.5000, 'INR', '2026-04-20'),
(12, 11, 16, 200,  445.7500,  'INR', '2026-04-20'),
(13, 12, 1,  50,   1620.5000, 'INR', '2026-04-20'),
(14, 12, 7,  150,  215.0000,  'INR', '2026-04-20'),
(15, 13, 13, 75,   6800.0000, 'INR', '2026-04-20'),
(16, 13, 35, 100,  10250.0000,'INR', '2026-04-20'),
(17, 14, 18, 80,   1085.0000, 'INR', '2026-04-20'),
(18, 14, 26, 500,  88.5000,   'INR', '2026-04-20'),
(19, 15, 4,  60,   145.7500,  'INR', '2026-04-20'),
(20, 16, 36, 50,   10500.0000,'INR', '2026-04-20'),
(21, 17, 19, 100,  1875.0000, 'INR', '2026-04-20'),
(22, 18, 27, 1000, 65.2500,   'INR', '2026-04-20'),
(23, 19, 23, 40,   3500.5000, 'INR', '2026-04-20'),
(24, 20, 32, 100,  10300.0000,'INR', '2026-04-20'),
(25, 21, 14, 30,   2980.0000, 'INR', '2026-04-20'),
(26, 22, 28, 600,  72.0000,   'INR', '2026-04-20'),
(27, 23, 5,  100,  9850.0000, 'INR', '2026-04-20'),
(28, 24, 15, 25,   10500.7500,'INR', '2026-04-20'),
(29, 25, 22, 50,   3320.5000, 'INR', '2026-04-20'),
(30, 26, 17, 200,  1130.2500, 'INR', '2026-04-20'),
(31, 27, 11, 75,   3500.0000, 'INR', '2026-04-20'),
(32, 28, 33, 50,   10220.0000,'INR', '2026-04-20'),
(33, 29, 25, 60,   1465.5000, 'INR', '2026-04-20'),
(34, 30, 30, 300,  450.7500,  'INR', '2026-04-20'),
(35, 31, 24, 80,   1450.0000, 'INR', '2026-04-20'),
(36, 32, 21, 40,   2270.0000, 'INR', '2026-04-20'),
(37, 33, 34, 25,   10180.0000,'INR', '2026-04-20'),
(38, 34, 29, 200,  60.5000,   'INR', '2026-04-20'),
(39, 36, 20, 30,   8950.0000, 'INR', '2026-04-20'),
(40, 39, 12, 90,   480.0000,  'INR', '2026-04-20');

-- ─────────────────────────────────────────────────
-- 10. PBOR.CASH_LEDGER — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO cash_ledger (ledger_id, account_id, txn_type, amount, currency, narrative, txn_date) VALUES
(11, 11, 'SUBSCRIPTION', 500000.00,  'INR', 'Initial deposit',                    '2025-04-12'),
(12, 11, 'DIVIDEND',     12500.00,   'INR', 'TCS interim dividend',               '2025-08-15'),
(13, 12, 'SUBSCRIPTION', 200000.00,  'INR', 'Initial deposit',                    '2025-05-04'),
(14, 13, 'SUBSCRIPTION', 5000000.00, 'INR', 'Trust funding round 1',              '2025-04-25'),
(15, 14, 'SUBSCRIPTION', 350000.00,  'INR', 'Initial deposit',                    '2025-05-18'),
(16, 14, 'FEE',          -1500.00,   'INR', 'Quarterly advisory fee',             '2025-09-30'),
(17, 15, 'SUBSCRIPTION', 150000.00,  'INR', 'Initial deposit',                    '2025-06-03'),
(18, 16, 'SUBSCRIPTION', 8000000.00, 'INR', 'UHNI account funding',               '2025-06-10'),
(19, 17, 'DIVIDEND',     8200.00,    'INR', 'Kotak Bank dividend',                '2025-08-22'),
(20, 18, 'SUBSCRIPTION', 100000.00,  'INR', 'Top-up',                             '2025-07-20'),
(21, 19, 'SUBSCRIPTION', 600000.00,  'INR', 'Initial deposit',                    '2025-07-28'),
(22, 20, 'REDEMPTION',   -75000.00,  'INR', 'Partial redemption mutual fund',     '2025-08-15'),
(23, 21, 'DIVIDEND',     22000.00,   'INR', 'L&T dividend',                       '2025-09-01'),
(24, 22, 'SUBSCRIPTION', 250000.00,  'INR', 'Top-up',                             '2025-09-05'),
(25, 23, 'FEE',          -3500.00,   'INR', 'Half-yearly fee',                    '2025-09-30'),
(26, 24, 'SUBSCRIPTION', 450000.00,  'INR', 'Initial deposit',                    '2025-09-20'),
(27, 25, 'DIVIDEND',     6500.00,    'INR', 'Titan dividend',                     '2025-10-15'),
(28, 26, 'SUBSCRIPTION', 180000.00,  'INR', 'Initial deposit',                    '2025-09-22'),
(29, 27, 'REDEMPTION',   -50000.00,  'INR', 'Cash withdrawal',                    '2025-10-30'),
(30, 28, 'SUBSCRIPTION', 4000000.00, 'INR', 'Trust funding',                      '2025-10-17'),
(31, 29, 'DIVIDEND',     4200.00,    'INR', 'Sun Pharma dividend',                '2025-11-15'),
(32, 30, 'SUBSCRIPTION', 250000.00,  'INR', 'Initial deposit',                    '2025-11-15'),
(33, 31, 'SUBSCRIPTION', 12000000.00,'INR', 'UHNI Trust setup',                   '2025-12-05'),
(34, 32, 'FEE',          -1200.00,   'INR', 'Quarterly fee',                      '2025-12-31'),
(35, 33, 'DIVIDEND',     11000.00,   'INR', 'PFC bond coupon',                    '2026-01-15'),
(36, 34, 'SUBSCRIPTION', 380000.00,  'INR', 'Initial deposit',                    '2026-01-22'),
(37, 36, 'SUBSCRIPTION', 220000.00,  'INR', 'Initial deposit',                    '2026-02-19'),
(38, 37, 'SUBSCRIPTION', 6500000.00, 'INR', 'UHNI Trust funding',                 '2026-03-17'),
(39, 39, 'SUBSCRIPTION', 320000.00,  'INR', 'Initial deposit',                    '2026-04-03'),
(40, 40, 'SUBSCRIPTION', 175000.00,  'INR', 'Initial deposit',                    '2026-04-15');

-- ─────────────────────────────────────────────────
-- 11. PBOR.CORPORATE_ACTION — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO corporate_action (ca_id, security_id, ca_type, ex_date, record_date, pay_date, terms_json) VALUES
(11, 11, 'DIVIDEND',  '2025-07-25', '2025-07-26', '2025-08-15', '{"per_share":71.0}'),
(12, 12, 'DIVIDEND',  '2025-08-05', '2025-08-06', '2025-08-25', '{"per_share":5.0}'),
(13, 13, 'BONUS',     '2025-09-10', '2025-09-11', '2025-09-25', '{"ratio":"1:1"}'),
(14, 14, 'DIVIDEND',  '2025-08-20', '2025-08-21', '2025-09-10', '{"per_share":21.5}'),
(15, 15, 'SPLIT',     '2025-10-05', '2025-10-06', '2025-10-15', '{"ratio":"5:1"}'),
(16, 16, 'DIVIDEND',  '2025-09-25', '2025-09-26', '2025-10-15', '{"per_share":12.5}'),
(17, 17, 'DIVIDEND',  '2025-10-15', '2025-10-16', '2025-11-05', '{"per_share":8.0}'),
(18, 18, 'DIVIDEND',  '2025-08-12', '2025-08-13', '2025-09-02', '{"per_share":14.0}'),
(19, 19, 'BONUS',     '2025-11-20', '2025-11-21', '2025-12-05', '{"ratio":"1:2"}'),
(20, 20, 'DIVIDEND',  '2025-11-30', '2025-12-01', '2025-12-20', '{"per_share":35.0}'),
(21, 21, 'DIVIDEND',  '2025-12-10', '2025-12-11', '2025-12-30', '{"per_share":275.0}'),
(22, 22, 'DIVIDEND',  '2025-09-18', '2025-09-19', '2025-10-08', '{"per_share":11.0}'),
(23, 23, 'DIVIDEND',  '2026-01-15', '2026-01-16', '2026-02-05', '{"per_share":28.0}'),
(24, 24, 'DIVIDEND',  '2025-10-25', '2025-10-26', '2025-11-15', '{"per_share":18.0}'),
(25, 25, 'DIVIDEND',  '2026-02-10', '2026-02-11', '2026-03-03', '{"per_share":7.5}'),
(26, 32, 'COUPON',    '2026-02-15', '2026-02-16', '2026-03-15', '{"coupon_pct":7.45}'),
(27, 33, 'COUPON',    '2026-03-30', '2026-03-31', '2026-04-15', '{"coupon_pct":7.20}'),
(28, 34, 'COUPON',    '2025-09-20', '2025-09-21', '2025-10-15', '{"coupon_pct":7.85}'),
(29, 5,  'COUPON',    '2025-08-15', '2025-08-16', '2025-09-05', '{"coupon_pct":7.10}'),
(30, 9,  'COUPON',    '2026-01-30', '2026-01-31', '2026-02-20', '{"coupon_pct":7.50}'),
(31, 10, 'REDEMPTION','2026-04-20', '2026-04-21', '2026-05-05', '{"redemption_value":105.0}'),
(32, 1,  'DIVIDEND',  '2025-06-25', '2025-06-26', '2025-07-15', '{"per_share":15.0}'),
(33, 2,  'DIVIDEND',  '2025-07-10', '2025-07-11', '2025-07-30', '{"per_share":9.0}'),
(34, 4,  'DIVIDEND',  '2025-09-05', '2025-09-06', '2025-09-25', '{"per_share":3.6}'),
(35, 6,  'BONUS',     '2025-12-15', '2025-12-16', '2025-12-30', '{"ratio":"1:1"}'),
(36, 37, 'DIVIDEND',  '2026-02-20', '2026-02-21', '2026-03-12', '{"per_share":24.0}'),
(37, 39, 'DIVIDEND',  '2026-03-10', '2026-03-11', '2026-03-30', '{"per_share":4.5}'),
(38, 22, 'DIVIDEND',  '2026-03-25', '2026-03-26', '2026-04-15', '{"per_share":12.0}'),
(39, 13, 'DIVIDEND',  '2026-04-05', '2026-04-06', '2026-04-25', '{"per_share":36.0}'),
(40, 11, 'DIVIDEND',  '2026-04-15', '2026-04-16', '2026-05-05', '{"per_share":78.0}');

-- ─────────────────────────────────────────────────
-- 12. ORDER_EXECUTION_DB.ORDERS — 30 new
-- ─────────────────────────────────────────────────
USE order_execution_db;

INSERT INTO orders (order_id, client_id, security_id, side, quantity, price_type, limit_price, status, routed_venue, order_date) VALUES
(11, 13, 11, 'BUY',       100, 'LIMIT',  3460.0, 'FILLED',           'SIMULATED_NSE', '2025-04-15 10:15:00'),
(12, 13, 16, 'BUY',       200, 'MARKET', NULL,   'FILLED',           'SIMULATED_NSE', '2025-04-15 10:25:00'),
(13, 14, 1,  'BUY',       50,  'LIMIT',  1625.0, 'FILLED',           'SIMULATED_BSE', '2025-05-08 11:00:00'),
(14, 14, 7,  'BUY',       150, 'MARKET', NULL,   'FILLED',           'SIMULATED_BSE', '2025-05-08 11:10:00'),
(15, 15, 13, 'BUY',       75,  'LIMIT',  6810.0, 'FILLED',           'SIMULATED_NSE', '2025-04-28 14:30:00'),
(16, 15, 35, 'SUBSCRIBE', 100, 'NAV',    NULL,   'FILLED',           'SIMULATED_NSE', '2025-04-28 14:45:00'),
(17, 16, 18, 'BUY',       80,  'LIMIT',  1090.0, 'FILLED',           'SIMULATED_NSE', '2025-05-21 09:45:00'),
(18, 16, 26, 'SUBSCRIBE', 500, 'NAV',    NULL,   'FILLED',           'SIMULATED_NSE', '2025-05-21 09:55:00'),
(19, 17, 4,  'BUY',       60,  'MARKET', NULL,   'FILLED',           'SIMULATED_BSE', '2025-06-06 13:00:00'),
(20, 18, 36, 'SUBSCRIBE', 50,  'NAV',    NULL,   'PARTIALLY_FILLED', 'SIMULATED_NSE', '2025-06-13 15:00:00'),
(21, 19, 19, 'BUY',       100, 'LIMIT',  1880.0, 'FILLED',           'SIMULATED_NSE', '2025-06-25 10:00:00'),
(22, 20, 27, 'SUBSCRIBE', 1000,'NAV',    NULL,   'PLACED',           NULL,            '2025-07-08 09:30:00'),
(23, 21, 23, 'BUY',       40,  'LIMIT',  3520.0, 'FILLED',           'SIMULATED_NSE', '2025-07-20 11:30:00'),
(24, 22, 32, 'BUY',       100, 'MARKET', NULL,   'FILLED',           'SIMULATED_BSE', '2025-08-06 12:15:00'),
(25, 23, 14, 'BUY',       30,  'LIMIT',  3000.0, 'CANCELLED',        NULL,            '2025-08-17 14:00:00'),
(26, 24, 28, 'SUBSCRIBE', 600, 'NAV',    NULL,   'FILLED',           'SIMULATED_NSE', '2025-08-30 10:30:00'),
(27, 25, 5,  'BUY',       100, 'LIMIT',  9870.0, 'FILLED',           'SIMULATED_NSE', '2025-09-10 11:45:00'),
(28, 26, 15, 'BUY',       25,  'MARKET', NULL,   'REJECTED',         NULL,            '2025-09-23 13:30:00'),
(29, 27, 22, 'BUY',       50,  'LIMIT',  3325.0, 'FILLED',           'SIMULATED_NSE', '2025-10-07 09:50:00'),
(30, 28, 17, 'BUY',       200, 'MARKET', NULL,   'FILLED',           'SIMULATED_BSE', '2025-10-20 14:20:00'),
(31, 29, 11, 'BUY',       75,  'LIMIT',  3520.0, 'FILLED',           'SIMULATED_NSE', '2025-11-06 10:15:00'),
(32, 30, 33, 'BUY',       50,  'LIMIT',  10250.0,'FILLED',           'SIMULATED_BSE', '2025-11-17 11:00:00'),
(33, 31, 25, 'BUY',       60,  'MARKET', NULL,   'FILLED',           'SIMULATED_NSE', '2025-12-08 12:30:00'),
(34, 32, 30, 'SUBSCRIBE', 300, 'NAV',    NULL,   'FILLED',           'SIMULATED_NSE', '2025-12-19 14:00:00'),
(35, 33, 24, 'BUY',       80,  'LIMIT',  1465.0, 'FILLED',           'SIMULATED_NSE', '2026-01-12 10:30:00'),
(36, 34, 21, 'BUY',       40,  'MARKET', NULL,   'FILLED',           'SIMULATED_BSE', '2026-01-26 11:15:00'),
(37, 35, 34, 'BUY',       25,  'LIMIT',  10200.0,'PARTIALLY_FILLED', 'SIMULATED_NSE', '2026-02-09 13:00:00'),
(38, 36, 29, 'BUY',       200, 'MARKET', NULL,   'FILLED',           'SIMULATED_NSE', '2026-02-23 14:45:00'),
(39, 38, 20, 'BUY',       30,  'LIMIT',  9000.0, 'PLACED',           NULL,            '2026-03-09 10:00:00'),
(40, 41, 12, 'BUY',       90,  'MARKET', NULL,   'ROUTED',           'SIMULATED_NSE', '2026-04-05 09:30:00');

-- ─────────────────────────────────────────────────
-- 13. ORDER_EXECUTION_DB.EXECUTION_FILLS — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO execution_fills (fill_id, order_id, fill_quantity, fill_price, status, venue, fill_date) VALUES
( 8, 11, 100, 3450.50,  'COMPLETED', 'SIMULATED_NSE', '2025-04-15 10:18:00'),
( 9, 12, 200, 445.75,   'COMPLETED', 'SIMULATED_NSE', '2025-04-15 10:28:00'),
(10, 13, 50,  1620.50,  'COMPLETED', 'SIMULATED_BSE', '2025-05-08 11:03:00'),
(11, 14, 150, 215.00,   'COMPLETED', 'SIMULATED_BSE', '2025-05-08 11:13:00'),
(12, 15, 75,  6800.00,  'COMPLETED', 'SIMULATED_NSE', '2025-04-28 14:33:00'),
(13, 16, 100, 10250.00, 'COMPLETED', 'SIMULATED_NSE', '2025-04-28 14:48:00'),
(14, 17, 80,  1085.00,  'COMPLETED', 'SIMULATED_NSE', '2025-05-21 09:48:00'),
(15, 18, 500, 88.50,    'COMPLETED', 'SIMULATED_NSE', '2025-05-21 09:58:00'),
(16, 19, 60,  145.75,   'COMPLETED', 'SIMULATED_BSE', '2025-06-06 13:03:00'),
(17, 20, 30,  10500.00, 'COMPLETED', 'SIMULATED_NSE', '2025-06-13 15:05:00'),
(18, 21, 100, 1875.00,  'COMPLETED', 'SIMULATED_NSE', '2025-06-25 10:03:00'),
(19, 23, 40,  3500.50,  'COMPLETED', 'SIMULATED_NSE', '2025-07-20 11:33:00'),
(20, 24, 100, 10300.00, 'COMPLETED', 'SIMULATED_BSE', '2025-08-06 12:18:00'),
(21, 26, 600, 72.00,    'COMPLETED', 'SIMULATED_NSE', '2025-08-30 10:33:00'),
(22, 27, 100, 9850.00,  'COMPLETED', 'SIMULATED_NSE', '2025-09-10 11:48:00'),
(23, 29, 50,  3320.50,  'COMPLETED', 'SIMULATED_NSE', '2025-10-07 09:53:00'),
(24, 30, 200, 1130.25,  'COMPLETED', 'SIMULATED_BSE', '2025-10-20 14:23:00'),
(25, 31, 75,  3500.00,  'COMPLETED', 'SIMULATED_NSE', '2025-11-06 10:18:00'),
(26, 32, 50,  10220.00, 'COMPLETED', 'SIMULATED_BSE', '2025-11-17 11:03:00'),
(27, 33, 60,  1465.50,  'COMPLETED', 'SIMULATED_NSE', '2025-12-08 12:33:00'),
(28, 34, 300, 450.75,   'COMPLETED', 'SIMULATED_NSE', '2025-12-19 14:03:00'),
(29, 35, 80,  1450.00,  'COMPLETED', 'SIMULATED_NSE', '2026-01-12 10:33:00'),
(30, 36, 40,  2270.00,  'COMPLETED', 'SIMULATED_BSE', '2026-01-26 11:18:00'),
(31, 37, 15,  10180.00, 'COMPLETED', 'SIMULATED_NSE', '2026-02-09 13:03:00'),
(32, 38, 200, 60.50,    'COMPLETED', 'SIMULATED_NSE', '2026-02-23 14:48:00'),
(33, 30, 50,  1131.00,  'COMPLETED', 'SIMULATED_BSE', '2025-10-20 14:25:00'),
(34, 11, 25,  3452.00,  'COMPLETED', 'SIMULATED_NSE', '2025-04-15 10:20:00'),
(35, 21, 30,  1876.00,  'COMPLETED', 'SIMULATED_NSE', '2025-06-25 10:05:00'),
(36, 24, 50,  10302.00, 'COMPLETED', 'SIMULATED_BSE', '2025-08-06 12:20:00'),
(37, 27, 25,  9852.00,  'COMPLETED', 'SIMULATED_NSE', '2025-09-10 11:50:00');

-- ─────────────────────────────────────────────────
-- 14. ORDER_EXECUTION_DB.ALLOCATIONS — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO allocations (allocation_id, order_id, account_id, alloc_quantity, alloc_price, alloc_date) VALUES
( 7, 11, 11, 100,  3450.50,  '2025-04-15 10:30:00'),
( 8, 12, 11, 200,  445.75,   '2025-04-15 10:35:00'),
( 9, 13, 12, 50,   1620.50,  '2025-05-08 11:15:00'),
(10, 14, 12, 150,  215.00,   '2025-05-08 11:20:00'),
(11, 15, 13, 75,   6800.00,  '2025-04-28 14:50:00'),
(12, 16, 13, 100,  10250.00, '2025-04-28 14:55:00'),
(13, 17, 14, 80,   1085.00,  '2025-05-21 10:00:00'),
(14, 18, 14, 500,  88.50,    '2025-05-21 10:05:00'),
(15, 19, 15, 60,   145.75,   '2025-06-06 13:10:00'),
(16, 20, 16, 30,   10500.00, '2025-06-13 15:15:00'),
(17, 21, 17, 100,  1875.00,  '2025-06-25 10:10:00'),
(18, 23, 19, 40,   3500.50,  '2025-07-20 11:40:00'),
(19, 24, 20, 100,  10300.00, '2025-08-06 12:25:00'),
(20, 26, 22, 600,  72.00,    '2025-08-30 10:40:00'),
(21, 27, 23, 100,  9850.00,  '2025-09-10 12:00:00'),
(22, 29, 25, 50,   3320.50,  '2025-10-07 10:00:00'),
(23, 30, 26, 200,  1130.25,  '2025-10-20 14:30:00'),
(24, 31, 27, 75,   3500.00,  '2025-11-06 10:25:00'),
(25, 32, 28, 50,   10220.00, '2025-11-17 11:10:00'),
(26, 33, 29, 60,   1465.50,  '2025-12-08 12:40:00'),
(27, 34, 30, 300,  450.75,   '2025-12-19 14:10:00'),
(28, 35, 31, 80,   1450.00,  '2026-01-12 10:40:00'),
(29, 36, 32, 40,   2270.00,  '2026-01-26 11:25:00'),
(30, 37, 33, 15,   10180.00, '2026-02-09 13:10:00'),
(31, 38, 34, 200,  60.50,    '2026-02-23 14:55:00'),
(32, 11, 11, 25,   3452.00,  '2025-04-15 10:33:00'),
(33, 21, 17, 30,   1876.00,  '2025-06-25 10:13:00'),
(34, 24, 20, 50,   10302.00, '2025-08-06 12:28:00'),
(35, 27, 23, 25,   9852.00,  '2025-09-10 12:05:00'),
(36, 30, 26, 50,   1131.00,  '2025-10-20 14:33:00');

-- ─────────────────────────────────────────────────
-- 15. ORDER_EXECUTION_DB.PRE_TRADE_CHECKS — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO pre_trade_checks (check_id, order_id, check_type, result, message, checked_date) VALUES
(33, 11, 'CASH',        'PASS', 'Sufficient buying power',           '2025-04-15 10:14:00'),
(34, 11, 'SUITABILITY', 'PASS', 'Within risk profile',               '2025-04-15 10:14:30'),
(35, 12, 'CASH',        'PASS', 'Sufficient buying power',           '2025-04-15 10:24:00'),
(36, 13, 'CASH',        'PASS', 'Sufficient buying power',           '2025-05-08 10:59:00'),
(37, 13, 'EXPOSURE',    'PASS', 'Concentration limit OK',            '2025-05-08 10:59:30'),
(38, 15, 'SUITABILITY', 'PASS', 'Aggressive client - high beta OK',  '2025-04-28 14:29:00'),
(39, 16, 'LIMIT',       'PASS', 'Within daily limit',                '2025-04-28 14:44:00'),
(40, 17, 'CASH',        'PASS', 'Sufficient buying power',           '2025-05-21 09:44:00'),
(41, 18, 'SUITABILITY', 'PASS', 'Balanced client - midcap allowed',  '2025-05-21 09:54:00'),
(42, 19, 'CASH',        'PASS', 'Sufficient buying power',           '2025-06-06 12:59:00'),
(43, 20, 'SUITABILITY', 'PASS', 'UHNI - structured allowed',         '2025-06-13 14:59:00'),
(44, 21, 'EXPOSURE',    'PASS', 'Banking sector under 30%',          '2025-06-25 09:59:00'),
(45, 22, 'CASH',        'PASS', 'Pending settlement',                '2025-07-08 09:29:00'),
(46, 23, 'CASH',        'PASS', 'Sufficient buying power',           '2025-07-20 11:29:00'),
(47, 25, 'SUITABILITY', 'FAIL', 'Conservative - equity exceeds 30%', '2025-08-17 13:59:00'),
(48, 26, 'CASH',        'PASS', 'Sufficient buying power',           '2025-08-30 10:29:00'),
(49, 27, 'EXPOSURE',    'PASS', 'Bond exposure within bounds',       '2025-09-10 11:44:00'),
(50, 28, 'SUITABILITY', 'FAIL', 'Retail - single trade exceeds 5L',  '2025-09-23 13:29:00'),
(51, 29, 'CASH',        'PASS', 'Sufficient buying power',           '2025-10-07 09:49:00'),
(52, 30, 'EXPOSURE',    'PASS', 'Telecom sector allowed',            '2025-10-20 14:19:00'),
(53, 31, 'CASH',        'PASS', 'Sufficient buying power',           '2025-11-06 10:14:00'),
(54, 32, 'LIMIT',       'PASS', 'Within order quantity limit',       '2025-11-17 10:59:00'),
(55, 33, 'SUITABILITY', 'PASS', 'Aggressive - pharma allowed',       '2025-12-08 12:29:00'),
(56, 34, 'CASH',        'PASS', 'Sufficient buying power',           '2025-12-19 13:59:00'),
(57, 35, 'EXPOSURE',    'PASS', 'Auto sector under threshold',       '2026-01-12 10:29:00'),
(58, 36, 'CASH',        'PASS', 'Sufficient buying power',           '2026-01-26 11:14:00'),
(59, 37, 'SUITABILITY', 'PASS', 'UHNI - bond allowed',               '2026-02-09 12:59:00'),
(60, 38, 'EXPOSURE',    'PASS', 'ETF allocation within limits',      '2026-02-23 14:44:00'),
(61, 39, 'CASH',        'PASS', 'Sufficient buying power',           '2026-03-09 09:59:00'),
(62, 40, 'CASH',        'PASS', 'Sufficient buying power',           '2026-04-05 09:29:00');

-- ─────────────────────────────────────────────────
-- 16. ANALYTICS_DB.COMPLIANCE_BREACHES — 30 new
-- ─────────────────────────────────────────────────
USE analytics_db;

INSERT INTO compliance_breaches (breach_id, account_id, rule_id, severity, description, status, detected_at, resolved_at) VALUES
(11, 11, 11, 'MEDIUM',   'TCS exposure 28% — exceeds 25% Balanced limit',                'CLOSED',       '2025-08-15 10:00:00', '2025-08-18 09:00:00'),
(12, 13, 13, 'HIGH',     'Trust account holds STRUCTURED — restricted',                  'ACKNOWLEDGED', '2025-09-01 11:30:00', NULL),
(13, 14, 12, 'MEDIUM',   'Conservative client equity 32% — exceeds 30%',                 'OPEN',         '2025-10-12 14:00:00', NULL),
(14, 15, 25, 'LOW',      'Aggressive equity at 78% — within bounds, advisory note',      'CLOSED',       '2025-09-20 10:00:00', '2025-09-22 11:00:00'),
(15, 16, 19, 'HIGH',     'Banking sector concentration 33% — over 30%',                  'ACKNOWLEDGED', '2025-11-05 13:00:00', NULL),
(16, 18, 26, 'CRITICAL', 'Trust account allocated STRUCTURED product',                   'OPEN',         '2025-12-01 09:00:00', NULL),
(17, 19, 18, 'LOW',      'Retail ETF allocation at 22% — over 20%',                      'CLOSED',       '2025-12-10 14:30:00', '2025-12-12 10:00:00'),
(18, 20, 22, 'MEDIUM',   'SIP below minimum 500 for Retail',                             'CLOSED',       '2025-08-20 11:00:00', '2025-08-21 09:00:00'),
(19, 21, 14, 'HIGH',     'Conservative client bond allocation 35% — below 40% min',      'OPEN',         '2026-01-15 10:00:00', NULL),
(20, 22, 17, 'MEDIUM',   'Single MF holding 38% — exceeds 35%',                          'ACKNOWLEDGED', '2026-01-22 13:00:00', NULL),
(21, 23, 11, 'LOW',      'Concentration warning Asian Paints 22%',                       'CLOSED',       '2025-10-25 09:30:00', '2025-10-26 10:00:00'),
(22, 24, 16, 'MEDIUM',   'Cash buffer 3% — below 5% minimum',                            'OPEN',         '2026-02-05 11:00:00', NULL),
(23, 25, 23, 'CRITICAL', 'Conservative bond rating below AA',                            'ACKNOWLEDGED', '2026-02-12 14:00:00', NULL),
(24, 26, 28, 'MEDIUM',   'Retail single trade 6L — exceeds 5L cap',                      'CLOSED',       '2026-01-08 13:30:00', '2026-01-10 10:00:00'),
(25, 27, 25, 'HIGH',     'Aggressive equity 82% — exceeds 80%',                          'OPEN',         '2026-02-20 10:00:00', NULL),
(26, 28, 13, 'CRITICAL', 'UHNI Trust holding STRUCTURED — review needed',                'ACKNOWLEDGED', '2026-03-01 11:00:00', NULL),
(27, 29, 21, 'CRITICAL', 'Order placed before KYC verified',                             'CLOSED',       '2025-11-08 14:00:00', '2025-11-09 09:00:00'),
(28, 30, 11, 'MEDIUM',   'Tata Steel concentration 27% — over 25%',                      'OPEN',         '2026-03-15 13:00:00', NULL),
(29, 31, 30, 'HIGH',     'UHNI single trade 8L — below 10L minimum',                     'CLOSED',       '2025-12-15 10:00:00', '2025-12-16 11:00:00'),
(30, 32, 16, 'LOW',      'Cash drift after dividend',                                    'CLOSED',       '2026-01-05 14:30:00', '2026-01-06 10:00:00'),
(31, 33, 19, 'MEDIUM',   'Banking sector creep at 31%',                                  'ACKNOWLEDGED', '2026-02-18 11:00:00', NULL),
(32, 34, 12, 'HIGH',     'Retail equity 45% — Conservative profile',                     'OPEN',         '2026-03-22 10:30:00', NULL),
(33, 35, 13, 'CRITICAL', 'UHNI Trust attempted STRUCTURED purchase',                     'ACKNOWLEDGED', '2026-04-01 09:00:00', NULL),
(34, 36, 24, 'LOW',      'Foreign currency drift to 12%',                                'CLOSED',       '2026-03-10 13:30:00', '2026-03-12 11:00:00'),
(35, 37, 20, 'CRITICAL', 'Inactive client placed order',                                 'CLOSED',       '2026-04-05 10:00:00', '2026-04-05 11:00:00'),
(36, 39, 11, 'MEDIUM',   'WIPRO 26% concentration',                                      'OPEN',         '2026-04-08 12:00:00', NULL),
(37, 40, 21, 'CRITICAL', 'Pending KYC client attempted purchase',                        'OPEN',         '2026-04-15 09:30:00', NULL),
(38, 31, 35, 'MEDIUM',   'More than 50 orders in a day',                                 'CLOSED',       '2026-01-20 17:00:00', '2026-01-21 09:00:00'),
(39, 22, 32, 'LOW',      'ELSS held under 3 years — tax review',                         'ACKNOWLEDGED', '2026-04-12 11:00:00', NULL),
(40, 28, 27, 'HIGH',     'Joint account missing dual auth',                              'OPEN',         '2026-04-18 14:00:00', NULL);

-- ─────────────────────────────────────────────────
-- 17. ANALYTICS_DB.PERFORMANCE_RECORDS — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO performance_records (record_id, account_id, portfolio_id, period, start_date, end_date, return_percentage, benchmark_return_percentage, calculated_at) VALUES
(11, 11, 11, 'MONTHLY', '2025-04-01', '2025-04-30',  3.45,  2.80, '2025-05-01 00:01:00'),
(12, 11, 11, 'MONTHLY', '2025-05-01', '2025-05-31',  2.10,  1.95, '2025-06-01 00:01:00'),
(13, 12, 12, 'MONTHLY', '2025-05-01', '2025-05-31',  1.85,  1.95, '2025-06-01 00:01:00'),
(14, 13, 13, 'MONTHLY', '2025-05-01', '2025-05-31',  4.20,  3.00, '2025-06-01 00:01:00'),
(15, 14, 14, 'MONTHLY', '2025-06-01', '2025-06-30',  2.75,  2.50, '2025-07-01 00:01:00'),
(16, 15, 15, 'MONTHLY', '2025-06-01', '2025-06-30',  1.20,  1.95, '2025-07-01 00:01:00'),
(17, 16, 16, 'MONTHLY', '2025-07-01', '2025-07-31',  3.60,  3.15, '2025-08-01 00:01:00'),
(18, 17, 17, 'MONTHLY', '2025-07-01', '2025-07-31',  2.90,  2.50, '2025-08-01 00:01:00'),
(19, 18, 18, 'MONTHLY', '2025-08-01', '2025-08-31',  1.45,  1.80, '2025-09-01 00:01:00'),
(20, 19, 19, 'MONTHLY', '2025-08-01', '2025-08-31',  4.75,  3.50, '2025-09-01 00:01:00'),
(21, 20, 20, 'MONTHLY', '2025-09-01', '2025-09-30',  3.30,  2.85, '2025-10-01 00:01:00'),
(22, 21, 21, 'MONTHLY', '2025-09-01', '2025-09-30',  5.10,  4.20, '2025-10-01 00:01:00'),
(23, 22, 22, 'MONTHLY', '2025-10-01', '2025-10-31',  2.40,  2.10, '2025-11-01 00:01:00'),
(24, 23, 23, 'MONTHLY', '2025-10-01', '2025-10-31',  1.95,  2.10, '2025-11-01 00:01:00'),
(25, 24, 24, 'MONTHLY', '2025-11-01', '2025-11-30',  3.85,  3.20, '2025-12-01 00:01:00'),
(26, 25, 25, 'MONTHLY', '2025-11-01', '2025-11-30',  2.50,  2.40, '2025-12-01 00:01:00'),
(27, 26, 26, 'MONTHLY', '2025-12-01', '2025-12-31',  4.10,  3.50, '2026-01-01 00:01:00'),
(28, 27, 27, 'MONTHLY', '2025-12-01', '2025-12-31',  3.25,  2.95, '2026-01-01 00:01:00'),
(29, 28, 28, 'MONTHLY', '2026-01-01', '2026-01-31',  2.80,  2.65, '2026-02-01 00:01:00'),
(30, 29, 29, 'MONTHLY', '2026-01-01', '2026-01-31',  3.55,  3.10, '2026-02-01 00:01:00'),
(31, 30, 30, 'MONTHLY', '2026-02-01', '2026-02-28',  1.75,  1.85, '2026-03-01 00:01:00'),
(32, 31, 31, 'MONTHLY', '2026-02-01', '2026-02-28',  2.95,  2.40, '2026-03-01 00:01:00'),
(33, 32, 32, 'MONTHLY', '2026-03-01', '2026-03-31',  3.40,  2.80, '2026-04-01 00:01:00'),
(34, 33, 33, 'MONTHLY', '2026-03-01', '2026-03-31',  4.85,  3.95, '2026-04-01 00:01:00'),
(35, 11, 11, 'DAILY',   '2026-04-15', '2026-04-15',  0.85,  0.62, '2026-04-15 18:00:00'),
(36, 12, 12, 'DAILY',   '2026-04-15', '2026-04-15',  0.45,  0.62, '2026-04-15 18:00:00'),
(37, 13, 13, 'DAILY',   '2026-04-15', '2026-04-15',  1.20,  0.62, '2026-04-15 18:00:00'),
(38, 14, 14, 'DAILY',   '2026-04-15', '2026-04-15',  0.78,  0.62, '2026-04-15 18:00:00'),
(39, 15, 15, 'DAILY',   '2026-04-15', '2026-04-15',  0.30,  0.62, '2026-04-15 18:00:00'),
(40, 16, 16, 'DAILY',   '2026-04-15', '2026-04-15',  1.05,  0.62, '2026-04-15 18:00:00');

-- ─────────────────────────────────────────────────
-- 18. ANALYTICS_DB.RISK_MEASURES — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO risk_measures (measure_id, account_id, measure_type, measure_value, description, calculated_at) VALUES
(11, 11, 'VOLATILITY',     12.50, 'Annualised vol from daily returns',  '2026-04-22 00:00:00'),
(12, 11, 'VAR_95',         -3.40, 'Daily VaR 95%',                       '2026-04-22 00:00:00'),
(13, 12, 'VOLATILITY',      8.75, 'Lower vol — Conservative tilt',       '2026-04-22 00:00:00'),
(14, 12, 'MAX_DRAWDOWN',   -4.20, 'Last 12m max drawdown',               '2026-04-22 00:00:00'),
(15, 13, 'VOLATILITY',     22.30, 'Aggressive UHNI vol',                 '2026-04-22 00:00:00'),
(16, 13, 'TRACKING_ERROR',  3.85, 'Vs Nifty50 benchmark',                '2026-04-22 00:00:00'),
(17, 14, 'VAR_95',         -2.20, 'Daily VaR 95%',                       '2026-04-22 00:00:00'),
(18, 14, 'VOLATILITY',     10.40, 'Balanced HNI',                        '2026-04-22 00:00:00'),
(19, 15, 'MAX_DRAWDOWN',   -2.10, 'Stable — Conservative',               '2026-04-22 00:00:00'),
(20, 16, 'VOLATILITY',     19.80, 'UHNI Aggressive vol',                 '2026-04-22 00:00:00'),
(21, 17, 'VAR_95',         -2.95, 'Daily VaR 95%',                       '2026-04-22 00:00:00'),
(22, 18, 'VOLATILITY',      7.25, 'Conservative low vol',                '2026-04-22 00:00:00'),
(23, 19, 'TRACKING_ERROR',  2.50, 'vs benchmark',                        '2026-04-22 00:00:00'),
(24, 20, 'MAX_DRAWDOWN',   -5.50, 'High-water mark deviation',           '2026-04-22 00:00:00'),
(25, 21, 'VOLATILITY',     24.10, 'Aggressive UHNI',                     '2026-04-22 00:00:00'),
(26, 22, 'VAR_95',         -3.80, 'Balanced HNI',                        '2026-04-22 00:00:00'),
(27, 23, 'VOLATILITY',      9.10, 'Conservative bias',                   '2026-04-22 00:00:00'),
(28, 24, 'TRACKING_ERROR',  4.20, 'Active mgmt deviation',               '2026-04-22 00:00:00'),
(29, 25, 'VOLATILITY',     21.50, 'Aggressive UHNI',                     '2026-04-22 00:00:00'),
(30, 26, 'MAX_DRAWDOWN',   -3.30, '12-month',                            '2026-04-22 00:00:00'),
(31, 27, 'VOLATILITY',     13.40, 'Balanced HNI',                        '2026-04-22 00:00:00'),
(32, 28, 'VAR_95',         -4.10, 'UHNI Trust',                          '2026-04-22 00:00:00'),
(33, 29, 'VOLATILITY',      8.95, 'Conservative Retail',                 '2026-04-22 00:00:00'),
(34, 30, 'TRACKING_ERROR',  3.10, 'vs benchmark',                        '2026-04-22 00:00:00'),
(35, 31, 'VOLATILITY',     23.80, 'UHNI Aggressive',                     '2026-04-22 00:00:00'),
(36, 32, 'MAX_DRAWDOWN',   -3.95, 'Recent drawdown',                     '2026-04-22 00:00:00'),
(37, 33, 'VOLATILITY',     14.20, 'Balanced HNI',                        '2026-04-22 00:00:00'),
(38, 34, 'VAR_95',         -2.50, 'Conservative Retail',                 '2026-04-22 00:00:00'),
(39, 36, 'VOLATILITY',     11.80, 'Balanced HNI',                        '2026-04-22 00:00:00'),
(40, 39, 'VOLATILITY',     20.30, 'UHNI Aggressive',                     '2026-04-22 00:00:00');

-- ─────────────────────────────────────────────────
-- 19. WEALTH.GOALS — 30 new
-- ─────────────────────────────────────────────────
USE wealth;

INSERT INTO goals (goal_id, client_id, goal_type, target_amount, target_date, priority, status) VALUES
(11, 13, 'RETIREMENT',  10000000.00, '2030-04-01', 1, 'IN_PROGRESS'),
(12, 13, 'EDUCATION',   5000000.00,  '2032-06-01', 2, 'IN_PROGRESS'),
(13, 14, 'WEALTH',      2500000.00,  '2030-12-31', 1, 'ACTIVE'),
(14, 15, 'CUSTOM',      30000000.00, '2028-04-01', 1, 'IN_PROGRESS'),
(15, 16, 'EDUCATION',   8000000.00,  '2035-06-01', 1, 'ACTIVE'),
(16, 17, 'RETIREMENT',  6000000.00,  '2035-09-01', 1, 'ACTIVE'),
(17, 18, 'WEALTH',      50000000.00, '2030-01-01', 1, 'IN_PROGRESS'),
(18, 19, 'EDUCATION',   4500000.00,  '2033-06-01', 2, 'ACTIVE'),
(19, 20, 'CUSTOM',      1000000.00,  '2028-12-31', 3, 'ACTIVE'),
(20, 21, 'WEALTH',      75000000.00, '2032-12-31', 1, 'IN_PROGRESS'),
(21, 22, 'RETIREMENT',  9000000.00,  '2036-04-01', 1, 'IN_PROGRESS'),
(22, 23, 'CUSTOM',      2000000.00,  '2029-06-01', 2, 'ACTIVE'),
(23, 24, 'EDUCATION',   6500000.00,  '2034-06-01', 1, 'IN_PROGRESS'),
(24, 25, 'WEALTH',      40000000.00, '2031-12-31', 1, 'IN_PROGRESS'),
(25, 26, 'RETIREMENT',  5500000.00,  '2037-04-01', 2, 'ACTIVE'),
(26, 27, 'EDUCATION',   7000000.00,  '2034-06-01', 1, 'IN_PROGRESS'),
(27, 28, 'WEALTH',      60000000.00, '2030-06-01', 1, 'IN_PROGRESS'),
(28, 29, 'CUSTOM',      800000.00,   '2028-03-01', 3, 'ACTIVE'),
(29, 30, 'RETIREMENT',  8500000.00,  '2034-04-01', 1, 'IN_PROGRESS'),
(30, 31, 'WEALTH',      80000000.00, '2031-12-31', 1, 'IN_PROGRESS'),
(31, 32, 'EDUCATION',   3500000.00,  '2032-06-01', 2, 'ACTIVE'),
(32, 33, 'RETIREMENT',  7000000.00,  '2035-04-01', 1, 'IN_PROGRESS'),
(33, 34, 'CUSTOM',      1500000.00,  '2029-12-31', 3, 'ACTIVE'),
(34, 35, 'WEALTH',      55000000.00, '2030-12-31', 1, 'IN_PROGRESS'),
(35, 36, 'EDUCATION',   5500000.00,  '2033-06-01', 2, 'ACTIVE'),
(36, 1,  'CUSTOM',      500000.00,   '2027-06-01', 3, 'ACHIEVED'),
(37, 2,  'WEALTH',      3500000.00,  '2030-12-31', 1, 'IN_PROGRESS'),
(38, 4,  'WEALTH',      45000000.00, '2030-12-31', 1, 'IN_PROGRESS'),
(39, 38, 'RETIREMENT',  8000000.00,  '2036-04-01', 1, 'ACTIVE'),
(40, 41, 'EDUCATION',   4000000.00,  '2032-06-01', 2, 'ACTIVE');

-- ─────────────────────────────────────────────────
-- 20. WEALTH.MODEL_PORTFOLIOS — 25 new (UNIQUE name)
-- ─────────────────────────────────────────────────
INSERT INTO model_portfolios (model_id, name, risk_class, status, weights_json) VALUES
(11, 'Balanced 60/40',          'BALANCED',     'ACTIVE',   '{"equity":0.60,"bond":0.30,"cash":0.10}'),
(12, 'Conservative Income',     'CONSERVATIVE', 'ACTIVE',   '{"equity":0.20,"bond":0.65,"cash":0.15}'),
(13, 'Aggressive Growth',       'AGGRESSIVE',   'ACTIVE',   '{"equity":0.85,"bond":0.10,"cash":0.05}'),
(14, 'Dividend Yield',          'BALANCED',     'ACTIVE',   '{"equity":0.55,"bond":0.40,"cash":0.05}'),
(15, 'Tax Saver ELSS',          'AGGRESSIVE',   'ACTIVE',   '{"equity":0.95,"bond":0.00,"cash":0.05}'),
(16, 'Senior Citizen Plan',     'CONSERVATIVE', 'ACTIVE',   '{"equity":0.10,"bond":0.75,"cash":0.15}'),
(17, 'Multi Asset Plus',        'BALANCED',     'ACTIVE',   '{"equity":0.50,"bond":0.30,"gold":0.10,"cash":0.10}'),
(18, 'Global Tech Tilt',        'AGGRESSIVE',   'ACTIVE',   '{"equity":0.90,"international":0.30,"cash":0.10}'),
(19, 'Indian Banks Focus',      'AGGRESSIVE',   'ACTIVE',   '{"banking_equity":0.70,"bond":0.20,"cash":0.10}'),
(20, 'ELSS Conservative',       'CONSERVATIVE', 'ACTIVE',   '{"equity":0.30,"bond":0.55,"cash":0.15}'),
(21, 'PSU Dividend',            'BALANCED',     'ACTIVE',   '{"psu_equity":0.45,"bond":0.45,"cash":0.10}'),
(22, 'Smallcap Aggressive',     'AGGRESSIVE',   'ACTIVE',   '{"smallcap_equity":0.80,"cash":0.20}'),
(23, 'Retirement Income',       'CONSERVATIVE', 'ACTIVE',   '{"equity":0.15,"bond":0.70,"cash":0.15}'),
(24, 'Balanced Hybrid',         'BALANCED',     'ACTIVE',   '{"equity":0.55,"bond":0.35,"cash":0.10}'),
(25, 'Mid+SmallCap Growth',     'AGGRESSIVE',   'ACTIVE',   '{"midcap_equity":0.50,"smallcap_equity":0.40,"cash":0.10}'),
(26, 'Gold Hedge',              'BALANCED',     'ACTIVE',   '{"equity":0.40,"bond":0.30,"gold":0.20,"cash":0.10}'),
(27, 'NRI Friendly',            'BALANCED',     'ACTIVE',   '{"equity":0.45,"bond":0.40,"cash":0.15}'),
(28, 'Inflation Beat',          'AGGRESSIVE',   'ACTIVE',   '{"equity":0.75,"reit":0.10,"gold":0.10,"cash":0.05}'),
(29, 'Stable Growth',           'BALANCED',     'ACTIVE',   '{"equity":0.50,"bond":0.40,"cash":0.10}'),
(30, 'Retired Pensioner',       'CONSERVATIVE', 'ACTIVE',   '{"equity":0.10,"bond":0.80,"cash":0.10}'),
(31, 'Sector Rotator',          'AGGRESSIVE',   'ACTIVE',   '{"equity":0.85,"cash":0.15}'),
(32, 'Global Diversified',      'BALANCED',     'ACTIVE',   '{"equity":0.55,"international":0.20,"bond":0.20,"cash":0.05}'),
(33, 'Wealth Preserver',        'CONSERVATIVE', 'ACTIVE',   '{"equity":0.20,"bond":0.70,"gold":0.05,"cash":0.05}'),
(34, 'Legacy 2018',             'BALANCED',     'INACTIVE', '{"equity":0.50,"bond":0.45,"cash":0.05}'),
(35, 'Aggressive Tech 2025',    'AGGRESSIVE',   'ACTIVE',   '{"equity":0.92,"cash":0.08}');

-- ─────────────────────────────────────────────────
-- 21. WEALTH.RECOMMENDATIONS — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO recommendations (reco_id, client_id, model_id, goal_id, proposal_json, proposed_date, status) VALUES
(11, 13, 11, 11, '{"summary":"Switch to Balanced 60/40 for retirement"}',     '2025-04-15', 'APPROVED'),
(12, 14, 12, 13, '{"summary":"Conservative tilt for wealth growth"}',         '2025-05-08', 'APPROVED'),
(13, 15, 13, 14, '{"summary":"Aggressive growth for trust corpus"}',          '2025-04-28', 'APPROVED'),
(14, 16, 11, 15, '{"summary":"Education savings plan"}',                      '2025-05-21', 'SUBMITTED'),
(15, 17, 12, 16, '{"summary":"Conservative retirement glide path"}',          '2025-06-06', 'APPROVED'),
(16, 18, 13, 17, '{"summary":"UHNI wealth accumulation"}',                    '2025-06-13', 'APPROVED'),
(17, 19, 14, 18, '{"summary":"Dividend yield income for education"}',         '2025-06-25', 'DRAFT'),
(18, 20, 12, 19, '{"summary":"Conservative play for short-term goal"}',       '2025-07-08', 'SUBMITTED'),
(19, 21, 13, 20, '{"summary":"Aggressive UHNI growth"}',                      '2025-07-20', 'APPROVED'),
(20, 22, 11, 21, '{"summary":"Retirement portfolio"}',                        '2025-08-03', 'APPROVED'),
(21, 23, 12, 22, '{"summary":"Conservative custom goal"}',                    '2025-08-17', 'REJECTED'),
(22, 24, 11, 23, '{"summary":"Balanced education plan"}',                     '2025-08-30', 'APPROVED'),
(23, 25, 13, 24, '{"summary":"Aggressive UHNI"}',                             '2025-09-10', 'APPROVED'),
(24, 26, 12, 25, '{"summary":"Conservative retirement"}',                     '2025-09-23', 'SUBMITTED'),
(25, 27, 11, 26, '{"summary":"Education balanced"}',                          '2025-10-07', 'APPROVED'),
(26, 28, 13, 27, '{"summary":"Aggressive trust corpus"}',                     '2025-10-20', 'APPROVED'),
(27, 29, 12, 28, '{"summary":"Conservative custom"}',                         '2025-11-06', 'DRAFT'),
(28, 30, 11, 29, '{"summary":"Balanced retirement"}',                         '2025-11-17', 'APPROVED'),
(29, 31, 13, 30, '{"summary":"Aggressive UHNI"}',                             '2025-12-08', 'APPROVED'),
(30, 32, 12, 31, '{"summary":"Conservative education"}',                      '2025-12-19', 'APPROVED'),
(31, 33, 11, 32, '{"summary":"Balanced retirement"}',                         '2026-01-12', 'SUBMITTED'),
(32, 34, 12, 33, '{"summary":"Conservative custom"}',                         '2026-01-26', 'APPROVED'),
(33, 35, 13, 34, '{"summary":"Aggressive UHNI"}',                             '2026-02-09', 'APPROVED'),
(34, 36, 11, 35, '{"summary":"Balanced education"}',                          '2026-02-23', 'DRAFT'),
(35, 1,  14, NULL,'{"summary":"Dividend yield review"}',                      '2026-03-05', 'APPROVED'),
(36, 2,  11, NULL,'{"summary":"Balanced rebalance"}',                         '2026-03-12', 'SUBMITTED'),
(37, 4,  13, NULL,'{"summary":"UHNI aggressive review"}',                     '2026-03-20', 'APPROVED'),
(38, 5,  17, NULL,'{"summary":"Multi-asset diversification"}',                '2026-03-27', 'APPROVED'),
(39, 38, 11, 39, '{"summary":"HNI retirement plan"}',                         '2026-04-04', 'SUBMITTED'),
(40, 41, 12, 40, '{"summary":"Conservative education plan"}',                 '2026-04-18', 'DRAFT');

-- ─────────────────────────────────────────────────
-- 22. NOTIFICATION.NOTIFICATION — 30 new
-- ─────────────────────────────────────────────────
USE notification;

INSERT INTO notification (notificationid, userid, message, category, status, created_date) VALUES
(11, 13, 'Order for TCS (100 shares) filled at Rs.3450.50.',                                         'Order',           'Read',      '2025-04-15 10:30:00'),
(12, 14, 'KYC verification successful — account fully active.',                                     'Compliance',      'Read',      '2025-05-04 12:00:00'),
(13, 15, 'Bajaj Finance bonus shares (1:1) credited to your account.',                              'CorporateAction', 'Unread',    '2025-09-25 09:00:00'),
(14, 16, 'Single-asset concentration alert: KOTAKBANK > 25% — review recommended.',                 'Compliance',      'Unread',    '2025-11-05 13:30:00'),
(15, 17, 'Maruti Suzuki dividend Rs.18 per share credited.',                                        'CorporateAction', 'Read',      '2025-11-18 10:00:00'),
(16, 18, 'Quarterly portfolio review scheduled for July 2025.',                                     'Review',          'Read',      '2025-06-25 09:00:00'),
(17, 19, 'Banking sector concentration approaching 30% limit.',                                     'Compliance',      'Unread',    '2025-12-10 14:00:00'),
(18, 20, 'SIP order pending settlement — funds expected within T+1.',                               'Order',           'Read',      '2025-07-08 10:00:00'),
(19, 21, 'Trust account corporate action: Bharti Airtel 5G dividend Rs.8 per share.',               'CorporateAction', 'Read',      '2025-11-05 11:00:00'),
(20, 22, 'Annual portfolio review — please book a slot.',                                           'Review',          'Unread',    '2026-03-25 10:00:00'),
(21, 23, 'Order cancelled: ASIANPAINT (30 shares) — limit not met.',                                'Order',           'Dismissed', '2025-08-17 14:30:00'),
(22, 24, 'Education goal on track — 18% of target reached.',                                        'Review',          'Read',      '2025-09-30 16:00:00'),
(23, 25, 'Cash balance below 5% buffer — top-up suggested.',                                        'Compliance',      'Unread',    '2026-02-05 11:30:00'),
(24, 26, 'Order rejected: trade size exceeds Retail 5L cap.',                                       'Order',           'Read',      '2025-09-23 13:45:00'),
(25, 27, 'Half-yearly review meeting confirmed for 2026-04-15.',                                    'Review',          'Read',      '2026-03-30 09:00:00'),
(26, 28, 'L&T order filled: 200 shares at Rs.1130.25.',                                             'Order',           'Read',      '2025-10-20 14:30:00'),
(27, 29, 'KYC alert: order placed before verification — under review.',                             'Compliance',      'Read',      '2025-11-08 14:30:00'),
(28, 30, 'Bank ETF (BANKBEES) subscription completed — 300 units.',                                 'Order',           'Read',      '2025-12-19 14:15:00'),
(29, 31, 'High-volatility alert: portfolio vol 23.8% — review risk profile.',                       'Compliance',      'Unread',    '2026-04-22 09:00:00'),
(30, 32, 'PFC bond coupon Rs.7.20% credited.',                                                      'CorporateAction', 'Read',      '2026-04-15 10:00:00'),
(31, 33, 'Dividend received: HDFC Bank Rs.15 per share.',                                           'CorporateAction', 'Read',      '2025-07-15 09:00:00'),
(32, 34, 'M&M order filled: 40 shares at Rs.2270.',                                                 'Order',           'Read',      '2026-01-26 11:30:00'),
(33, 35, 'UHNI Trust Aggressive recommendation approved.',                                          'Review',          'Unread',    '2026-02-09 13:30:00'),
(34, 36, 'Education plan review next month.',                                                       'Review',          'Read',      '2026-03-15 10:00:00'),
(35, 1,  'Order for HDFCBANK (50 shares) filled at Rs.1645.0.',                                     'Order',           'Read',      '2026-04-12 11:00:00'),
(36, 4,  'Trust account: STRUCTURED purchase under compliance review.',                             'Compliance',      'Unread',    '2026-04-18 14:30:00'),
(37, 5,  'Dividend distribution: Nifty BeES — Rs.15 per unit.',                                     'CorporateAction', 'Read',      '2026-04-05 09:00:00'),
(38, 7,  'Joint account: dual signature pending for new investment.',                               'Compliance',      'Unread',    '2026-04-18 14:30:00'),
(39, 8,  'Bond concentration above target — rebalancing in progress.',                              'Compliance',      'Read',      '2026-04-10 13:00:00'),
(40, 10, 'Quarterly statement available for download.',                                             'Review',          'Read',      '2026-04-01 09:00:00');

-- ─────────────────────────────────────────────────
-- 23. WEALTHPRO_REVIEW.REVIEWS — 30 new
-- ─────────────────────────────────────────────────
USE wealthpro_review;

INSERT INTO reviews (review_id, account_id, period_type, period_start, period_end, review_date, reviewed_by, status, highlights_json) VALUES
(11, 11, 'QUARTERLY',   '2025-04-01', '2025-06-30', '2025-07-15', 'rm1',     'COMPLETED',  '{"return":3.45,"benchmark":2.80,"breach_count":0}'),
(12, 12, 'QUARTERLY',   '2025-04-01', '2025-06-30', '2025-07-18', 'rm1',     'COMPLETED',  '{"return":1.85,"benchmark":1.95,"action":"None"}'),
(13, 13, 'HALF_YEARLY', '2025-04-01', '2025-09-30', '2025-10-15', 'rm1',     'COMPLETED',  '{"return":7.20,"breach_count":1,"action":"Reviewed"}'),
(14, 14, 'QUARTERLY',   '2025-07-01', '2025-09-30', '2025-10-20', 'rm1',     'COMPLETED',  '{"return":2.75}'),
(15, 15, 'QUARTERLY',   '2025-07-01', '2025-09-30', '2025-10-22', 'rm1',     'COMPLETED',  '{"return":1.20,"action":"Rebalance recommended"}'),
(16, 16, 'HALF_YEARLY', '2025-04-01', '2025-09-30', '2025-10-25', 'rm1',     'COMPLETED',  '{"return":5.85,"breach_count":2}'),
(17, 17, 'QUARTERLY',   '2025-07-01', '2025-09-30', '2025-10-28', 'rm1',     'COMPLETED',  '{"return":2.90}'),
(18, 18, 'QUARTERLY',   '2025-10-01', '2025-12-31', '2026-01-15', 'rm1',     'COMPLETED',  '{"return":1.45,"highlight":"UHNI Trust"}'),
(19, 19, 'QUARTERLY',   '2025-10-01', '2025-12-31', '2026-01-17', 'rm1',     'COMPLETED',  '{"return":4.75}'),
(20, 20, 'HALF_YEARLY', '2025-07-01', '2025-12-31', '2026-01-20', 'rm1',     'COMPLETED',  '{"return":3.30,"action":"Top-up"}'),
(21, 21, 'QUARTERLY',   '2025-10-01', '2025-12-31', '2026-01-22', 'rm1',     'COMPLETED',  '{"return":5.10}'),
(22, 22, 'QUARTERLY',   '2025-10-01', '2025-12-31', '2026-01-25', 'rm1',     'COMPLETED',  '{"return":2.40}'),
(23, 23, 'QUARTERLY',   '2025-10-01', '2025-12-31', '2026-01-28', 'rm1',     'COMPLETED',  '{"return":1.95}'),
(24, 24, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-04-15', 'rm1',     'COMPLETED',  '{"return":3.85}'),
(25, 25, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-04-17', 'rm1',     'IN_PROGRESS','{}'),
(26, 26, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-04-18', 'rm1',     'SCHEDULED',  '{}'),
(27, 27, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-04-20', 'rm1',     'SCHEDULED',  '{}'),
(28, 28, 'HALF_YEARLY', '2025-10-01', '2026-03-31', '2026-04-22', 'rm1',     'SCHEDULED',  '{}'),
(29, 29, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-04-25', 'rm1',     'SCHEDULED',  '{}'),
(30, 30, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-04-27', 'rm1',     'SCHEDULED',  '{}'),
(31, 31, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-04-29', 'rm1',     'SCHEDULED',  '{}'),
(32, 32, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-05-01', 'rm1',     'SCHEDULED',  '{}'),
(33, 33, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-05-03', 'rm1',     'SCHEDULED',  '{}'),
(34, 1,  'ANNUAL',      '2025-04-01', '2026-03-31', '2026-04-15', 'rm1',     'COMPLETED',  '{"return":12.50,"action":"On track"}'),
(35, 2,  'ANNUAL',      '2025-04-01', '2026-03-31', '2026-04-16', 'rm1',     'COMPLETED',  '{"return":10.20}'),
(36, 4,  'ANNUAL',      '2025-04-01', '2026-03-31', '2026-04-17', 'rm1',     'COMPLETED',  '{"return":15.80,"highlight":"UHNI Trust"}'),
(37, 5,  'ANNUAL',      '2025-04-01', '2026-03-31', '2026-04-18', 'rm1',     'COMPLETED',  '{"return":11.40}'),
(38, 7,  'ANNUAL',      '2025-04-01', '2026-03-31', '2026-04-19', 'rm1',     'IN_PROGRESS','{}'),
(39, 36, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-04-20', 'rm1',     'SCHEDULED',  '{}'),
(40, 39, 'QUARTERLY',   '2026-01-01', '2026-03-31', '2026-04-22', 'rm1',     'SCHEDULED',  '{}');

-- ─────────────────────────────────────────────────
-- 24. WEALTHPRO_REVIEW.STATEMENTS — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO statements (statement_id, account_id, period_type, period_start, period_end, generated_date, status, summary_json) VALUES
(11, 11, 'QUARTERLY', '2025-04-01', '2025-06-30', '2025-07-05', 'DELIVERED', '{"opening":500000,"closing":517250,"return":3.45}'),
(12, 11, 'QUARTERLY', '2025-07-01', '2025-09-30', '2025-10-05', 'DELIVERED', '{"opening":517250,"closing":532600,"return":2.97}'),
(13, 12, 'QUARTERLY', '2025-04-01', '2025-06-30', '2025-07-06', 'DELIVERED', '{"opening":200000,"closing":203700}'),
(14, 13, 'QUARTERLY', '2025-04-01', '2025-06-30', '2025-07-07', 'DELIVERED', '{"opening":5000000,"closing":5210000}'),
(15, 14, 'QUARTERLY', '2025-04-01', '2025-06-30', '2025-07-08', 'DELIVERED', '{"opening":350000,"closing":359625}'),
(16, 15, 'QUARTERLY', '2025-04-01', '2025-06-30', '2025-07-09', 'DELIVERED', '{"opening":150000,"closing":151800}'),
(17, 16, 'QUARTERLY', '2025-04-01', '2025-06-30', '2025-07-10', 'DELIVERED', '{"opening":8000000,"closing":8288000}'),
(18, 17, 'QUARTERLY', '2025-07-01', '2025-09-30', '2025-10-07', 'DELIVERED', '{"opening":600000,"closing":617400}'),
(19, 18, 'QUARTERLY', '2025-07-01', '2025-09-30', '2025-10-08', 'DELIVERED', '{"opening":100000,"closing":101450}'),
(20, 19, 'QUARTERLY', '2025-07-01', '2025-09-30', '2025-10-09', 'DELIVERED', '{"opening":600000,"closing":628500}'),
(21, 20, 'QUARTERLY', '2025-07-01', '2025-09-30', '2025-10-10', 'DELIVERED', '{"opening":225000,"closing":232425}'),
(22, 21, 'QUARTERLY', '2025-10-01', '2025-12-31', '2026-01-05', 'DELIVERED', '{"opening":750000,"closing":788250}'),
(23, 22, 'QUARTERLY', '2025-10-01', '2025-12-31', '2026-01-07', 'DELIVERED', '{"opening":250000,"closing":256000}'),
(24, 23, 'QUARTERLY', '2025-10-01', '2025-12-31', '2026-01-09', 'DELIVERED', '{"opening":350000,"closing":356825}'),
(25, 24, 'QUARTERLY', '2026-01-01', '2026-03-31', '2026-04-05', 'GENERATED', '{"opening":500000,"closing":519250}'),
(26, 25, 'QUARTERLY', '2026-01-01', '2026-03-31', '2026-04-06', 'GENERATED', '{"opening":120000,"closing":123000}'),
(27, 26, 'QUARTERLY', '2026-01-01', '2026-03-31', '2026-04-07', 'PENDING',   '{}'),
(28, 27, 'QUARTERLY', '2026-01-01', '2026-03-31', '2026-04-08', 'PENDING',   '{}'),
(29, 28, 'QUARTERLY', '2026-01-01', '2026-03-31', '2026-04-09', 'PENDING',   '{}'),
(30, 29, 'QUARTERLY', '2026-01-01', '2026-03-31', '2026-04-10', 'PENDING',   '{}'),
(31, 30, 'QUARTERLY', '2026-01-01', '2026-03-31', '2026-04-11', 'PENDING',   '{}'),
(32, 31, 'QUARTERLY', '2026-01-01', '2026-03-31', '2026-04-12', 'PENDING',   '{}'),
(33, 32, 'QUARTERLY', '2026-01-01', '2026-03-31', '2026-04-13', 'PENDING',   '{}'),
(34, 1,  'ANNUAL',    '2025-04-01', '2026-03-31', '2026-04-15', 'DELIVERED', '{"opening":1000000,"closing":1125000}'),
(35, 2,  'ANNUAL',    '2025-04-01', '2026-03-31', '2026-04-15', 'DELIVERED', '{"opening":2000000,"closing":2204000}'),
(36, 4,  'ANNUAL',    '2025-04-01', '2026-03-31', '2026-04-15', 'DELIVERED', '{"opening":10000000,"closing":11580000}'),
(37, 5,  'ANNUAL',    '2025-04-01', '2026-03-31', '2026-04-15', 'DELIVERED', '{"opening":1500000,"closing":1671000}'),
(38, 7,  'ANNUAL',    '2025-04-01', '2026-03-31', '2026-04-15', 'GENERATED', '{}'),
(39, 8,  'ANNUAL',    '2025-04-01', '2026-03-31', '2026-04-15', 'GENERATED', '{}'),
(40, 10, 'MONTHLY',   '2026-03-01', '2026-03-31', '2026-04-01', 'DELIVERED', '{"return":1.85}');

-- ─────────────────────────────────────────────────
-- 25. GATEWAY_DB.USERS — 30 new (with shared bcrypt hash)
-- ─────────────────────────────────────────────────
USE gateway_db;

INSERT IGNORE INTO users (username, password, name, email, phone, roles) VALUES
('client13', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Suresh Iyer',       'suresh.iyer@gmail.com',     '9123450013', 'CLIENT'),
('client14', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Pooja Sharma',      'pooja.sharma@gmail.com',    '9123450014', 'CLIENT'),
('client15', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Karan Mehta',       'karan.mehta@gmail.com',     '9123450015', 'CLIENT'),
('client16', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Nisha Agarwal',     'nisha.agarwal@gmail.com',   '9123450016', 'CLIENT'),
('client17', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Manoj Saxena',      'manoj.saxena@gmail.com',    '9123450017', 'CLIENT'),
('client18', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Lakshmi Pillai',    'lakshmi.pillai@gmail.com',  '9123450018', 'CLIENT'),
('client19', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Rajesh Kumar',      'rajesh.kumar@gmail.com',    '9123450019', 'CLIENT'),
('client20', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Aarti Khanna',      'aarti.khanna@gmail.com',    '9123450020', 'CLIENT'),
('client21', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Vivek Bansal',      'vivek.bansal@gmail.com',    '9123450021', 'CLIENT'),
('client22', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Shilpa Menon',      'shilpa.menon@gmail.com',    '9123450022', 'CLIENT'),
('client23', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Ashok Rao',         'ashok.rao@gmail.com',       '9123450023', 'CLIENT'),
('client24', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Divya Choudhary',   'divya.choudhary@gmail.com', '9123450024', 'CLIENT'),
('client25', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Sunil Joshi',       'sunil.joshi@gmail.com',     '9123450025', 'CLIENT'),
('rm2',      '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Anil Bedi',         'anil.bedi@wealthpro.com',   '9000000002', 'RM'),
('rm3',      '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Sneha Roy',         'sneha.roy@wealthpro.com',   '9000000003', 'RM'),
('rm4',      '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Mohit Khan',        'mohit.khan@wealthpro.com',  '9000000004', 'RM'),
('dealer1',  '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Rahul Vyas',        'rahul.vyas@wealthpro.com',  '9000000007', 'DEALER'),
('dealer2',  '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Tina Mathur',       'tina.mathur@wealthpro.com', '9000000008', 'DEALER'),
('compliance1','$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a','Ravi Subramanian', 'ravi.s@wealthpro.com',      '9000000009', 'COMPLIANCE'),
('compliance2','$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a','Madhuri Pawar',    'madhuri.p@wealthpro.com',   '9000000010', 'COMPLIANCE'),
('client26', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Ritu Malhotra',     'ritu.malhotra@gmail.com',   '9123450026', 'CLIENT'),
('client27', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Amit Trivedi',      'amit.trivedi@gmail.com',    '9123450027', 'CLIENT'),
('client28', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Geeta Rangan',      'geeta.rangan@gmail.com',    '9123450028', 'CLIENT'),
('client29', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Prakash Naidu',     'prakash.naidu@gmail.com',   '9123450029', 'CLIENT'),
('client30', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Kavita Sinha',      'kavita.sinha@gmail.com',    '9123450030', 'CLIENT'),
('client31', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Harshad Desai',     'harshad.desai@gmail.com',   '9123450031', 'CLIENT'),
('client32', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Neha Bhattacharya', 'neha.bhatta@gmail.com',     '9123450032', 'CLIENT'),
('client33', '$2b$12$Vja37SAkt2BDiLNkiqBZxOA5myzimDzYXSCYIgQWybbPCR4XScO8a', 'Sandeep Goyal',     'sandeep.goyal@gmail.com',   '9123450033', 'CLIENT');

-- ─────────────────────────────────────────────────
-- 26. GATEWAY_DB.AUDITLOG — 30 new
-- ─────────────────────────────────────────────────
INSERT INTO auditlog (id, username, roles, remoteAddress, method, pathinfo, action, resource, timestamp, metadata) VALUES
(27,'rm1',         '[ROLE_RM]',         '127.0.0.1:53001','POST','/api/clients',         'POST','/api/clients',         '2025-04-12 09:00:00','{"clientCreated":13}'),
(28,'rm1',         '[ROLE_RM]',         '127.0.0.1:53002','POST','/api/clients/13/kyc',  'POST','/api/clients/13/kyc',  '2025-04-12 09:30:00','{"kycId":11,"docType":"AADHAAR"}'),
(29,'rm1',         '[ROLE_RM]',         '127.0.0.1:53003','POST','/api/clients/13/risk-profile','POST','/api/clients/13/risk-profile','2025-04-13 10:00:00','{"riskClass":"Balanced","score":58.5}'),
(30,'rm1',         '[ROLE_RM]',    '127.0.0.1:53004','POST','/api/orders',          'POST','/api/orders',          '2025-04-15 10:00:00','{"orderId":11,"security":"TCS"}'),
(31,'dealer1',     '[ROLE_DEALER]',     '127.0.0.1:53005','POST','/api/orders/11/route', 'POST','/api/orders/11/route', '2025-04-15 10:14:00','{"venue":"SIMULATED_NSE"}'),
(32,'dealer1',     '[ROLE_DEALER]',     '127.0.0.1:53006','POST','/api/orders/11/fills', 'POST','/api/orders/11/fills', '2025-04-15 10:18:00','{"fillId":8,"qty":100}'),
(33,'compliance1', '[ROLE_COMPLIANCE]', '127.0.0.1:53007','GET', '/api/compliance-breaches','GET', '/api/compliance-breaches','2025-08-15 10:30:00','{}'),
(34,'rm1',         '[ROLE_RM]',         '127.0.0.1:53008','POST','/api/clients',         'POST','/api/clients',         '2025-05-04 11:00:00','{"clientCreated":14}'),
(35,'rm1',         '[ROLE_RM]',         '127.0.0.1:53009','POST','/api/clients',         'POST','/api/clients',         '2025-04-25 14:00:00','{"clientCreated":15}'),
(36,'rm1',         '[ROLE_RM]',    '127.0.0.1:53010','POST','/api/orders',          'POST','/api/orders',          '2025-04-28 14:30:00','{"orderId":15}'),
(37,'client13',    '[ROLE_CLIENT]',     '127.0.0.1:53011','GET', '/api/analytics/13/dashboard','GET','/api/analytics/13/dashboard','2025-05-01 09:00:00','{}'),
(38,'client14',    '[ROLE_CLIENT]',     '127.0.0.1:53012','GET', '/api/analytics/14/dashboard','GET','/api/analytics/14/dashboard','2025-05-10 10:00:00','{}'),
(39,'rm2',         '[ROLE_RM]',         '127.0.0.1:53013','POST','/api/clients',         'POST','/api/clients',         '2025-06-13 09:00:00','{"clientCreated":18}'),
(40,'compliance1', '[ROLE_COMPLIANCE]', '127.0.0.1:53014','PUT', '/api/compliance-breaches/12/acknowledge','PUT','/api/compliance-breaches/12/acknowledge','2025-09-02 11:00:00','{"breachId":12}'),
(41,'admin',       '[ROLE_ADMIN]',      '127.0.0.1:53015','POST','/auth/users/register', 'POST','/auth/users/register', '2025-07-01 09:00:00','{"createdUser":"rm2"}'),
(42,'admin',       '[ROLE_ADMIN]',      '127.0.0.1:53016','POST','/auth/users/register', 'POST','/auth/users/register', '2025-07-01 09:05:00','{"createdUser":"rm3"}'),
(43,'admin',       '[ROLE_ADMIN]',      '127.0.0.1:53017','POST','/auth/users/register', 'POST','/auth/users/register', '2025-07-01 09:10:00','{"createdUser":"rm4"}'),
(44,'admin',       '[ROLE_ADMIN]',      '127.0.0.1:53018','POST','/auth/users/register', 'POST','/auth/users/register', '2025-07-01 09:15:00','{"createdUser":"dealer2"}'),
(45,'admin',       '[ROLE_ADMIN]',      '127.0.0.1:53019','POST','/auth/users/register', 'POST','/auth/users/register', '2025-07-01 09:20:00','{"createdUser":"compliance2"}'),
(46,'rm2',         '[ROLE_RM]',         '127.0.0.1:53020','POST','/api/clients',         'POST','/api/clients',         '2025-08-03 10:00:00','{"clientCreated":22}'),
(47,'rm2',         '[ROLE_RM]',    '127.0.0.1:53021','POST','/api/orders',          'POST','/api/orders',          '2025-08-30 10:30:00','{"orderId":26}'),
(48,'dealer2',     '[ROLE_DEALER]',     '127.0.0.1:53022','POST','/api/orders/26/fills', 'POST','/api/orders/26/fills', '2025-08-30 10:33:00','{"fillId":21}'),
(49,'rm3',         '[ROLE_RM]',         '127.0.0.1:53023','POST','/api/clients/30/kyc',  'POST','/api/clients/30/kyc',  '2025-11-12 10:00:00','{"kycId":20}'),
(50,'compliance2', '[ROLE_COMPLIANCE]', '127.0.0.1:53024','PUT', '/api/compliance-breaches/22/acknowledge','PUT','/api/compliance-breaches/22/acknowledge','2026-02-05 12:00:00','{"breachId":22}'),
(51,'client18',    '[ROLE_CLIENT]',     '127.0.0.1:53025','GET', '/api/notifications/user/18','GET','/api/notifications/user/18','2026-02-15 09:00:00','{}'),
(52,'rm4',         '[ROLE_RM]',         '127.0.0.1:53026','POST','/api/clients',         'POST','/api/clients',         '2026-03-09 10:00:00','{"clientCreated":38}'),
(53,'admin',       '[ROLE_ADMIN]',      '127.0.0.1:53027','POST','/auth/users/register', 'POST','/auth/users/register', '2026-03-01 09:00:00','{"createdUser":"rm4"}'),
(54,'compliance1', '[ROLE_COMPLIANCE]', '127.0.0.1:53028','PUT', '/api/compliance-breaches/35/close','PUT','/api/compliance-breaches/35/close','2026-04-05 11:30:00','{"breachId":35}'),
(55,'rm1',         '[ROLE_RM]',    '127.0.0.1:53029','POST','/api/orders',          'POST','/api/orders',          '2026-04-05 09:30:00','{"orderId":40}'),
(56,'client27',    '[ROLE_CLIENT]',     '127.0.0.1:53030','GET', '/api/analytics/27/dashboard','GET','/api/analytics/27/dashboard','2026-04-22 09:00:00','{}');

-- ─────────────────────────────────────────────────
-- VERIFICATION
-- ─────────────────────────────────────────────────
SELECT '──── ROW COUNTS AFTER LOAD ────' AS verify;
SELECT 'gateway_db.users'                  AS tbl, COUNT(*) AS rows_count FROM gateway_db.users        UNION ALL
SELECT 'gateway_db.auditlog',                       COUNT(*)              FROM gateway_db.auditlog     UNION ALL
SELECT 'kyc.client',                                COUNT(*)              FROM kyc.client              UNION ALL
SELECT 'kyc.kycdocument',                           COUNT(*)              FROM kyc.kycdocument         UNION ALL
SELECT 'kyc.risk_profile',                          COUNT(*)              FROM kyc.risk_profile        UNION ALL
SELECT 'kyc.suitability_rule',                      COUNT(*)              FROM kyc.suitability_rule    UNION ALL
SELECT 'productcatalog.securities',                 COUNT(*)              FROM productcatalog.securities UNION ALL
SELECT 'productcatalog.product_terms',              COUNT(*)              FROM productcatalog.product_terms UNION ALL
SELECT 'productcatalog.research_notes',             COUNT(*)              FROM productcatalog.research_notes UNION ALL
SELECT 'order_execution_db.orders',                 COUNT(*)              FROM order_execution_db.orders UNION ALL
SELECT 'order_execution_db.execution_fills',        COUNT(*)              FROM order_execution_db.execution_fills UNION ALL
SELECT 'order_execution_db.allocations',            COUNT(*)              FROM order_execution_db.allocations UNION ALL
SELECT 'order_execution_db.pre_trade_checks',       COUNT(*)              FROM order_execution_db.pre_trade_checks UNION ALL
SELECT 'pbor.account',                              COUNT(*)              FROM pbor.account            UNION ALL
SELECT 'pbor.holding',                              COUNT(*)              FROM pbor.holding            UNION ALL
SELECT 'pbor.cash_ledger',                          COUNT(*)              FROM pbor.cash_ledger        UNION ALL
SELECT 'pbor.corporate_action',                     COUNT(*)              FROM pbor.corporate_action   UNION ALL
SELECT 'analytics_db.compliance_breaches',          COUNT(*)              FROM analytics_db.compliance_breaches UNION ALL
SELECT 'analytics_db.performance_records',          COUNT(*)              FROM analytics_db.performance_records UNION ALL
SELECT 'analytics_db.risk_measures',                COUNT(*)              FROM analytics_db.risk_measures UNION ALL
SELECT 'wealth.goals',                              COUNT(*)              FROM wealth.goals            UNION ALL
SELECT 'wealth.model_portfolios',                   COUNT(*)              FROM wealth.model_portfolios UNION ALL
SELECT 'wealth.recommendations',                    COUNT(*)              FROM wealth.recommendations  UNION ALL
SELECT 'notification.notification',                 COUNT(*)              FROM notification.notification UNION ALL
SELECT 'wealthpro_review.reviews',                  COUNT(*)              FROM wealthpro_review.reviews UNION ALL
SELECT 'wealthpro_review.statements',               COUNT(*)              FROM wealthpro_review.statements;
-- ============================================================
