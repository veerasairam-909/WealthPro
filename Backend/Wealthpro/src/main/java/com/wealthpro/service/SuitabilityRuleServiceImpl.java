package com.wealthpro.service;

import com.wealthpro.dto.request.SuitabilityRuleRequestDTO;
import com.wealthpro.dto.response.SuitabilityRuleResponseDTO;
import com.wealthpro.entities.SuitabilityRule;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.repositories.SuitabilityRuleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class SuitabilityRuleServiceImpl implements SuitabilityRuleService{

    private final SuitabilityRuleRepository suitabilityRuleRepository;
    private final ModelMapper modelMapper;

    public SuitabilityRuleServiceImpl(SuitabilityRuleRepository suitabilityRuleRepository,
                                      ModelMapper modelMapper) {
        this.suitabilityRuleRepository = suitabilityRuleRepository;
        this.modelMapper = modelMapper;
    }


    // CREATE a suitability rule

    public SuitabilityRuleResponseDTO createRule(SuitabilityRuleRequestDTO requestDTO) {
        SuitabilityRule rule = modelMapper.map(requestDTO, SuitabilityRule.class);
        SuitabilityRule saved = suitabilityRuleRepository.save(rule);
        return mapToResponse(saved);
    }

    // GET all rules
    public List<SuitabilityRuleResponseDTO> getAllRules() {
        List<SuitabilityRule> rules = suitabilityRuleRepository.findAll();
        List<SuitabilityRuleResponseDTO> result = new ArrayList<SuitabilityRuleResponseDTO>();
        for (SuitabilityRule r : rules) {
            result.add(mapToResponse(r));
        }
        return result;
    }


    // GET rule by ID

    public SuitabilityRuleResponseDTO getRuleById(Long ruleId) {
        SuitabilityRule rule = findRuleOrThrow(ruleId);
        return mapToResponse(rule);
    }


    // UPDATE rule by ID

    public SuitabilityRuleResponseDTO updateRule(Long ruleId,
                                                 SuitabilityRuleRequestDTO requestDTO) {
        SuitabilityRule existing = findRuleOrThrow(ruleId);

        existing.setDescription(requestDTO.getDescription());
        existing.setExpression(requestDTO.getExpression());
        existing.setStatus(requestDTO.getStatus());

        SuitabilityRule updated = suitabilityRuleRepository.save(existing);
        return mapToResponse(updated);
    }


    // DELETE rule by ID

    public void deleteRule(Long ruleId) {
        findRuleOrThrow(ruleId);
        suitabilityRuleRepository.deleteById(ruleId);
    }


    // PRIVATE HELPERS
    private SuitabilityRule findRuleOrThrow(Long ruleId) {
        Optional<SuitabilityRule> optional = suitabilityRuleRepository.findById(ruleId);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new ResourceNotFoundException("Suitability rule not found with ID: " + ruleId);
        }
    }

    private SuitabilityRuleResponseDTO mapToResponse(SuitabilityRule rule) {
        return modelMapper.map(rule, SuitabilityRuleResponseDTO.class);
    }
}