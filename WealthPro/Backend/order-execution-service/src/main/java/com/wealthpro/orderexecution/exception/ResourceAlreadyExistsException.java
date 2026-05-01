package com.wealthpro.orderexecution.exception;

/**
 * Custom exception thrown when attempting to create a resource that already exists.
 * <p>Mapped to HTTP 409 (CONFLICT) by {@link GlobalExceptionHandler}.</p>
 *
 * @author WealthPro Team
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
