package com.wealthpro.controller;

import com.wealthpro.dto.request.RiskProfileRequestDTO;
import com.wealthpro.dto.response.RiskProfileResponseDTO;
import com.wealthpro.service.RiskProfileService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class RiskProfileController {

    private final RiskProfileService riskProfileService;

    public RiskProfileController(RiskProfileService riskProfileService) {
        this.riskProfileService = riskProfileService;
    }

    // POST /api/clients/{clientId}/risk-profile
    // Create risk profile for a client
    // Postman: Body → raw → JSON
    @PostMapping("/{clientId}/risk-profile")
    public ResponseEntity<RiskProfileResponseDTO> createRiskProfile(
            @PathVariable Long clientId,
            @Valid @RequestBody RiskProfileRequestDTO requestDTO) {

        RiskProfileResponseDTO response = riskProfileService.createRiskProfile(
                clientId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
    }

    // GET /api/clients/{clientId}/risk-profile
    // Get risk profile by client ID
    @GetMapping("/{clientId}/risk-profile")
    public ResponseEntity<RiskProfileResponseDTO> getRiskProfileByClientId(
            @PathVariable Long clientId) {

        return ResponseEntity.ok(
                riskProfileService.getRiskProfileByClientId(clientId)); // 200
    }


    // PUT /api/clients/{clientId}/risk-profile
    // Update risk profile for a client
    // Postman: Body → raw → JSON

    @PutMapping("/{clientId}/risk-profile")
    public ResponseEntity<RiskProfileResponseDTO> updateRiskProfile(
            @PathVariable Long clientId,
            @Valid @RequestBody RiskProfileRequestDTO requestDTO) {

        return ResponseEntity.ok(
                riskProfileService.updateRiskProfile(clientId, requestDTO)); // 200
    }


    // DELETE /api/clients/{clientId}/risk-profile
    // Delete risk profile for a client
    @DeleteMapping("/{clientId}/risk-profile")
    public ResponseEntity<String> deleteRiskProfile(
            @PathVariable Long clientId) {

        riskProfileService.deleteRiskProfile(clientId);
        return ResponseEntity.ok("Risk profile deleted successfully"); // 200
    }
}