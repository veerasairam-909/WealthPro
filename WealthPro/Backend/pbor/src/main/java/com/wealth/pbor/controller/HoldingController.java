package com.wealth.pbor.controller;

import com.wealth.pbor.dto.request.HoldingRequest;
import com.wealth.pbor.dto.response.HoldingResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface HoldingController {

    ResponseEntity<HoldingResponse> createHolding(HoldingRequest request);

    ResponseEntity<HoldingResponse> getHoldingById(Long holdingId);

    ResponseEntity<List<HoldingResponse>> getHoldingsByAccountId(Long accountId);

    ResponseEntity<List<HoldingResponse>> getHoldingsBySecurityId(Long securityId);

    ResponseEntity<HoldingResponse> updateHolding(Long holdingId, HoldingRequest request);

    ResponseEntity<Void> deleteHolding(Long holdingId);
}