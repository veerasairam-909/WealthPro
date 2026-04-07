package com.reviewservice.enums;

/**
 * Represents the generation/delivery status of a client statement.
 */
public enum StatementStatus {

    PENDING,        // Statement has been requested but not yet generated
    GENERATED,      // Statement has been successfully generated
    DELIVERED,      // Statement has been delivered to the client
    FAILED          // Statement generation or delivery failed
}
