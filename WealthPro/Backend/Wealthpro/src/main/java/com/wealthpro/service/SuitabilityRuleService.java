package com.wealthpro.service;

import com.wealthpro.dto.request.SuitabilityRuleRequestDTO;
import com.wealthpro.dto.response.SuitabilityRuleResponseDTO;

import java.util.List;

public interface SuitabilityRuleService {

    SuitabilityRuleResponseDTO createRule(SuitabilityRuleRequestDTO requestDTO);

    List<SuitabilityRuleResponseDTO> getAllRules();

    SuitabilityRuleResponseDTO getRuleById(Long ruleId);

    SuitabilityRuleResponseDTO updateRule(Long ruleId,
                                          SuitabilityRuleRequestDTO requestDTO);

    void deleteRule(Long ruleId);
}