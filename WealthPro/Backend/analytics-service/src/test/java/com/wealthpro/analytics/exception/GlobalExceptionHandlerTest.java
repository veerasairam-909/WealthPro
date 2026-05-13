package com.wealthpro.analytics.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for analytics GlobalExceptionHandler.
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
        ResourceNotFoundException ex = new ResourceNotFoundException("Account not found with id: 5");

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
    }

    @Test
    void testHandleResourceNotFoundException_MessageInBody() {
        ResourceNotFoundException ex = new ResourceNotFoundException("ComplianceBreach", 42L);

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFoundException(ex);

        String message = (String) response.getBody().get("message");
        assertTrue(message.contains("42"));
    }

    @Test
    void testHandleResourceNotFoundException_TimestampPresent() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Account", 1L);
        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFoundException(ex);
        assertNotNull(response.getBody().get("timestamp"));
    }

    // ─── ResourceAlreadyExistsException → 409 ─────────────────────────────────

    @Test
    void testHandleResourceAlreadyExistsException_Returns409() {
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException("Record already exists");

        ResponseEntity<Map<String, Object>> response = handler.handleResourceAlreadyExistsException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().get("status"));
        assertEquals("Record already exists", response.getBody().get("message"));
    }

    // ─── IllegalStateException → 400 ──────────────────────────────────────────

    @Test
    void testHandleIllegalStateException_Returns400() {
        IllegalStateException ex = new IllegalStateException("Can only close ACKNOWLEDGED breaches");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalStateException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Can only close ACKNOWLEDGED breaches", response.getBody().get("message"));
    }

    // ─── ResponseStatusException → original status ────────────────────────────

    @Test
    void testHandleResponseStatusException_Returns403() {
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Not authorised.");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().get("status"));
        assertEquals("Not authorised.", response.getBody().get("message"));
    }

    @Test
    void testHandleResponseStatusException_NullReason_UsesDefaultPhrase() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatusException(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody().get("message"));
    }

    // ─── Generic Exception → 500 ──────────────────────────────────────────────

    @Test
    void testHandleGenericException_Returns500() {
        Exception ex = new RuntimeException("Unexpected DB failure");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().get("status"));
        String message = (String) response.getBody().get("message");
        assertTrue(message.contains("Unexpected DB failure"));
    }

    @Test
    void testHandleGenericException_TimestampInBody() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGenericException(new RuntimeException("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}
