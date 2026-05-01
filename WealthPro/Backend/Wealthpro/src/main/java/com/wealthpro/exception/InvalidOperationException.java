package com.wealthpro.exception;

// Thrown when a business rule is violated
// like trying to set KYC status from Verified back to Pending



public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}