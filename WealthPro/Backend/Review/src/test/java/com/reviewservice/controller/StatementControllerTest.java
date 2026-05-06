package com.reviewservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reviewservice.dto.request.StatementRequest;
import com.reviewservice.dto.response.StatementResponse;
import com.reviewservice.enums.PeriodType;
import com.reviewservice.enums.StatementStatus;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.security.OwnershipGuard;
import com.reviewservice.service.StatementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatementControllerImpl.class)
class StatementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatementService statementService;

    @MockitoBean
    private OwnershipGuard ownershipGuard;

    private ObjectMapper objectMapper;
    private StatementRequest statementRequest;
    private StatementResponse statementResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        statementRequest = StatementRequest.builder()
                .accountId(201L)
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 1, 31))
                .periodType(PeriodType.MONTHLY)
                .generatedDate(LocalDate.of(2024, 2, 1))
                .summaryJson("{\"totalValue\": \"100000\"}")
                .status(StatementStatus.GENERATED)
                .build();

        statementResponse = StatementResponse.builder()
                .statementId(1L)
                .accountId(201L)
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 1, 31))
                .periodType(PeriodType.MONTHLY)
                .generatedDate(LocalDate.of(2024, 2, 1))
                .summaryJson("{\"totalValue\": \"100000\"}")
                .status(StatementStatus.GENERATED)
                .build();
    }

    @Test
    @DisplayName("POST /api/statements - Should create statement and return 201")
    void testCreateStatement() throws Exception {
        when(statementService.createStatement(any(StatementRequest.class))).thenReturn(statementResponse);

        mockMvc.perform(post("/api/statements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statementRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statementId").value(1L))
                .andExpect(jsonPath("$.accountId").value(201L))
                .andExpect(jsonPath("$.status").value("GENERATED"));
    }

    @Test
    @DisplayName("GET /api/statements/{id} - Should return statement by ID")
    void testGetStatementById() throws Exception {
        when(statementService.getStatementById(1L)).thenReturn(statementResponse);

        mockMvc.perform(get("/api/statements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statementId").value(1L))
                .andExpect(jsonPath("$.accountId").value(201L));
    }

    @Test
    @DisplayName("GET /api/statements/{id} - Should return 404 when not found")
    void testGetStatementById_NotFound() throws Exception {
        when(statementService.getStatementById(99L))
                .thenThrow(new ResourceNotFoundException("Statement", 99L));

        mockMvc.perform(get("/api/statements/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/statements - Should return all statements")
    void testGetAllStatements() throws Exception {
        when(statementService.getAllStatements()).thenReturn(List.of(statementResponse));

        mockMvc.perform(get("/api/statements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/statements/{id} - Should update statement and return 200")
    void testUpdateStatement() throws Exception {
        when(statementService.updateStatement(eq(1L), any(StatementRequest.class))).thenReturn(statementResponse);

        mockMvc.perform(put("/api/statements/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statementRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statementId").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/statements/{id} - Should delete statement and return 204")
    void testDeleteStatement() throws Exception {
        doNothing().when(statementService).deleteStatement(1L);

        mockMvc.perform(delete("/api/statements/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/statements/{id} - Should return 404 when not found")
    void testDeleteStatement_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Statement", 99L))
                .when(statementService).deleteStatement(99L);

        mockMvc.perform(delete("/api/statements/99"))
                .andExpect(status().isNotFound());
    }
}
