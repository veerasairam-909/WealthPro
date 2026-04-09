package com.wealthpro.service;

import com.wealthpro.dto.request.KYCStatusUpdateRequestDTO;
import com.wealthpro.dto.response.KYCDocumentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KYCService {

    KYCDocumentResponseDTO addKYCDocument(Long clientId,
                                          String documentType,
                                          MultipartFile document);

    List<KYCDocumentResponseDTO> getKYCDocumentsByClient(Long clientId);

    KYCDocumentResponseDTO getKYCDocumentById(Long kycId);

    KYCDocumentResponseDTO updateKYCStatus(Long kycId,
                                           KYCStatusUpdateRequestDTO requestDTO);

    void deleteKYCDocument(Long kycId);
}