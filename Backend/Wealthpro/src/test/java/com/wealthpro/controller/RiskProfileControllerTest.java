package com.wealthpro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealthpro.dto.request.RiskProfileRequestDTO;
import com.wealthpro.dto.response.RiskProfileResponseDTO;
import com.wealthpro.enums.RiskClass;
import com.wealthpro.exception.DuplicateResourceException;
import com.wealthpro.exception.GlobalExceptionHandler;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.service.RiskProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiskProfileController.class)
@Import(GlobalExceptionHandler.class)
public class RiskProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RiskProfileService riskProfileService;

    @Autowired
    private ObjectMapper objectMapper;

    private RiskProfileRequestDTO requestDTO;
    private RiskProfileResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Register JavaTimeModule so ObjectMapper handles LocalDate correctly
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, String> answers = new HashMap<>();
        answers.put("q1", "C");
        answers.put("q2", "D");
        answers.put("q3", "C");
        answers.put("q4", "B");
        answers.put("q5", "C");

        requestDTO = new RiskProfileRequestDTO();
        requestDTO.setAnswers(answers);
        requestDTO.setAssessedDate(LocalDate.now());

        responseDTO = new RiskProfileResponseDTO();
        responseDTO.setRiskId(1L);
        responseDTO.setClientId(1L);
        responseDTO.setQuestionnaireJSON("{\"q1\":\"C\",\"q2\":\"D\"}");
        responseDTO.setRiskScore(BigDecimal.valueOf(75.00));
        responseDTO.setRiskClass(RiskClass.Aggressive);
        responseDTO.setAssessedDate(LocalDate.now());
    }

    // TEST 1: POST /api/clients/{clientId}/risk-profile — 201 Created
    @Test
    void testCreateRiskProfile_Returns201() throws Exception {
        when(riskProfileService.createRiskProfile(eq(1L),
                any(RiskProfileRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/clients/1/risk-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())                    // 201
                .andExpect(jsonPath("$.riskId").value(1))
                .andExpect(jsonPath("$.riskClass").value("Aggressive"))
                .andExpect(jsonPath("$.riskScore").value(75.00));

        verify(riskProfileService, times(1))
                .createRiskProfile(eq(1L), any(RiskProfileRequestDTO.class));
    }

    // TEST 2: POST — 409 Duplicate
    @Test
    void testCreateRiskProfile_Duplicate_Returns409() throws Exception {
        when(riskProfileService.createRiskProfile(eq(1L),
                any(RiskProfileRequestDTO.class)))
                .thenThrow(new DuplicateResourceException(
                        "Risk profile already exists for client ID: 1"));

        mockMvc.perform(post("/api/clients/1/risk-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())                   // 409
                .andExpect(jsonPath("$.message")
                        .value("Risk profile already exists for client ID: 1"));
    }

    // TEST 3: GET /api/clients/{clientId}/risk-profile — 200 OK
    @Test
    void testGetRiskProfileByClientId_Returns200() throws Exception {
        when(riskProfileService.getRiskProfileByClientId(1L))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/clients/1/risk-profile"))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.riskClass").value("Aggressive"))
                .andExpect(jsonPath("$.clientId").value(1));

        verify(riskProfileService, times(1)).getRiskProfileByClientId(1L);
    }

    // TEST 4: GET — 404 Not Found
    @Test
    void testGetRiskProfileByClientId_NotFound_Returns404() throws Exception {
        when(riskProfileService.getRiskProfileByClientId(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Risk profile not found for client ID: 999"));

        mockMvc.perform(get("/api/clients/999/risk-profile"))
                .andExpect(status().isNotFound())                   // 404
                .andExpect(jsonPath("$.message")
                        .value("Risk profile not found for client ID: 999"));
    }

    // TEST 5: PUT /api/clients/{clientId}/risk-profile — 200 OK
    @Test
    void testUpdateRiskProfile_Returns200() throws Exception {
        when(riskProfileService.updateRiskProfile(eq(1L),
                any(RiskProfileRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/clients/1/risk-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.riskClass").value("Aggressive"));

        verify(riskProfileService, times(1))
                .updateRiskProfile(eq(1L), any(RiskProfileRequestDTO.class));
    }


    // TEST 6: DELETE /api/clients/{clientId}/risk-profile — 200 OK
    @Test
    void testDeleteRiskProfile_Returns200() throws Exception {
        doNothing().when(riskProfileService).deleteRiskProfile(1L);

        mockMvc.perform(delete("/api/clients/1/risk-profile"))
                .andExpect(status().isOk())                         // 200
                .andExpect(content().string("Risk profile deleted successfully"));

        verify(riskProfileService, times(1)).deleteRiskProfile(1L);
    }

    // TEST 7: DELETE — 404 Not Found
    @Test
    void testDeleteRiskProfile_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException(
                "Risk profile not found for client ID: 999"))
                .when(riskProfileService).deleteRiskProfile(999L);

        mockMvc.perform(delete("/api/clients/999/risk-profile"))
                .andExpect(status().isNotFound())                   // 404
                .andExpect(jsonPath("$.message")
                        .value("Risk profile not found for client ID: 999"));
    }
}