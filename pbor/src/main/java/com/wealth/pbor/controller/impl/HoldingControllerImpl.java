package com.wealth.pbor.controller.impl;

import com.wealth.pbor.dto.request.HoldingRequest;
import com.wealth.pbor.dto.response.HoldingResponse;
import com.wealth.pbor.service.HoldingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
public class HoldingControllerImpl {

    private final HoldingService holdingService;

    @PostMapping
    public ResponseEntity<HoldingResponse> createHolding(@Valid @RequestBody HoldingRequest request) {
        HoldingResponse response = holdingService.createHolding(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{holdingId}")
    public ResponseEntity<HoldingResponse> getHoldingById(@PathVariable Long holdingId) {
        HoldingResponse response = holdingService.getHoldingById(holdingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<HoldingResponse>> getAllHoldings() {
        List<HoldingResponse> responseList = holdingService.getAllHoldings();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<HoldingResponse>> getHoldingsByAccountId(@PathVariable Long accountId) {
        List<HoldingResponse> responseList = holdingService.getHoldingsByAccountId(accountId);
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/security/{securityId}")
    public ResponseEntity<List<HoldingResponse>> getHoldingsBySecurityId(@PathVariable Long securityId) {
        List<HoldingResponse> responseList = holdingService.getHoldingsBySecurityId(securityId);
        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{holdingId}")
    public ResponseEntity<HoldingResponse> updateHolding(@PathVariable Long holdingId,
                                                         @Valid @RequestBody HoldingRequest request) {
        HoldingResponse response = holdingService.updateHolding(holdingId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{holdingId}")
    public ResponseEntity<String> deleteHolding(@PathVariable Long holdingId) {
        holdingService.deleteHolding(holdingId);
        return ResponseEntity.ok("Holding deleted successfully.");
    }
}
