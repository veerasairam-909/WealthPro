package com.wealthpro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthpro.dto.request.SuitabilityRuleRequestDTO;
import com.wealthpro.dto.response.SuitabilityRuleResponseDTO;
import com.wealthpro.enums.RuleStatus;
import com.wealthpro.exception.GlobalExceptionHandler;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.service.SuitabilityRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SuitabilityRuleController.class)
@Import(GlobalExceptionHandler.class)
public class SuitabilityRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SuitabilityRuleService suitabilityRuleService;

    @Autowired
    private ObjectMapper objectMapper;

    private SuitabilityRuleRequestDTO requestDTO;
    private SuitabilityRuleResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
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

    // TEST 1: POST /api/suitability-rules — 201 Created
    @Test
    void testCreateRule_Returns201() throws Exception {
        // Arrange
        when(suitabilityRuleService.createRule(any(SuitabilityRuleRequestDTO.class)))
                .thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(post("/api/suitability-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())                    // 201
                .andExpect(jsonPath("$.ruleId").value(1))
                .andExpect(jsonPath("$.status").value("Active"));

        verify(suitabilityRuleService, times(1))
                .createRule(any(SuitabilityRuleRequestDTO.class));
    }

    // TEST 2: POST — 400 Validation error
    @Test
    void testCreateRule_InvalidBody_Returns400() throws Exception {
        SuitabilityRuleRequestDTO emptyRequest = new SuitabilityRuleRequestDTO();

        mockMvc.perform(post("/api/suitability-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());                // 400
    }

    // TEST 3: GET /api/suitability-rules — 200 OK
    @Test
    void testGetAllRules_Returns200() throws Exception {
        when(suitabilityRuleService.getAllRules()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/suitability-rules"))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ruleId").value(1));

        verify(suitabilityRuleService, times(1)).getAllRules();
    }

    // TEST 4: GET /api/suitability-rules/{ruleId} — 200 OK
    @Test
    void testGetRuleById_Returns200() throws Exception {
        // Arrange
        when(suitabilityRuleService.getRuleById(1L)).thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(get("/api/suitability-rules/1"))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.ruleId").value(1))
                .andExpect(jsonPath("$.status").value("Active"));
    }

    // ─────────────────────────────────────────
    // TEST 5: GET — 404 Not Found
    // ─────────────────────────────────────────
    @Test
    void testGetRuleById_NotFound_Returns404() throws Exception {
        // Arrange
        when(suitabilityRuleService.getRuleById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Suitability rule not found with ID: 999"));

        // Act + Assert
        mockMvc.perform(get("/api/suitability-rules/999"))
                .andExpect(status().isNotFound())                   // 404
                .andExpect(jsonPath("$.message")
                        .value("Suitability rule not found with ID: 999"));
    }

    // ─────────────────────────────────────────
    // TEST 6: PUT /api/suitability-rules/{ruleId} — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testUpdateRule_Returns200() throws Exception {
        // Arrange
        when(suitabilityRuleService.updateRule(eq(1L),
                any(SuitabilityRuleRequestDTO.class)))
                .thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(put("/api/suitability-rules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.ruleId").value(1));

        verify(suitabilityRuleService, times(1))
                .updateRule(eq(1L), any(SuitabilityRuleRequestDTO.class));
    }

    // ─────────────────────────────────────────
    // TEST 7: DELETE /api/suitability-rules/{ruleId} — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testDeleteRule_Returns200() throws Exception {
        // Arrange
        doNothing().when(suitabilityRuleService).deleteRule(1L);

        // Act + Assert
        mockMvc.perform(delete("/api/suitability-rules/1"))
                .andExpect(status().isOk())                         // 200
                .andExpect(content().string("Suitability rule deleted successfully"));

        verify(suitabilityRuleService, times(1)).deleteRule(1L);
    }

    // TEST 8: DELETE — 404 Not Found
    @Test
    void testDeleteRule_NotFound_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException(
                "Suitability rule not found with ID: 999"))
                .when(suitabilityRuleService).deleteRule(999L);

        // Act + Assert
        mockMvc.perform(delete("/api/suitability-rules/999"))
                .andExpect(status().isNotFound())                   // 404
                .andExpect(jsonPath("$.message")
                        .value("Suitability rule not found with ID: 999"));
    }
}