package com.wealthpro.orderexecution.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests each exception handler method in isolation.
 */
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ─── ResourceNotFoundException → 404 ──────────────────────────────────────

    @Test
    void testHandleResourceNotFoundException_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Order not found with ID: 1");

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
    }

    @Test
    void testHandleResourceNotFoundException_MessageInBody() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Order", 99L);

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFoundException(ex);

        String message = (String) response.getBody().get("message");
        assertTrue(message.contains("Order"));
        assertTrue(message.contains("99"));
    }

    @Test
    void testHandleResourceNotFoundException_TimestampInBody() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Security", 5L);
        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFoundException(ex);
        assertNotNull(response.getBody().get("timestamp"));
    }

    // ─── ResourceAlreadyExistsException → 409 ─────────────────────────────────

    @Test
    void testHandleResourceAlreadyExistsException_Returns409() {
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException("Order already exists");

        ResponseEntity<Map<String, Object>> response = handler.handleResourceAlreadyExistsException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().get("status"));
        assertEquals("Order already exists", response.getBody().get("message"));
    }

    // ─── IllegalStateException → 400 ──────────────────────────────────────────

    @Test
    void testHandleIllegalStateException_Returns400() {
        IllegalStateException ex = new IllegalStateException("Cannot cancel a FILLED order");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalStateException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Cannot cancel a FILLED order", response.getBody().get("message"));
    }

    @Test
    void testHandleIllegalStateException_TimestampPresent() {
        IllegalStateException ex = new IllegalStateException("Invalid transition");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalStateException(ex);
        assertNotNull(response.getBody().get("timestamp"));
    }

    // ─── ResponseStatusException → original status ────────────────────────────

    @Test
    void testHandleResponseStatusException_Returns403() {
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Client does not own this order");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().get("status"));
        assertEquals("Client does not own this order", response.getBody().get("message"));
    }

    @Test
    void testHandleResponseStatusException_Returns401() {
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Token expired");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().get("status"));
    }

    @Test
    void testHandleResponseStatusException_NullReason_UsesDefaultPhrase() {
        // ResponseStatusException with no reason — should use the default reason phrase
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST);
        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatusException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().get("message"));
    }

    // ─── Generic Exception → 500 ──────────────────────────────────────────────

    @Test
    void testHandleGenericException_Returns500() {
        Exception ex = new RuntimeException("Something unexpected happened");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().get("status"));
        String message = (String) response.getBody().get("message");
        assertTrue(message.contains("Something unexpected happened"));
    }

    @Test
    void testHandleGenericException_TimestampInBody() {
        Exception ex = new RuntimeException("DB is down");
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);
        assertNotNull(response.getBody().get("timestamp"));
    }
}
