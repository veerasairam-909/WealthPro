package com.wealth.pbor.controllertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealth.pbor.controller.impl.CashLedgerControllerImpl;
import com.wealth.pbor.dto.request.CashLedgerRequest;
import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.enums.TxnType;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.security.OwnershipGuard;
import com.wealth.pbor.service.CashLedgerService;
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

@WebMvcTest(CashLedgerControllerImpl.class)
class CashLedgerControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CashLedgerService cashLedgerService;

    @MockBean
    private OwnershipGuard ownershipGuard;

    private ObjectMapper objectMapper;
    private CashLedgerRequest request;
    private CashLedgerResponse response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        request = new CashLedgerRequest();
        request.setAccountId(1L);
        request.setTxnType(TxnType.SUBSCRIPTION);
        request.setAmount(new BigDecimal("5000.00"));
        request.setCurrency("INR");
        request.setTxnDate(LocalDate.now().minusDays(1));
        request.setNarrative("Test subscription");

        response = new CashLedgerResponse();
        response.setLedgerId(1L);
        response.setAccountId(1L);
        response.setTxnType(TxnType.SUBSCRIPTION);
        response.setAmount(new BigDecimal("5000.00"));
        response.setCurrency("INR");
        response.setTxnDate(LocalDate.now().minusDays(1));
        response.setNarrative("Test subscription");
    }

    @Test
    void testCreateCashLedgerEntry_Success() throws Exception {
        when(cashLedgerService.createCashLedgerEntry(any(CashLedgerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/cash-ledger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ledgerId").value(1L))
                .andExpect(jsonPath("$.txnType").value("SUBSCRIPTION"));
    }

    @Test
    void testCreateCashLedgerEntry_ValidationFails() throws Exception {
        CashLedgerRequest invalidRequest = new CashLedgerRequest();

        mockMvc.perform(post("/api/cash-ledger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCashLedgerEntryById_Success() throws Exception {
        when(cashLedgerService.getCashLedgerEntryById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/cash-ledger/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ledgerId").value(1L));
    }

    @Test
    void testGetCashLedgerEntryById_NotFound() throws Exception {
        when(cashLedgerService.getCashLedgerEntryById(99L)).thenThrow(new ResourceNotFoundException("Cash ledger entry not found with id: 99"));

        mockMvc.perform(get("/api/cash-ledger/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllCashLedgerEntries() throws Exception {
        when(cashLedgerService.getAllCashLedgerEntries()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/cash-ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetCashLedgerEntriesByAccountId() throws Exception {
        when(cashLedgerService.getCashLedgerEntriesByAccountId(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/cash-ledger/account/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetCashLedgerEntriesByAccountIdAndTxnType() throws Exception {
        when(cashLedgerService.getCashLedgerEntriesByAccountIdAndTxnType(1L, TxnType.SUBSCRIPTION))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/cash-ledger/account/1/txn-type/SUBSCRIPTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetCashLedgerEntriesByDateRange() throws Exception {
        when(cashLedgerService.getCashLedgerEntriesByAccountIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/cash-ledger/account/1/date-range")
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testUpdateCashLedgerEntry_Success() throws Exception {
        when(cashLedgerService.updateCashLedgerEntry(eq(1L), any(CashLedgerRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/cash-ledger/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ledgerId").value(1L));
    }

    @Test
    void testDeleteCashLedgerEntry_Success() throws Exception {
        doNothing().when(cashLedgerService).deleteCashLedgerEntry(1L);

        mockMvc.perform(delete("/api/cash-ledger/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cash ledger entry deleted successfully."));
    }
}