package com.wealthpro.service;

import com.wealthpro.dto.request.SuitabilityRuleRequestDTO;
import com.wealthpro.dto.response.SuitabilityRuleResponseDTO;
import com.wealthpro.entities.SuitabilityRule;
import com.wealthpro.enums.RuleStatus;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.repositories.SuitabilityRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SuitabilityRuleServiceImplTest {

    @Mock
    private SuitabilityRuleRepository suitabilityRuleRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SuitabilityRuleServiceImpl suitabilityRuleService;

    private SuitabilityRule rule;
    private SuitabilityRuleRequestDTO requestDTO;
    private SuitabilityRuleResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        rule = new SuitabilityRule();
        rule.setRuleId(1L);
        rule.setDescription("Conservative clients allowed only in low risk assets");
        rule.setExpression("IF riskClass == Conservative THEN allowedAssets = [Bond, FD]");
        rule.setStatus(RuleStatus.Active);

        requestDTO = new SuitabilityRuleRequestDTO();
        requestDTO.setDescription("Conservative clients allowed only in low risk assets");
        requestDTO.setExpression("IF riskClass == Conservative THEN allowedAssets = [Bond, FD]");
        requestDTO.setStatus(RuleStatus.Active);

        responseDTO = new SuitabilityRuleResponseDTO();
        responseDTO.setRuleId(1L);
        responseDTO.setDescription("Conservative clients allowed only in low risk assets");
        responseDTO.setExpression("IF riskClass == Conservative THEN allowedAssets = [Bond, FD]");
        responseDTO.setStatus(RuleStatus.Active);
    }

    // ─────────────────────────────────────────
    // TEST 1: Create rule — success
    // ─────────────────────────────────────────
    @Test
    void testCreateRule_Success() {
        // Arrange
        when(modelMapper.map(requestDTO, SuitabilityRule.class)).thenReturn(rule);
        when(suitabilityRuleRepository.save(any(SuitabilityRule.class))).thenReturn(rule);
        when(modelMapper.map(rule, SuitabilityRuleResponseDTO.class)).thenReturn(responseDTO);

        // Act
        SuitabilityRuleResponseDTO result =
                suitabilityRuleService.createRule(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRuleId());
        assertEquals(RuleStatus.Active, result.getStatus());

        verify(suitabilityRuleRepository, times(1)).save(any(SuitabilityRule.class));
    }

    // ─────────────────────────────────────────
    // TEST 2: Get all rules
    // ─────────────────────────────────────────
    @Test
    void testGetAllRules_ReturnsList() {
        // Arrange
        when(suitabilityRuleRepository.findAll()).thenReturn(List.of(rule));
        when(modelMapper.map(rule, SuitabilityRuleResponseDTO.class)).thenReturn(responseDTO);

        // Act
        List<SuitabilityRuleResponseDTO> result =
                suitabilityRuleService.getAllRules();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(suitabilityRuleRepository, times(1)).findAll();
    }

    // ─────────────────────────────────────────
    // TEST 3: Get rule by ID — success
    // ─────────────────────────────────────────
    @Test
    void testGetRuleById_Success() {
        // Arrange
        when(suitabilityRuleRepository.findById(1L)).thenReturn(Optional.of(rule));
        when(modelMapper.map(rule, SuitabilityRuleResponseDTO.class)).thenReturn(responseDTO);

        // Act
        SuitabilityRuleResponseDTO result = suitabilityRuleService.getRuleById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRuleId());

        verify(suitabilityRuleRepository, times(1)).findById(1L);
    }

    // ─────────────────────────────────────────
    // TEST 4: Get rule by ID — not found → exception
    // ─────────────────────────────────────────
    @Test
    void testGetRuleById_NotFound_ThrowsException() {
        // Arrange
        when(suitabilityRuleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            suitabilityRuleService.getRuleById(999L);
        });
    }

    // ─────────────────────────────────────────
    // TEST 5: Update rule — success
    // ─────────────────────────────────────────
    @Test
    void testUpdateRule_Success() {
        // Arrange
        when(suitabilityRuleRepository.findById(1L)).thenReturn(Optional.of(rule));
        when(suitabilityRuleRepository.save(any(SuitabilityRule.class))).thenReturn(rule);
        when(modelMapper.map(rule, SuitabilityRuleResponseDTO.class)).thenReturn(responseDTO);

        // Act
        SuitabilityRuleResponseDTO result =
                suitabilityRuleService.updateRule(1L, requestDTO);

        // Assert
        assertNotNull(result);
        verify(suitabilityRuleRepository, times(1)).save(any(SuitabilityRule.class));
    }

    // ─────────────────────────────────────────
    // TEST 6: Delete rule — success
    // ─────────────────────────────────────────
    @Test
    void testDeleteRule_Success() {
        // Arrange
        when(suitabilityRuleRepository.findById(1L)).thenReturn(Optional.of(rule));

        // Act
        suitabilityRuleService.deleteRule(1L);

        // Assert
        verify(suitabilityRuleRepository, times(1)).deleteById(1L);
    }

    // ─────────────────────────────────────────
    // TEST 7: Delete rule — not found → exception
    // ─────────────────────────────────────────
    @Test
    void testDeleteRule_NotFound_ThrowsException() {
        // Arrange
        when(suitabilityRuleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            suitabilityRuleService.deleteRule(999L);
        });

        verify(suitabilityRuleRepository, never()).deleteById(any());
    }
}