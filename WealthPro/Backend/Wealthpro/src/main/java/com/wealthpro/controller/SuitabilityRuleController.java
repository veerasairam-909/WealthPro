package com.wealthpro.controller;

import com.wealthpro.dto.request.SuitabilityRuleRequestDTO;
import com.wealthpro.dto.response.SuitabilityRuleResponseDTO;
import com.wealthpro.service.SuitabilityRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suitability-rules")
public class SuitabilityRuleController {

    private final SuitabilityRuleService suitabilityRuleService;

    public SuitabilityRuleController(SuitabilityRuleService suitabilityRuleService) {
        this.suitabilityRuleService = suitabilityRuleService;
    }

    // POST /api/suitability-rules
    // Create a new suitability rule
    // Postman: Body → raw → JSON
    @PostMapping
    public ResponseEntity<SuitabilityRuleResponseDTO> createRule(
            @Valid @RequestBody SuitabilityRuleRequestDTO requestDTO) {

        SuitabilityRuleResponseDTO response = suitabilityRuleService.createRule(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
    }

    // GET /api/suitability-rules
    // Get all suitability rules
    @GetMapping
    public ResponseEntity<List<SuitabilityRuleResponseDTO>> getAllRules() {
        return ResponseEntity.ok(suitabilityRuleService.getAllRules()); // 200
    }

    // GET /api/suitability-rules/{ruleId}
    // Get a single rule by ID
    @GetMapping("/{ruleId}")
    public ResponseEntity<SuitabilityRuleResponseDTO> getRuleById(
            @PathVariable Long ruleId) {

        return ResponseEntity.ok(suitabilityRuleService.getRuleById(ruleId)); // 200
    }

    // PUT /api/suitability-rules/{ruleId}
    // Update a rule by ID
    // Postman: Body → raw → JSON
    @PutMapping("/{ruleId}")
    public ResponseEntity<SuitabilityRuleResponseDTO> updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody SuitabilityRuleRequestDTO requestDTO) {

        return ResponseEntity.ok(
                suitabilityRuleService.updateRule(ruleId, requestDTO)); // 200
    }

    // DELETE /api/suitability-rules/{ruleId}
    // Delete a rule by ID
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<String> deleteRule(
            @PathVariable Long ruleId) {

        suitabilityRuleService.deleteRule(ruleId);
        return ResponseEntity.ok("Suitability rule deleted successfully"); // 200
    }
}