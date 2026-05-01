package com.wealthpro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Resource Not Found
    // Triggered by: clientService.findClientOrThrow()
    //               kycService.findKYCOrThrow()
    //               riskProfileService.findRiskProfileOrThrow()
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 409 - Duplicate Resource
    // Triggered by: trying to create a second RiskProfile for same client
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResourceException(
            DuplicateResourceException ex) {

        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 400 - Invalid Operation (business rule violation)
    // Triggered by: KYC status going Verified → Pending
    //               updating an Expired KYC document
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOperationException(
            InvalidOperationException ex) {

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ─────────────────────────────────────────
    // 400 - Validation Errors
    // Triggered by: @Valid failing on any RequestDTO field
    // e.g. sending blank name, null segment, invalid date
    // Returns all field errors at once so user knows everything wrong
    // ─────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        // Collect all field-level errors into a map
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("messages", fieldErrors);   // shows all invalid fields

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ─────────────────────────────────────────
    // 400 - File Too Large
    // Triggered by: uploading a KYC document image bigger than
    //               spring.servlet.multipart.max-file-size in application.yml
    // ─────────────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex) {

        return buildResponse(HttpStatus.BAD_REQUEST,
                "File size exceeds the maximum allowed limit of 10MB");
    }

    // Preserve the HTTP status on ResponseStatusException.
    // Thrown by OwnershipGuard and controllers to signal 403/401/404 etc.
    // Without this handler, the catch-all below would downgrade every
    // ResponseStatusException to 500 Internal Server Error.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return buildResponse(status, message);
    }

    // 400 - Bad request (invalid input values, e.g. unknown enum)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 500 - Any other unexpected exception
    // Safety net — catches everything not handled above
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage());
    }


    // PRIVATE HELPER — builds consistent error response body
    // Every error response looks the same in Postman:
    // {
    //   "timestamp": "2026-03-10T10:30:00",
    //   "status": 404,
    //   "error": "Not Found",
    //   "message": "Client not found with ID: 5"
    // }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status,
                                                              String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}