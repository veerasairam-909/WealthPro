package com.wealth.pbor.controllertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.pbor.controller.AccountController;
import com.wealth.pbor.controller.impl.AccountControllerImpl;
import com.wealth.pbor.dto.request.AccountRequest;
import com.wealth.pbor.dto.response.AccountResponse;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountControllerImpl.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountRequest request;
    private AccountResponse response;

    @BeforeEach
    void setUp() {
        request = new AccountRequest();
        request.setClientId(1L);
        request.setAccountType(AccountType.INDIVIDUAL);
        request.setBaseCurrency("INR");
        request.setStatus(AccountStatus.ACTIVE);

        response = new AccountResponse();
        response.setAccountId(1L);
        response.setClientId(1L);
        response.setAccountType(AccountType.INDIVIDUAL);
        response.setBaseCurrency("INR");
        response.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        when(accountService.createAccount(any(AccountRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(1L))
                .andExpect(jsonPath("$.clientId").value(1L))
                .andExpect(jsonPath("$.accountType").value("INDIVIDUAL"));
    }

    @Test
    void testCreateAccount_ValidationFails() throws Exception {
        AccountRequest invalidRequest = new AccountRequest();

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAccountById_Success() throws Exception {
        when(accountService.getAccountById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1L));
    }

    @Test
    void testGetAccountById_NotFound() throws Exception {
        when(accountService.getAccountById(99L)).thenThrow(new ResourceNotFoundException("Account not found with id: 99"));

        mockMvc.perform(get("/api/accounts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllAccounts() throws Exception {
        when(accountService.getAllAccounts()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetAccountsByClientId() throws Exception {
        when(accountService.getAccountsByClientId(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/accounts/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetAccountsByStatus() throws Exception {
        when(accountService.getAccountsByStatus(AccountStatus.ACTIVE)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/accounts/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testUpdateAccount_Success() throws Exception {
        when(accountService.updateAccount(eq(1L), any(AccountRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1L));
    }

    @Test
    void testDeleteAccount_Success() throws Exception {
        doNothing().when(accountService).deleteAccount(1L);

        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account deleted successfully."));
    }
}