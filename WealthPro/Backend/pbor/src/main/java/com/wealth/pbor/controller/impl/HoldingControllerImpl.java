package com.wealth.pbor.controller.impl;

import com.wealth.pbor.dto.request.HoldingRequest;
import com.wealth.pbor.dto.response.HoldingResponse;
import com.wealth.pbor.security.AuthContext;
import com.wealth.pbor.security.OwnershipGuard;
import com.wealth.pbor.service.HoldingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
public class HoldingControllerImpl {

    private final HoldingService holdingService;
    private final OwnershipGuard ownershipGuard;

    @PostMapping
    public ResponseEntity<HoldingResponse> createHolding(@Valid @RequestBody HoldingRequest request) {
        // Writes are staff-only at gateway.
        HoldingResponse response = holdingService.createHolding(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{holdingId}")
    public ResponseEntity<HoldingResponse> getHoldingById(
            @PathVariable Long holdingId,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        HoldingResponse response = holdingService.getHoldingById(holdingId);
        // Holding response exposes accountId — enforce ownership on it.
        ownershipGuard.checkAccount(new AuthContext(null, roles, authClientId), response.getAccountId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<HoldingResponse>> getAllHoldings(
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles) {
        if (new AuthContext(null, roles, null).isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot list all holdings. Use /account/{yourAccountId}.");
        }
        return ResponseEntity.ok(holdingService.getAllHoldings());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<HoldingResponse>> getHoldingsByAccountId(
            @PathVariable Long accountId,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        ownershipGuard.checkAccount(new AuthContext(null, roles, authClientId), accountId);
        return ResponseEntity.ok(holdingService.getHoldingsByAccountId(accountId));
    }

    @GetMapping("/security/{securityId}")
    public ResponseEntity<List<HoldingResponse>> getHoldingsBySecurityId(
            @PathVariable Long securityId,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles) {
        // Cross-client view — staff only.
        if (new AuthContext(null, roles, null).isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot query holdings across all accounts by security.");
        }
        return ResponseEntity.ok(holdingService.getHoldingsBySecurityId(securityId));
    }

    @PutMapping("/{holdingId}")
    public ResponseEntity<HoldingResponse> updateHolding(@PathVariable Long holdingId,
                                                         @Valid @RequestBody HoldingRequest request) {
        return ResponseEntity.ok(holdingService.updateHolding(holdingId, request));
    }

    @DeleteMapping("/{holdingId}")
    public ResponseEntity<String> deleteHolding(@PathVariable Long holdingId) {
        holdingService.deleteHolding(holdingId);
        return ResponseEntity.ok("Holding deleted successfully.");
    }
}
