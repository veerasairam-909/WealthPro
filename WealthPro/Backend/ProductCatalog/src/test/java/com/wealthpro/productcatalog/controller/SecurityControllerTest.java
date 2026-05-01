package com.wealthpro.productcatalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthpro.productcatalog.controller.SecurityControllerImpl;
import com.wealthpro.productcatalog.dto.request.SecurityRequest;
import com.wealthpro.productcatalog.dto.response.SecurityResponse;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import com.wealthpro.productcatalog.exception.DuplicateResourceException;
import com.wealthpro.productcatalog.exception.GlobalExceptionHandler;
import com.wealthpro.productcatalog.exception.ResourceNotFoundException;
import com.wealthpro.productcatalog.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private SecurityControllerImpl securityController;

    private SecurityRequest request;
    private SecurityResponse response;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(securityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        request = new SecurityRequest();
        request.setSymbol("AAPL");
        request.setAssetClass(AssetClass.EQUITY);
        request.setCurrency("USD");
        request.setCountry("USA");
        request.setStatus(SecurityStatus.ACTIVE);

        response = new SecurityResponse();
        response.setSecurityId(1L);
        response.setSymbol("AAPL");
        response.setAssetClass(AssetClass.EQUITY);
        response.setCurrency("USD");
        response.setCountry("USA");
        response.setStatus(SecurityStatus.ACTIVE);
    }

    @Test
    void shouldCreateSecurityAndReturn201() throws Exception {
        when(securityService.createSecurity(any(SecurityRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/securities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.securityId").value(1))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.assetClass").value("EQUITY"));
    }

    @Test
    void shouldReturn409WhenDuplicateSymbol() throws Exception {
        when(securityService.createSecurity(any(SecurityRequest.class)))
                .thenThrow(new DuplicateResourceException("Security already exists with symbol: AAPL"));

        mockMvc.perform(post("/api/securities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturn400WhenRequestBodyInvalid() throws Exception {
        SecurityRequest invalid = new SecurityRequest();

        mockMvc.perform(post("/api/securities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldGetSecurityByIdAndReturn200() throws Exception {
        when(securityService.getSecurityById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/securities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.securityId").value(1))
                .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    void shouldReturn404WhenSecurityIdNotFound() throws Exception {
        when(securityService.getSecurityById(99L))
                .thenThrow(new ResourceNotFoundException("Security not found with id: 99"));

        mockMvc.perform(get("/api/securities/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldGetSecurityBySymbolAndReturn200() throws Exception {
        when(securityService.getSecurityBySymbol("AAPL")).thenReturn(response);

        mockMvc.perform(get("/api/securities/symbol/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    void shouldGetAllSecuritiesAndReturn200() throws Exception {
        when(securityService.getAllSecurities()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/securities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetSecuritiesByAssetClass() throws Exception {
        when(securityService.getSecuritiesByAssetClass(AssetClass.EQUITY))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/securities/asset-class/EQUITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetSecuritiesByStatus() throws Exception {
        when(securityService.getSecuritiesByStatus(SecurityStatus.ACTIVE))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/securities/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldUpdateSecurityAndReturn200() throws Exception {
        when(securityService.updateSecurity(eq(1L), any(SecurityRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/securities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    void shouldDeleteSecurityAndReturn204() throws Exception {
        doNothing().when(securityService).deleteSecurity(1L);

        mockMvc.perform(delete("/api/securities/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentSecurity() throws Exception {
        doThrow(new ResourceNotFoundException("Security not found with id: 99"))
                .when(securityService).deleteSecurity(99L);

        mockMvc.perform(delete("/api/securities/99"))
                .andExpect(status().isNotFound());
    }
}