package com.wealth.goalsadvisory.controllertestcases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.goalsadvisory.controller.impl.ModelPortfolioControllerImpl;
import com.wealth.goalsadvisory.dto.request.ModelPortfolioRequest;
import com.wealth.goalsadvisory.dto.response.ModelPortfolioResponse;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import com.wealth.goalsadvisory.exception.ResourceNotFoundException;
import com.wealth.goalsadvisory.service.ModelPortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ModelPortfolioControllerImpl.class)
class ModelPortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModelPortfolioService modelPortfolioService;
    private ObjectMapper objectMapper;
    private ModelPortfolioRequest portfolioRequest;
    private ModelPortfolioResponse portfolioResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        portfolioRequest = new ModelPortfolioRequest();
        portfolioRequest.setName("Balanced Growth Portfolio");
        portfolioRequest.setRiskClass(RiskClass.BALANCED);
        portfolioRequest.setWeightsJson(
                "{\"Equity\": 60, \"Bond\": 30, \"Derivatives\": 10}");
        portfolioRequest.setStatus(ModelPortfolioStatus.ACTIVE);

        portfolioResponse = new ModelPortfolioResponse();
        portfolioResponse.setModelId(1L);
        portfolioResponse.setName("Balanced Growth Portfolio");
        portfolioResponse.setRiskClass(RiskClass.BALANCED);
        portfolioResponse.setWeightsJson(
                "{\"Equity\": 60, \"Bond\": 30, \"Derivatives\": 10}");
        portfolioResponse.setStatus(ModelPortfolioStatus.ACTIVE);
    }

    @Test
    void createModelPortfolio() throws Exception {
        when(modelPortfolioService.createModelPortfolio(any(ModelPortfolioRequest.class))).thenReturn(portfolioResponse);
        mockMvc.perform(post("/api/model-portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(portfolioRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.modelId").value(1))
                .andExpect(jsonPath("$.name").value(
                        "Balanced Growth Portfolio"))
                .andExpect(jsonPath("$.riskClass").value("BALANCED"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
//negative when name is missing
    @Test
    void createModelPortfolio_negative() throws Exception {
        portfolioRequest.setName(null);

        mockMvc.perform(post("/api/model-portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                portfolioRequest)))
                .andExpect(status().isBadRequest());
    }
//negative testcase when name is already found
    @Test
    void createModelPortfolio_name_alreadyExists() throws Exception {
        when(modelPortfolioService.createModelPortfolio(
                any(ModelPortfolioRequest.class)))
                .thenThrow(new IllegalArgumentException(
                        "Model portfolio already exists with name: "
                                + "Balanced Growth Portfolio"));

        mockMvc.perform(post("/api/model-portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                portfolioRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getModelPortfolioById() throws Exception {
        when(modelPortfolioService.getModelPortfolioById(1L))
                .thenReturn(portfolioResponse);

        mockMvc.perform(get("/api/model-portfolios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelId").value(1))
                .andExpect(jsonPath("$.riskClass").value("BALANCED"));
    }

    @Test
    void getModelPortfolioById_negative() throws Exception {
        when(modelPortfolioService.getModelPortfolioById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "Model portfolio not found with id: 99"));

        mockMvc.perform(get("/api/model-portfolios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteModelPortfolio() throws Exception {
        doNothing().when(modelPortfolioService).deleteModelPortfolio(1L);

        mockMvc.perform(delete("/api/model-portfolios/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Model portfolio deleted successfully with id: 1"));
    }

    @Test
    void deleteModelPortfolio_negative() throws Exception {
        doThrow(new ResourceNotFoundException(
                "Model portfolio not found with id: 99"))
                .when(modelPortfolioService).deleteModelPortfolio(99L);

        mockMvc.perform(delete("/api/model-portfolios/99"))
                .andExpect(status().isNotFound());
    }
    @Test
    void getAllModelPortfolios_success() throws Exception {
        when(modelPortfolioService.getAllModelPortfolios())
                .thenReturn(java.util.List.of(portfolioResponse));

        mockMvc.perform(get("/api/model-portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Balanced Growth Portfolio"));
    }

    @Test
    void getModelPortfoliosByRiskClass_success() throws Exception {
        when(modelPortfolioService.getModelPortfoliosByRiskClass(RiskClass.BALANCED))
                .thenReturn(java.util.List.of(portfolioResponse));

        mockMvc.perform(get("/api/model-portfolios/risk-class/BALANCED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].riskClass").value("BALANCED"));
    }

    @Test
    void getModelPortfoliosByStatus_success() throws Exception {
        when(modelPortfolioService.getModelPortfoliosByStatus(ModelPortfolioStatus.ACTIVE))
                .thenReturn(java.util.List.of(portfolioResponse));

        mockMvc.perform(get("/api/model-portfolios/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void updateModelPortfolio_success() throws Exception {
        when(modelPortfolioService.updateModelPortfolio(eq(1L), any(ModelPortfolioRequest.class)))
                .thenReturn(portfolioResponse);

        mockMvc.perform(put("/api/model-portfolios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(portfolioRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelId").value(1))
                .andExpect(jsonPath("$.name").value("Balanced Growth Portfolio"));
    }

    @Test
    void updateModelPortfolio_notFound() throws Exception {
        when(modelPortfolioService.updateModelPortfolio(eq(99L), any(ModelPortfolioRequest.class)))
                .thenThrow(new ResourceNotFoundException("Model portfolio not found with id: 99"));

        mockMvc.perform(put("/api/model-portfolios/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(portfolioRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getModelPortfoliosByStatus_invalidEnum() throws Exception {
        // Testing that an invalid status string returns a 400 Bad Request
        mockMvc.perform(get("/api/model-portfolios/status/NOT_A_STATUS"))
                .andExpect(status().isBadRequest());
    }
}