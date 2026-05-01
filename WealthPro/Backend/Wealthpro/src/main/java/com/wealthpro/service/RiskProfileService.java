package com.wealthpro.service;

import com.wealthpro.dto.request.RiskProfileRequestDTO;
import com.wealthpro.dto.response.RiskProfileResponseDTO;

public interface RiskProfileService {

    RiskProfileResponseDTO createRiskProfile(Long clientId,
                                             RiskProfileRequestDTO requestDTO);

    RiskProfileResponseDTO getRiskProfileByClientId(Long clientId);

    RiskProfileResponseDTO getRiskProfileById(Long riskId);

    RiskProfileResponseDTO updateRiskProfile(Long clientId,
                                             RiskProfileRequestDTO requestDTO);

    void deleteRiskProfile(Long clientId);
}