package com.wealthpro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthpro.dto.request.KYCStatusUpdateRequestDTO;
import com.wealthpro.dto.response.KYCDocumentResponseDTO;
import com.wealthpro.enums.KycStatus;
import com.wealthpro.exception.GlobalExceptionHandler;
import com.wealthpro.exception.InvalidOperationException;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.service.KYCService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KYCController.class)
@Import(GlobalExceptionHandler.class)
public class KYCControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KYCService kycService;

    @Autowired
    private ObjectMapper objectMapper;

    private KYCDocumentResponseDTO responseDTO;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        responseDTO = new KYCDocumentResponseDTO();
        responseDTO.setKycId(1L);
        responseDTO.setClientId(1L);
        responseDTO.setDocumentType("PAN");
        responseDTO.setDocumentRef("http://cloudinary.com/pan.jpg");
        responseDTO.setStatus(KycStatus.Pending);

        // Fake image file for multipart upload test
        mockFile = new MockMultipartFile(
                "document",
                "pan.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );
    }

    // TEST 1: POST /api/clients/{clientId}/kyc — 201 Created
    @Test
    void testAddKYCDocument_Returns201() throws Exception {
        when(kycService.addKYCDocument(eq(1L), eq("PAN"), any()))
                .thenReturn(responseDTO);

        // multipart() is used for form-data requests
        mockMvc.perform(multipart("/api/clients/1/kyc")
                        .file(mockFile)
                        .param("documentType", "PAN"))
                .andExpect(status().isCreated())                    // 201
                .andExpect(jsonPath("$.kycId").value(1))
                .andExpect(jsonPath("$.documentType").value("PAN"))
                .andExpect(jsonPath("$.status").value("Pending"));

        verify(kycService, times(1))
                .addKYCDocument(eq(1L), eq("PAN"), any());
    }

    // TEST 2: POST /api/clients/{clientId}/kyc — 404 Client not found
    @Test
    void testAddKYCDocument_ClientNotFound_Returns404() throws Exception {
        when(kycService.addKYCDocument(eq(999L), eq("PAN"), any()))
                .thenThrow(new ResourceNotFoundException(
                        "Client not found with ID: 999"));

        mockMvc.perform(multipart("/api/clients/999/kyc")
                        .file(mockFile)
                        .param("documentType", "PAN"))
                .andExpect(status().isNotFound())                   // 404
                .andExpect(jsonPath("$.message")
                        .value("Client not found with ID: 999"));
    }


    // TEST 3: GET /api/clients/{clientId}/kyc — 200 OK

    @Test
    void testGetKYCDocumentsByClient_Returns200() throws Exception {
        // Arrange
        when(kycService.getKYCDocumentsByClient(1L))
                .thenReturn(List.of(responseDTO));

        // Act + Assert
        mockMvc.perform(get("/api/clients/1/kyc"))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].documentType").value("PAN"));

        verify(kycService, times(1)).getKYCDocumentsByClient(1L);
    }


    // TEST 4: GET /api/clients/kyc/{kycId} — 200 OK

    @Test
    void testGetKYCDocumentById_Returns200() throws Exception {
        when(kycService.getKYCDocumentById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/clients/kyc/1"))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.kycId").value(1))
                .andExpect(jsonPath("$.status").value("Pending"));
    }












    // ─────────────────────────────────────────
    // TEST 5: PUT /api/clients/kyc/{kycId}/status — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testUpdateKYCStatus_Returns200() throws Exception {
        // Arrange
        KYCStatusUpdateRequestDTO statusRequest = new KYCStatusUpdateRequestDTO();
        statusRequest.setStatus(KycStatus.Verified);

        responseDTO.setStatus(KycStatus.Verified);
        responseDTO.setVerifiedDate(LocalDate.now());

        when(kycService.updateKYCStatus(eq(1L), any(KYCStatusUpdateRequestDTO.class)))
                .thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(put("/api/clients/kyc/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())                         // 200
                .andExpect(jsonPath("$.status").value("Verified"));
    }

    // ─────────────────────────────────────────
    // TEST 6: PUT /api/clients/kyc/{kycId}/status
    //         Verified → Pending — 400 Bad Request
    // ─────────────────────────────────────────
    @Test
    void testUpdateKYCStatus_InvalidTransition_Returns400() throws Exception {
        // Arrange
        KYCStatusUpdateRequestDTO statusRequest = new KYCStatusUpdateRequestDTO();
        statusRequest.setStatus(KycStatus.Pending);

        when(kycService.updateKYCStatus(eq(1L), any(KYCStatusUpdateRequestDTO.class)))
                .thenThrow(new InvalidOperationException(
                        "Cannot revert KYC status from Verified back to Pending"));

        // Act + Assert
        mockMvc.perform(put("/api/clients/kyc/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isBadRequest())                 // 400
                .andExpect(jsonPath("$.message")
                        .value("Cannot revert KYC status from Verified back to Pending"));
    }

    // ─────────────────────────────────────────
    // TEST 7: DELETE /api/clients/kyc/{kycId} — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testDeleteKYCDocument_Returns200() throws Exception {
        // Arrange
        doNothing().when(kycService).deleteKYCDocument(1L);

        // Act + Assert
        mockMvc.perform(delete("/api/clients/kyc/1"))
                .andExpect(status().isOk())                         // 200
                .andExpect(content().string("KYC document deleted successfully"));

        verify(kycService, times(1)).deleteKYCDocument(1L);
    }

    // ─────────────────────────────────────────
    // TEST 8: DELETE /api/clients/kyc/{kycId} — 404 Not Found
    // ─────────────────────────────────────────
    @Test
    void testDeleteKYCDocument_NotFound_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("KYC Document not found with ID: 999"))
                .when(kycService).deleteKYCDocument(999L);

        // Act + Assert
        mockMvc.perform(delete("/api/clients/kyc/999"))
                .andExpect(status().isNotFound())                   // 404
                .andExpect(jsonPath("$.message")
                        .value("KYC Document not found with ID: 999"));
    }



}