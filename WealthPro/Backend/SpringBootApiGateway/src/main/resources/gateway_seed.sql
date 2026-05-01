-- ============================================================
--  WealthPro — Gateway Database Seed Data
--  Database : gateway_db
--
--  PURPOSE:
--  This file only seeds the `auditlog` table with sample entries.
--  It does NOT seed any user accounts.
--
--  HOW USERS ARE CREATED (no SQL needed):
--    1. ADMIN        → seeded once via admin_seed.sql
--                      (BCrypt hash generated at bcrypt-generator.com)
--    2. CLIENT       → public self-registration
--                      POST /auth/register/client
--    3. RM / DEALER / → created by ADMIN via API
--       COMPLIANCE       POST /auth/users/register
--
--  BCrypt is applied automatically by Spring Security's
--  PasswordEncoder bean, so no manual hashing is ever required
--  at runtime. The admin_seed.sql is the ONLY place a manual
--  hash is needed, because the ADMIN role cannot be created
--  through the API (blocked in UserController.guardRole).
-- ============================================================

USE gateway_db;

-- ── Reset auditlog ────────────────────────────────────────────
TRUNCATE TABLE auditlog;
ALTER TABLE auditlog AUTO_INCREMENT = 1;

-- ── Optional: Seed demo CLIENT logins linked to kyc.Client ─────
-- Uncomment & replace <BCRYPT_OF_Wealth@2024> with a BCrypt hash
-- (strength 10) generated at https://bcrypt-generator.com/ for the
-- password "Wealth@2024". These usernames match kyc_seed.sql Client.Username
-- so a CLIENT login will resolve to the correct clientId via /auth/login.
--
-- INSERT IGNORE INTO users (username, password, name, email, phone, roles) VALUES
-- ('client1',  '<BCRYPT_OF_Wealth@2024>', 'Priya Sharma',  'priya.sharma@gmail.com',  '9123456781', 'CLIENT'),
-- ('client2',  '<BCRYPT_OF_Wealth@2024>', 'Rohan Verma',   'rohan.verma@gmail.com',   '9123456782', 'CLIENT'),
-- ('client3',  '<BCRYPT_OF_Wealth@2024>', 'Anjali Singh',  'anjali.singh@gmail.com',  '9123456783', 'CLIENT'),
-- ('client4',  '<BCRYPT_OF_Wealth@2024>', 'Vikram Patel',  'vikram.patel@gmail.com',  '9123456784', 'CLIENT'),
-- ('client5',  '<BCRYPT_OF_Wealth@2024>', 'Deepa Nambiar', 'deepa.nambiar@gmail.com', '9123456785', 'CLIENT');

-- ── Sample Audit Log Entries ──────────────────────────────────
INSERT INTO auditlog (id, username, roles, remoteAddress, method, pathinfo, action, resource, timestamp, metadata) VALUES
(1,  'admin',       '[ROLE_ADMIN]',      '127.0.0.1:52001', 'POST',   '/auth/users/register', 'POST',   '/auth/users/register', '2025-01-10 09:00:00', '{"createdUser":"rm1","role":"RM"}'),
(2,  'admin',       '[ROLE_ADMIN]',      '127.0.0.1:52002', 'POST',   '/auth/users/register', 'POST',   '/auth/users/register', '2025-01-10 09:05:00', '{"createdUser":"rm2","role":"RM"}'),
(3,  'admin',       '[ROLE_ADMIN]',      '127.0.0.1:52003', 'POST',   '/auth/users/register', 'POST',   '/auth/users/register', '2025-01-10 09:10:00', '{"createdUser":"rm3","role":"RM"}'),
(4,  'admin',       '[ROLE_ADMIN]',      '127.0.0.1:52004', 'POST',   '/auth/users/register', 'POST',   '/auth/users/register', '2025-01-10 09:15:00', '{"createdUser":"dealer1","role":"DEALER"}'),
(5,  'admin',       '[ROLE_ADMIN]',      '127.0.0.1:52005', 'POST',   '/auth/users/register', 'POST',   '/auth/users/register', '2025-01-10 09:20:00', '{"createdUser":"compliance1","role":"COMPLIANCE"}'),
(6,  'rm1',         '[ROLE_RM]',         '127.0.0.1:52010', 'POST',   '/api/clients',         'POST',   '/api/clients',         '2025-01-14 09:30:00', '{"clientCreated":1,"clientName":"Priya Sharma"}'),
(7,  'rm1',         '[ROLE_RM]',         '127.0.0.1:52011', 'POST',   '/api/clients/1/kyc',   'POST',   '/api/clients/1/kyc',   '2025-01-14 09:45:00', '{"kycId":1,"documentType":"AADHAAR"}'),
(8,  'dealer1',     '[ROLE_DEALER]',     '127.0.0.1:52020', 'POST',   '/api/orders/1/route',  'POST',   '/api/orders/1/route',  '2025-01-14 10:18:00', '{"orderId":1,"venue":"SIMULATED_BSE"}'),
(9,  'compliance1', '[ROLE_COMPLIANCE]', '127.0.0.1:52030', 'GET',    '/api/compliance-breaches/1', 'GET', '/api/compliance-breaches/1', '2025-02-15 14:05:00', '{"breachId":2,"severity":"MEDIUM"}'),
(10, 'client1',     '[ROLE_CLIENT]',     '127.0.0.1:52040', 'GET',    '/api/analytics/1/dashboard', 'GET', '/api/analytics/1/dashboard', '2025-03-31 19:00:00', '{"accountId":1}');

-- ── Verify ────────────────────────────────────────────────────
SELECT 'users'    AS tbl, COUNT(*) AS row_count FROM users
UNION ALL
SELECT 'auditlog', COUNT(*) FROM auditlog;

-- Confirm userId was auto-assigned to existing rows after schema migration:
-- SELECT userId, username, roles FROM users ORDER BY userId;
-- ============================================================
