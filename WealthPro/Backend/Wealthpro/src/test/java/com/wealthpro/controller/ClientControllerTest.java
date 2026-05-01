package com.wealthpro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthpro.dto.request.ClientRequestDTO;
import com.wealthpro.dto.response.ClientResponseDTO;
import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
import com.wealthpro.exception.GlobalExceptionHandler;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import(GlobalExceptionHandler.class)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;            // simulates HTTP requests

    @MockBean
    private ClientService clientService; // fake service

    @Autowired
    private ObjectMapper objectMapper;   // converts object to JSON string

    private ClientRequestDTO requestDTO;
    private ClientResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new ClientRequestDTO();
        requestDTO.setName("Murali Krishna");
        requestDTO.setDob(LocalDate.of(1995, 6, 15));
        requestDTO.setContactInfo("murali@gmail.com");
        requestDTO.setSegment(ClientSegment.HNI);
        requestDTO.setStatus(ClientStatus.Active);

        responseDTO = new ClientResponseDTO();
        responseDTO.setClientId(1L);
        responseDTO.setName("Murali Krishna");
        responseDTO.setDob(LocalDate.of(1995, 6, 15));
        responseDTO.setContactInfo("murali@gmail.com");
        responseDTO.setSegment(ClientSegment.HNI);
        responseDTO.setStatus(ClientStatus.Active);
    }


    // TEST 1: POST /api/clients — 201 Created
    @Test
    void testCreateClient_Returns201() throws Exception {
        when(clientService.createClient(any(ClientRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())                    // 201
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.name").value("Murali Krishna"))
                .andExpect(jsonPath("$.segment").value("HNI"))
                .andExpect(jsonPath("$.status").value("Active"));

        verify(clientService, times(1))
                .createClient(any(ClientRequestDTO.class));
    }

    // TEST 2: POST /api/clients — 400 Validation error
    @Test
    void testCreateClient_InvalidBody_Returns400() throws Exception {
        // Arrange — empty request body (missing required fields)
        ClientRequestDTO emptyRequest = new ClientRequestDTO();

        // Act + Assert
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());                // 400
    }

    // TEST 3: GET /api/clients — 200 OK

    @Test
    void testGetAllClients_Returns200() throws Exception {
        // Arrange
        when(clientService.getAllClients()).thenReturn(List.of(responseDTO));

        // Act + Assert
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Murali Krishna"));

        verify(clientService, times(1)).getAllClients();
    }


    // TEST 4: GET /api/clients/{clientId} — 200 OK

    @Test
    void testGetClientById_Returns200() throws Exception {

        when(clientService.getClientById(1L)).thenReturn(responseDTO);


        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.name").value("Murali Krishna"));

        verify(clientService, times(1)).getClientById(1L);
    }


    // TEST 5: GET /api/clients/{clientId} — 404 Not Found

    @Test
    void testGetClientById_NotFound_Returns404() throws Exception {
        when(clientService.getClientById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Client not found with ID: 999"));

        mockMvc.perform(get("/api/clients/999"))
                .andExpect(status().isNotFound())                   // 404
                .andExpect(jsonPath("$.message")
                        .value("Client not found with ID: 999"));
    }

    // TEST 6: PUT /api/clients/{clientId} — 200 OK
    @Test
    void testUpdateClient_Returns200() throws Exception {
        when(clientService.updateClient(eq(1L), any(ClientRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.name").value("Murali Krishna"));

        verify(clientService, times(1))
                .updateClient(eq(1L), any(ClientRequestDTO.class));
    }

    // TEST 7: DELETE /api/clients/{clientId} — 200 OK
    @Test
    void testDeleteClient_Returns200() throws Exception {
        // Arrange
        doNothing().when(clientService).deleteClient(1L);

        // Act + Assert
        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isOk())                         // 200
                .andExpect(content().string("Client deleted successfully"));

        verify(clientService, times(1)).deleteClient(1L);
    }

    // TEST 8: DELETE /api/clients/{clientId} — 404 Not Found
    @Test
    void testDeleteClient_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Client not found with ID: 999"))
                .when(clientService).deleteClient(999L);

        mockMvc.perform(delete("/api/clients/999"))
                .andExpect(status().isNotFound())                   // 404
                .andExpect(jsonPath("$.message")
                        .value("Client not found with ID: 999"));
    }
}