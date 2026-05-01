-- ============================================================
--  WealthPro — Admin Account Seed Script
--  Database : gateway_db
-- ============================================================
--
--  PURPOSE:
--    Seeds the single pre-configured ADMIN account. The ADMIN
--    role can NEVER be created through the API (it is blocked
--    explicitly in UserController.guardRole), so this is the
--    only way to provision the first administrator.
--
--    All other users are created dynamically:
--      - CLIENT       → public self-registration
--                         POST /auth/register/client
--      - RM, → created by ADMIN only
--        DEALER,         POST /auth/users/register
--        COMPLIANCE
--
-- ------------------------------------------------------------
--  INSTRUCTIONS (run ONCE):
--
--  Step 1: Generate a BCrypt hash for your chosen admin password
--          using any online BCrypt generator, for example:
--            https://bcrypt-generator.com/
--            https://www.browserling.com/tools/bcrypt
--          Use strength/rounds = 10 (Spring Security default).
--
--          Example input  : Wealth@2024
--          Example output : $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa
--
--  Step 2: Paste the generated hash below where it says
--            <PASTE_BCRYPT_HASH_HERE>
--
--  Step 3: Run this script on gateway_db:
--            mysql -u root -p gateway_db < admin_seed.sql
--          OR paste and execute in MySQL Workbench / DBeaver.
--
--  Step 4: Verify:
--            SELECT username, name, email, roles
--            FROM users WHERE username = 'admin';
--
--  Step 5: Log in via the API to confirm:
--            POST /auth/login
--            { "username":"admin", "password":"<your-plaintext-password>" }
--
--  NOTE:
--    - INSERT IGNORE means re-running this is safe; it will
--      silently skip if the admin row already exists.
--    - Never commit the raw password to Git — only the hash.
-- ============================================================

USE gateway_db;

-- Safety net in case Hibernate/R2DBC hasn't created the tables yet
CREATE TABLE IF NOT EXISTS users (
    userId   BIGINT        NOT NULL AUTO_INCREMENT UNIQUE,
    username VARCHAR(100)  NOT NULL PRIMARY KEY,
    password VARCHAR(255)  NOT NULL,
    name     VARCHAR(255)  NOT NULL,
    email    VARCHAR(255)  NOT NULL,
    phone    VARCHAR(20)   NOT NULL,
    roles    VARCHAR(100)  NOT NULL
);

-- Column names here must EXACTLY match the @Column annotations in
-- AuditUsers.java (R2DBC does not translate camelCase ↔ snake_case).
CREATE TABLE IF NOT EXISTS auditlog (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(100),
    roles          VARCHAR(255),
    remoteAddress  VARCHAR(100),
    method         VARCHAR(10),
    pathinfo       VARCHAR(500),
    action         VARCHAR(100),
    resource       VARCHAR(500),
    timestamp      DATETIME,
    metadata       TEXT
);

-- ── Insert admin user (safe to re-run) ────────────────────────────────────────
INSERT IGNORE INTO users (username, password, name, email, phone, roles)
VALUES (
    'admin',
    '<PASTE_BCRYPT_HASH_HERE>',   -- BCrypt hash generated via bcrypt-generator.com
    'System Administrator',
    'admin@wealthpro.com',
    '0000000000',
    'ADMIN'
);

-- ── Verify ────────────────────────────────────────────────────────────────────
SELECT username, name, email, roles FROM users WHERE username = 'admin';
-- ============================================================
