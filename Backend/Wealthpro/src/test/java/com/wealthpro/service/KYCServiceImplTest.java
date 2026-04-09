package com.wealthpro.service;

import com.wealthpro.dto.request.KYCStatusUpdateRequestDTO;
import com.wealthpro.dto.response.KYCDocumentResponseDTO;
import com.wealthpro.entities.Client;
import com.wealthpro.entities.KYCDocument;
import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
import com.wealthpro.enums.KycStatus;
import com.wealthpro.exception.InvalidOperationException;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.repositories.KYCDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KYCServiceImplTest {

    @Mock
    private KYCDocumentRepository kycDocumentRepository;

    @Mock
    private ClientService clientService;        // interface — Spring injects impl

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private KYCServiceImpl kycService;

    private Client client;
    private KYCDocument kycDocument;
    private KYCDocumentResponseDTO responseDTO;

    // MockMultipartFile = fake image file for testing
    // We don't need a real image — just a fake one
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId(1L);
        client.setName("Murali Krishna");
        client.setDob(LocalDate.of(1995, 6, 15));
        client.setContactInfo("murali@gmail.com");
        client.setSegment(ClientSegment.HNI);
        client.setStatus(ClientStatus.Active);

        kycDocument = new KYCDocument();
        kycDocument.setKycId(1L);
        kycDocument.setClient(client);
        kycDocument.setDocumentType("PAN");
        kycDocument.setDocumentRef("http://cloudinary.com/pan.jpg");
        kycDocument.setStatus(KycStatus.Pending);

        responseDTO = new KYCDocumentResponseDTO();
        responseDTO.setKycId(1L);
        responseDTO.setClientId(1L);
        responseDTO.setDocumentType("PAN");
        responseDTO.setDocumentRef("http://cloudinary.com/pan.jpg");
        responseDTO.setStatus(KycStatus.Pending);

        // MockMultipartFile(fieldName, originalFileName, contentType, content)
        mockFile = new MockMultipartFile(
                "document",
                "pan.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );
    }

    // ─────────────────────────────────────────
    // TEST 1: Add KYC document — success
    // ─────────────────────────────────────────
    @Test
    void testAddKYCDocument_Success() {
        // Arrange
        when(clientService.findClientOrThrow(1L)).thenReturn(client);

        // Change this
        // when(cloudinaryService.uploadKYCDocument(mockFile, 1L, "PAN"))
        //         .thenReturn("http://cloudinary.com/pan.jpg");

        // To this
        when(fileStorageService.saveFile(mockFile, 1L, "PAN"))
                .thenReturn("C:/wealthpro/kyc-documents/client_1/PAN_123.jpg");

        when(kycDocumentRepository.save(any(KYCDocument.class))).thenReturn(kycDocument);

        // Act
        KYCDocumentResponseDTO result = kycService.addKYCDocument(1L, "PAN", mockFile);

        // Assert
        assertNotNull(result);
        assertEquals("PAN", result.getDocumentType());
        assertEquals(KycStatus.Pending, result.getStatus());

        // Verify fileStorageService was called instead of cloudinaryService
        verify(fileStorageService, times(1)).saveFile(mockFile, 1L, "PAN");
        verify(kycDocumentRepository, times(1)).save(any(KYCDocument.class));
    }

    // ─────────────────────────────────────────
    // TEST 2: Add KYC document — client not found → exception
    // ─────────────────────────────────────────
    @Test
    void testAddKYCDocument_ClientNotFound_ThrowsException() {
        // Arrange
        when(clientService.findClientOrThrow(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Client not found with ID: 999"));

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            kycService.addKYCDocument(999L, "PAN", mockFile);
        });

        // Change this
        // verify(cloudinaryService, never()).uploadKYCDocument(any(), any(), any());

        // To this
        verify(fileStorageService, never()).saveFile(any(), any(), any());
    }


    // ─────────────────────────────────────────
    // TEST 3: Get all KYC documents for client
    // ─────────────────────────────────────────
    @Test
    void testGetKYCDocumentsByClient_ReturnsList() {
        // Arrange
        when(clientService.findClientOrThrow(1L)).thenReturn(client);
        when(kycDocumentRepository.findByClientClientId(1L))
                .thenReturn(List.of(kycDocument));

        // Act
        List<KYCDocumentResponseDTO> result =
                kycService.getKYCDocumentsByClient(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(kycDocumentRepository, times(1)).findByClientClientId(1L);
    }

  @Test
  void mpTest(){
     //   when(kycService.findKYCOrThrow(1L)).thenReturn(kycDocument);

      when(kycDocumentRepository.findById(1L)).thenReturn(Optional.of(kycDocument));
      KYCDocumentResponseDTO res=kycService.getKYCDocumentById(1L);
      assertNotNull(res);

  }



    // ─────────────────────────────────────────
    // TEST 4: Update KYC status — Pending to Verified
    // ─────────────────────────────────────────
    @Test
    void testUpdateKYCStatus_PendingToVerified_Success() {
        // Arrange
        KYCStatusUpdateRequestDTO statusRequest = new KYCStatusUpdateRequestDTO();
        statusRequest.setStatus(KycStatus.Verified);

        when(kycDocumentRepository.findById(1L))
                .thenReturn(Optional.of(kycDocument));
        when(kycDocumentRepository.save(any(KYCDocument.class)))
                .thenReturn(kycDocument);

        // Act
        KYCDocumentResponseDTO result =
                kycService.updateKYCStatus(1L, statusRequest);

        // Assert
        assertNotNull(result);
        // Verify save was called after status update
        verify(kycDocumentRepository, times(1)).save(any(KYCDocument.class));
    }

    // ─────────────────────────────────────────
    // TEST 5: Update KYC status — Verified to Pending → exception
    // Business rule: cannot revert Verified back to Pending
    // ─────────────────────────────────────────
    @Test
    void testUpdateKYCStatus_VerifiedToPending_ThrowsException() {
        // Arrange — set current status to Verified
        kycDocument.setStatus(KycStatus.Verified);

        KYCStatusUpdateRequestDTO statusRequest = new KYCStatusUpdateRequestDTO();
        statusRequest.setStatus(KycStatus.Pending); // trying to revert

        when(kycDocumentRepository.findById(1L))
                .thenReturn(Optional.of(kycDocument));

        // Act + Assert
        assertThrows(InvalidOperationException.class, () -> {
            kycService.updateKYCStatus(1L, statusRequest);
        });

        // Verify save was NEVER called since rule was violated
        verify(kycDocumentRepository, never()).save(any());
    }

    // ─────────────────────────────────────────
    // TEST 6: Update KYC status — Expired document → exception
    // Business rule: cannot update an Expired document
    // ─────────────────────────────────────────
    @Test
    void testUpdateKYCStatus_ExpiredDocument_ThrowsException() {
        // Arrange — set current status to Expired
        kycDocument.setStatus(KycStatus.Expired);

        KYCStatusUpdateRequestDTO statusRequest = new KYCStatusUpdateRequestDTO();
        statusRequest.setStatus(KycStatus.Verified);

        when(kycDocumentRepository.findById(1L))
                .thenReturn(Optional.of(kycDocument));

        // Act + Assert
        assertThrows(InvalidOperationException.class, () -> {
            kycService.updateKYCStatus(1L, statusRequest);
        });
    }

    // ─────────────────────────────────────────
    // TEST 7: Delete KYC document — success
    // ─────────────────────────────────────────
    @Test
    void testDeleteKYCDocument_Success() {
        // Arrange
        when(kycDocumentRepository.findById(1L))
                .thenReturn(Optional.of(kycDocument));

        // Act
        kycService.deleteKYCDocument(1L);

        // Assert
        verify(kycDocumentRepository, times(1)).deleteById(1L);
    }

    // ─────────────────────────────────────────
    // TEST 8: Delete KYC document — not found → exception
    // ─────────────────────────────────────────
    @Test
    void testDeleteKYCDocument_NotFound_ThrowsException() {
        // Arrange
        when(kycDocumentRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            kycService.deleteKYCDocument(999L);
        });

        verify(kycDocumentRepository, never()).deleteById(any());
    }
}