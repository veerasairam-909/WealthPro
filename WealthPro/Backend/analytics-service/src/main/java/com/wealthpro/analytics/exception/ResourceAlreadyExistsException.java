package com.wealthpro.analytics.exception;

/** Thrown when creating a resource that already exists. Mapped to 409. */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) { super(message); }
}
