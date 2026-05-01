package com.wealthpro.orderexecution.exception;

/**
 * Custom exception thrown when a requested resource cannot be found.
 * <p>Mapped to HTTP 404 by {@link GlobalExceptionHandler}.</p>
 *
 * @author WealthPro Team
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience constructor producing a standardised message.
     *
     * @param resourceName the entity type name (e.g. "Order")
     * @param id           the ID that was not found
     */
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with ID: " + id);
    }
}
