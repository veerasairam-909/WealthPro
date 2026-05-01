package com.reviewservice.enums;

/**
 * Represents the lifecycle status of a periodic client review.
 */
public enum ReviewStatus {

    SCHEDULED,      // Review has been scheduled but not yet conducted
    IN_PROGRESS,    // Review is currently being conducted
    COMPLETED,      // Review has been completed and signed off
    CANCELLED       // Review was cancelled
}
