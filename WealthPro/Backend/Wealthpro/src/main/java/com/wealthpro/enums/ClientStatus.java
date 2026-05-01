package com.wealthpro.enums;

public enum ClientStatus {

    /** Self-registered user awaiting KYC from an RM. Cannot trade yet. */
    PENDING_KYC,

    /** Fully onboarded and active. */
    Active,

    /** Deactivated / dormant. */
    Inactive

}
