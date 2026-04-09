package com.wealthpro.controller;

import com.wealthpro.dto.request.KYCStatusUpdateRequestDTO;
import com.wealthpro.dto.response.KYCDocumentResponseDTO;
import com.wealthpro.service.KYCService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class KYCController {

    private final KYCService kycService;

    public KYCController(KYCService kycService) {
        this.kycService = kycService;
    }


    // POST /api/clients/{clientId}/kyc
    // Upload a KYC document image for a client

    @PostMapping(value = "/{clientId}/kyc",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KYCDocumentResponseDTO> addKYCDocument(
            @PathVariable Long clientId,
            @RequestParam("documentType") String documentType,
            @RequestParam("document") MultipartFile document) {

        KYCDocumentResponseDTO response = kycService.addKYCDocument(
                clientId, documentType, document);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
    }

    // GET /api/clients/{clientId}/kyc
    // Get all KYC documents for a client

    @GetMapping("/{clientId}/kyc")
    public ResponseEntity<List<KYCDocumentResponseDTO>> getKYCDocumentsByClient(
            @PathVariable Long clientId) {

        return ResponseEntity.ok(kycService.getKYCDocumentsByClient(clientId)); // 200
    }


    // GET /api/kyc/{kycId}
    // Get a single KYC document by its own ID

    @GetMapping("/kyc/{kycId}")
    public ResponseEntity<KYCDocumentResponseDTO> getKYCDocumentById(
            @PathVariable Long kycId) {

        return ResponseEntity.ok(kycService.getKYCDocumentById(kycId)); // 200
    }


    // PUT /api/kyc/{kycId}/status
    // Update KYC status (Pending → Verified / Expired)

    @PutMapping("/kyc/{kycId}/status")
    public ResponseEntity<KYCDocumentResponseDTO> updateKYCStatus(
            @PathVariable Long kycId,
            @Valid @RequestBody KYCStatusUpdateRequestDTO requestDTO) {

        return ResponseEntity.ok(kycService.updateKYCStatus(kycId, requestDTO)); // 200
    }


    // DELETE /api/kyc/{kycId}
    // Delete a KYC document

    @DeleteMapping("/kyc/{kycId}")
    public ResponseEntity<String> deleteKYCDocument(
            @PathVariable Long kycId) {

        kycService.deleteKYCDocument(kycId);
        return ResponseEntity.ok("KYC document deleted successfully"); // 200
    }
}