package com.wealth.goalsadvisory.controllertestcases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealth.goalsadvisory.controller.impl.RecommendationControllerImpl;
import com.wealth.goalsadvisory.dto.request.RecommendationRequest;
import com.wealth.goalsadvisory.dto.response.RecommendationResponse;
import com.wealth.goalsadvisory.enums.RecommendationStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import com.wealth.goalsadvisory.exception.ResourceNotFoundException;
import com.wealth.goalsadvisory.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationControllerImpl.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;
    @Autowired
    private ObjectMapper objectMapper;
    private RecommendationRequest recommendationRequest;
    private RecommendationResponse recommendationResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        recommendationRequest = new RecommendationRequest();
        recommendationRequest.setClientId(101L);
        recommendationRequest.setRiskClass(RiskClass.BALANCED);
        recommendationRequest.setProposalJson(
                "{\"notes\": \"Balanced suits client\"}");
        recommendationRequest.setProposedDate(LocalDate.now());
        recommendationRequest.setStatus(RecommendationStatus.DRAFT);

        recommendationResponse = new RecommendationResponse();
        recommendationResponse.setRecoId(1L);
        recommendationResponse.setClientId(101L);
        recommendationResponse.setModelId(1L);
        recommendationResponse.setModelName("Balanced Growth Portfolio");
        recommendationResponse.setRiskClass(RiskClass.BALANCED);
        recommendationResponse.setProposalJson(
                "{\"notes\": \"Balanced suits client\"}");
        recommendationResponse.setProposedDate(LocalDate.now());
        recommendationResponse.setStatus(RecommendationStatus.DRAFT);
    }

    @Test
    void createRecommendation_positive() throws Exception {
        when(recommendationService.createRecommendation(
                any(RecommendationRequest.class)))
                .thenReturn(recommendationResponse);

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                recommendationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recoId").value(1))
                .andExpect(jsonPath("$.clientId").value(101))
                .andExpect(jsonPath("$.riskClass").value("BALANCED"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }
//negative testcase when id is missing
    @Test
    void createRecommendation_negative() throws Exception {
        recommendationRequest.setClientId(null);

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                recommendationRequest)))
                .andExpect(status().isBadRequest());
    }
//negative when riskclass is missing
    @Test
    void createRecommendation_negative_rc() throws Exception {
        when(recommendationService.createRecommendation(
                any(RecommendationRequest.class)))
                .thenThrow(new ResourceNotFoundException(
                        "No active model portfolio found for risk class: BALANCED"));

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                recommendationRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRecommendationById_valid() throws Exception {
        when(recommendationService.getRecommendationById(1L))
                .thenReturn(recommendationResponse);

        mockMvc.perform(get("/api/recommendations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recoId").value(1))
                .andExpect(jsonPath("$.clientId").value(101));
    }
//when id is missing
    @Test
    void getRecommendationById_invalid() throws Exception {
        when(recommendationService.getRecommendationById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "Recommendation not found with id: 99"));

        mockMvc.perform(get("/api/recommendations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRecommendationStatus() throws Exception {
        recommendationResponse.setStatus(RecommendationStatus.SUBMITTED);
        when(recommendationService.updateRecommendationStatus(
                eq(1L), eq(RecommendationStatus.SUBMITTED)))
                .thenReturn(recommendationResponse);

        mockMvc.perform(patch("/api/recommendations/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"SUBMITTED\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void deleteRecommendation() throws Exception {
        doNothing().when(recommendationService).deleteRecommendation(1L);

        mockMvc.perform(delete("/api/recommendations/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Recommendation deleted successfully with id: 1"));
    }
//deletion done when the status id draft only
    @Test
    void deleteRecommendation_draft() throws Exception {
        doThrow(new IllegalArgumentException(
                "Only DRAFT recommendations can be deleted."))
                .when(recommendationService).deleteRecommendation(1L);

        mockMvc.perform(delete("/api/recommendations/1"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void getAllRecommendations_success() throws Exception {
        when(recommendationService.getAllRecommendations())
                .thenReturn(java.util.List.of(recommendationResponse));

        mockMvc.perform(get("/api/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].recoId").value(1));
    }

    @Test
    void getRecommendationsByClientId_success() throws Exception {
        when(recommendationService.getRecommendationsByClientId(101L))
                .thenReturn(java.util.List.of(recommendationResponse));

        mockMvc.perform(get("/api/recommendations/client/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value(101));
    }

    @Test
    void getRecommendationsByClientIdAndStatus_success() throws Exception {
        when(recommendationService.getRecommendationsByClientIdAndStatus(101L, RecommendationStatus.DRAFT))
                .thenReturn(java.util.List.of(recommendationResponse));

        mockMvc.perform(get("/api/recommendations/client/101/status/DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("DRAFT"));
    }

    @Test
    void getRecommendationsByModelId_success() throws Exception {
        when(recommendationService.getRecommendationsByModelId(1L))
                .thenReturn(java.util.List.of(recommendationResponse));

        mockMvc.perform(get("/api/recommendations/model/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].modelId").value(1));
    }

    @Test
    void updateRecommendation_success() throws Exception {
        when(recommendationService.updateRecommendation(eq(1L), any(RecommendationRequest.class)))
                .thenReturn(recommendationResponse);

        mockMvc.perform(put("/api/recommendations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recommendationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recoId").value(1));
    }

    @Test
    void updateRecommendationStatus_invalidEnum() throws Exception {
        // Testing how the controller handles an invalid status string
        mockMvc.perform(patch("/api/recommendations/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"INVALID_STATUS\""))
                .andExpect(status().isBadRequest());
    }
}