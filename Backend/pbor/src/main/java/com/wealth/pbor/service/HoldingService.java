package com.wealth.pbor.service;

import com.wealth.pbor.dto.request.HoldingRequest;
import com.wealth.pbor.dto.response.HoldingResponse;

import java.util.List;

public interface HoldingService {

    HoldingResponse createHolding(HoldingRequest requestDTO);

    HoldingResponse getHoldingById(Long holdingId);

    List<HoldingResponse> getAllHoldings();

    List<HoldingResponse> getHoldingsByAccountId(Long accountId);

    List<HoldingResponse> getHoldingsBySecurityId(Long securityId);

    HoldingResponse updateHolding(Long holdingId, HoldingRequest requestDTO);

    void deleteHolding(Long holdingId);
}