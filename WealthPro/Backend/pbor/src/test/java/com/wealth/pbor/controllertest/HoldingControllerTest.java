package com.wealth.pbor.controllertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealth.pbor.controller.HoldingController;
import com.wealth.pbor.controller.impl.HoldingControllerImpl;
import com.wealth.pbor.dto.request.HoldingRequest;
import com.wealth.pbor.dto.response.HoldingResponse;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.security.OwnershipGuard;
import com.wealth.pbor.service.HoldingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HoldingControllerImpl.class)
class HoldingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HoldingService holdingService;

    @MockBean
    private OwnershipGuard ownershipGuard;

    private ObjectMapper objectMapper;
    private HoldingRequest request;
    private HoldingResponse response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        request = new HoldingRequest();
        request.setAccountId(1L);
        request.setSecurityId(101L);
        request.setQuantity(new BigDecimal("100.0000"));
        request.setAvgCost(new BigDecimal("250.0000"));
        request.setValuationCurrency("INR");
        request.setLastValuationDate(LocalDate.now());

        response = new HoldingResponse();
        response.setHoldingId(1L);
        response.setAccountId(1L);
        response.setSecurityId(101L);
        response.setQuantity(new BigDecimal("100.0000"));
        response.setAvgCost(new BigDecimal("250.0000"));
        response.setValuationCurrency("INR");
        response.setLastValuationDate(LocalDate.now());
    }

    @Test
    void testCreateHolding_Success() throws Exception {
        when(holdingService.createHolding(any(HoldingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/holdings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.holdingId").value(1L))
                .andExpect(jsonPath("$.securityId").value(101L));
    }

    @Test
    void testCreateHolding_ValidationFails() throws Exception {
        HoldingRequest invalidRequest = new HoldingRequest();

        mockMvc.perform(post("/api/holdings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetHoldingById_Success() throws Exception {
        when(holdingService.getHoldingById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/holdings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdingId").value(1L));
    }

    @Test
    void testGetHoldingById_NotFound() throws Exception {
        when(holdingService.getHoldingById(99L)).thenThrow(new ResourceNotFoundException("Holding not found with id: 99"));

        mockMvc.perform(get("/api/holdings/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllHoldings() throws Exception {
        when(holdingService.getAllHoldings()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/holdings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetHoldingsByAccountId() throws Exception {
        when(holdingService.getHoldingsByAccountId(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/holdings/account/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetHoldingsBySecurityId() throws Exception {
        when(holdingService.getHoldingsBySecurityId(101L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/holdings/security/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testUpdateHolding_Success() throws Exception {
        when(holdingService.updateHolding(eq(1L), any(HoldingRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdingId").value(1L));
    }

    @Test
    void testDeleteHolding_Success() throws Exception {
        doNothing().when(holdingService).deleteHolding(1L);

        mockMvc.perform(delete("/api/holdings/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Holding deleted successfully."));
    }
}