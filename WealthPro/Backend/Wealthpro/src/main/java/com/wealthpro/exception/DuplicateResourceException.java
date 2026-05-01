package com.wealthpro.exception;

// Thrown when trying to create something that already exists
// e.g. RiskProfile already exists for a client
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}