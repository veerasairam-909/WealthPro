-- ============================================================
--  WealthPro — Notification Database Seed Data
--  Database : notification
-- ============================================================

USE notification;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notification;
ALTER TABLE notification AUTO_INCREMENT = 1;
SET FOREIGN_KEY_CHECKS = 1;

-- ── Notification ──────────────────────────────────────────────
-- userid corresponds to client_id from kyc.client for CLIENT users
-- userid 101-105 represent staff users (RM, Advisor, etc.)
INSERT INTO notification (notificationid, userid, message, category, status, created_date) VALUES
(1,  1,  'Your order for HDFCBANK (100 shares) has been successfully filled at Rs.1648.50.',                            'Order',            'Read',      '2025-01-14 10:25:00'),
(2,  2,  'SBI Bluechip Fund subscription of 500 units completed at NAV Rs.52.75.',                                     'Order',            'Read',      '2025-01-17 15:05:00'),
(3,  3,  'KYC verification completed. Your account is now fully active.',                                               'Compliance',       'Read',      '2025-01-22 09:00:00'),
(4,  4,  'Vikram Patel - HDFCBANK single-asset concentration breach detected. Action required.',                        'Compliance',       'Unread',    '2025-02-15 14:00:00'),
(5,  5,  'Your order for Nifty BeES ETF (300 units) has been filled at Rs.213.25.',                                    'Order',            'Read',      '2025-02-08 10:10:00'),
(6,  1,  'HDFC Bank has declared an interim dividend of Rs.15 per share. Pay date: 20-Jan-2025.',                      'CorporateAction',  'Read',      '2025-01-20 09:00:00'),
(7,  2,  'Your quarterly portfolio review has been scheduled for April 2025. Please confirm your availability.',        'Review',           'Unread',    '2025-03-25 10:00:00'),
(8,  8,  'Compliance Alert: Your bond concentration exceeds 60% of portfolio. Rebalancing recommended.',               'Compliance',       'Unread',    '2025-02-20 09:05:00'),
(9,  3,  'Order for Infosys (50 shares) executed at market price Rs.1918.00 on NSE.',                                  'Order',            'Dismissed', '2025-01-21 09:55:00'),
(10, 7,  'Joint account corporate action: Tata Steel bonus shares (1:2 ratio) credited. Record date: 15-Feb-2025.',   'CorporateAction',  'Unread',    '2025-02-20 10:00:00');

-- ── Verify ────────────────────────────────────────────────────
SELECT 'notification' AS tbl, COUNT(*) AS row_count FROM notification;
-- ============================================================
