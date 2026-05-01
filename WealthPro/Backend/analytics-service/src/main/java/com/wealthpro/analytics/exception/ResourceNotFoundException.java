package com.wealthpro.analytics.exception;

/** Thrown when a requested resource cannot be found. Mapped to 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
    public ResourceNotFoundException(String resourceName, Long id) { super(resourceName + " not found with ID: " + id); }
}
