package com.wealthpro.controller;

import com.wealthpro.dto.request.RiskProfileRequestDTO;
import com.wealthpro.dto.response.RiskProfileResponseDTO;
import com.wealthpro.security.AuthContext;
import com.wealthpro.service.RiskProfileService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/clients")
public class RiskProfileController {

    private final RiskProfileService riskProfileService;

    public RiskProfileController(RiskProfileService riskProfileService) {
        this.riskProfileService = riskProfileService;
    }

    // POST /api/clients/{clientId}/risk-profile — submit questionnaire.
    // CLIENT may submit their OWN; staff may submit for any client.
    @PostMapping("/{clientId}/risk-profile")
    public ResponseEntity<RiskProfileResponseDTO> createRiskProfile(
            @PathVariable Long clientId,
            @Valid @RequestBody RiskProfileRequestDTO requestDTO,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only submit your own risk profile.");
        }

        RiskProfileResponseDTO response = riskProfileService.createRiskProfile(
                clientId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
    }

    // GET /api/clients/{clientId}/risk-profile — view.
    // CLIENT may view their OWN; staff may view any client's.
    @GetMapping("/{clientId}/risk-profile")
    public ResponseEntity<RiskProfileResponseDTO> getRiskProfileByClientId(
            @PathVariable Long clientId,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view your own risk profile.");
        }

        return ResponseEntity.ok(
                riskProfileService.getRiskProfileByClientId(clientId)); // 200
    }


    // PUT /api/clients/{clientId}/risk-profile — staff-only update
    // (clients can only submit once — no re-take).
    @PutMapping("/{clientId}/risk-profile")
    public ResponseEntity<RiskProfileResponseDTO> updateRiskProfile(
            @PathVariable Long clientId,
            @Valid @RequestBody RiskProfileRequestDTO requestDTO,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot update their own risk profile. Contact your RM.");
        }

        return ResponseEntity.ok(
                riskProfileService.updateRiskProfile(clientId, requestDTO)); // 200
    }


    // DELETE /api/clients/{clientId}/risk-profile — staff-only.
    @DeleteMapping("/{clientId}/risk-profile")
    public ResponseEntity<String> deleteRiskProfile(
            @PathVariable Long clientId,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot delete risk profiles.");
        }

        riskProfileService.deleteRiskProfile(clientId);
        return ResponseEntity.ok("Risk profile deleted successfully"); // 200
    }
}
