package com.wealthpro.controller;

import com.wealthpro.dto.request.AmlFlagRequestDTO;
import com.wealthpro.dto.request.AmlFlagReviewRequestDTO;
import com.wealthpro.dto.response.AmlFlagResponseDTO;
import com.wealthpro.service.AmlFlagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aml-flags")
public class AmlFlagController {

    private final AmlFlagService amlFlagService;

    public AmlFlagController(AmlFlagService amlFlagService) {
        this.amlFlagService = amlFlagService;
    }

    // POST /api/aml-flags
    // Create a new AML flag (COMPLIANCE / ADMIN only)
    @PostMapping
    public ResponseEntity<AmlFlagResponseDTO> createFlag(
            @Valid @RequestBody AmlFlagRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(amlFlagService.createFlag(requestDTO));
    }

    // GET /api/aml-flags
    // Get all AML flags — optionally filter by status query param
    @GetMapping
    public ResponseEntity<List<AmlFlagResponseDTO>> getFlags(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(amlFlagService.getFlagsByStatus(status));
        }
        return ResponseEntity.ok(amlFlagService.getAllFlags());
    }

    // GET /api/aml-flags/{id}
    // Get a single flag by ID
    @GetMapping("/{id}")
    public ResponseEntity<AmlFlagResponseDTO> getFlagById(@PathVariable Long id) {
        return ResponseEntity.ok(amlFlagService.getFlagById(id));
    }

    // GET /api/aml-flags/client/{clientId}
    // Get all AML flags for a specific client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AmlFlagResponseDTO>> getFlagsByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(amlFlagService.getFlagsByClient(clientId));
    }

    // PUT /api/aml-flags/{id}/review
    // Review (update status) of an AML flag
    @PutMapping("/{id}/review")
    public ResponseEntity<AmlFlagResponseDTO> reviewFlag(
            @PathVariable Long id,
            @Valid @RequestBody AmlFlagReviewRequestDTO reviewDTO,
            @RequestHeader(value = "X-Auth-Username", required = false) String reviewedBy) {
        String reviewer = (reviewedBy != null) ? reviewedBy : "compliance";
        return ResponseEntity.ok(amlFlagService.reviewFlag(id, reviewDTO, reviewer));
    }

    // PUT /api/aml-flags/{id}/request-closure
    // RM calls this after investigating to request the compliance analyst close the flag.
    // Does NOT change the flag status — only sends a notification to compliance.
    @PutMapping("/{id}/request-closure")
    public ResponseEntity<AmlFlagResponseDTO> requestClosure(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-Username", required = false) String rmUsername) {
        String rm = (rmUsername != null && !rmUsername.isBlank()) ? rmUsername : "RM";
        return ResponseEntity.ok(amlFlagService.requestClosure(id, rm));
    }

    // DELETE /api/aml-flags/{id}
    // Delete an AML flag record
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFlag(@PathVariable Long id) {
        amlFlagService.deleteFlag(id);
        return ResponseEntity.ok("AML flag deleted successfully");
    }
}
