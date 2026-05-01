package wealthpro.springbootapigateway.enums;

/**
 * Roles supported in WealthPro.
 * Stored as VARCHAR in MySQL (enum name string — e.g. "RM", "ADMIN").
 * Spring Data R2DBC maps enum ↔ String automatically via Enum.name() / Enum.valueOf().
 *
 * ADMIN      — Pre-seeded in DB. Cannot be created or assigned via API.
 * RM         — Relationship Manager. Onboards clients, manages portfolios,
 *              creates goals and investment recommendations.
 * DEALER     — Dealer / Trader. Executes and routes orders.
 * COMPLIANCE — Compliance Analyst. Monitors rules, breaches, KYC.
 * CLIENT     — End investor / client.
 */
public enum Role {
    ADMIN,
    RM,
    DEALER,
    COMPLIANCE,
    CLIENT
}
