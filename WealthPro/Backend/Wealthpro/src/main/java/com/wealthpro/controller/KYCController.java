package com.wealthpro.controller;

import com.wealthpro.dto.request.KYCStatusUpdateRequestDTO;
import com.wealthpro.dto.response.KYCDocumentResponseDTO;
import com.wealthpro.security.AuthContext;
import com.wealthpro.service.KYCService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/clients")
public class KYCController {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "application/pdf"
    );

    private final KYCService kycService;

    public KYCController(KYCService kycService) {
        this.kycService = kycService;
    }


    // POST /api/clients/{clientId}/kyc — upload KYC doc.
    // CLIENT may upload their OWN; staff may upload for any client.
    @PostMapping(value = "/{clientId}/kyc",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KYCDocumentResponseDTO> addKYCDocument(
            @PathVariable Long clientId,
            @RequestParam("documentType") String documentType,
            @RequestParam("document") MultipartFile document,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only upload KYC for your own account.");
        }

        String contentType = document.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file type. Only JPEG, PNG, and PDF files are allowed.");
        }

        KYCDocumentResponseDTO response = kycService.addKYCDocument(
                clientId, documentType, document);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
    }

    // GET /api/clients/{clientId}/kyc — list KYC docs.
    // CLIENT may see their OWN; staff may see any client's.
    @GetMapping("/{clientId}/kyc")
    public ResponseEntity<List<KYCDocumentResponseDTO>> getKYCDocumentsByClient(
            @PathVariable Long clientId,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view your own KYC documents.");
        }

        return ResponseEntity.ok(kycService.getKYCDocumentsByClient(clientId)); // 200
    }


    // GET /api/clients/kyc/{kycId} — staff-only.
    @GetMapping("/kyc/{kycId}")
    public ResponseEntity<KYCDocumentResponseDTO> getKYCDocumentById(
            @PathVariable Long kycId) {

        return ResponseEntity.ok(kycService.getKYCDocumentById(kycId)); // 200
    }


    // PUT /api/clients/kyc/{kycId}/status — staff-only KYC verification.
    @PutMapping("/kyc/{kycId}/status")
    public ResponseEntity<KYCDocumentResponseDTO> updateKYCStatus(
            @PathVariable Long kycId,
            @Valid @RequestBody KYCStatusUpdateRequestDTO requestDTO) {

        return ResponseEntity.ok(kycService.updateKYCStatus(kycId, requestDTO)); // 200
    }


    // GET /api/clients/kyc/{kycId}/document — serve the raw document file.
    // Used by the Compliance KYC Approval page to preview the uploaded image/PDF.
    @GetMapping("/kyc/{kycId}/document")
    public ResponseEntity<Resource> getKycDocumentFile(@PathVariable Long kycId) {

        // 1. Fetch the stored file-system path from DB
        String filePath = kycService.getKycDocumentFilePath(kycId);

        // 2. Resolve to a filesystem resource
        Path path = Paths.get(filePath);
        Resource resource = new FileSystemResource(path);

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Document file not found on server: " + filePath);
        }

        // 3. Detect MIME type from the file extension / content
        String contentType;
        try {
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            contentType = null;
        }
        if (contentType == null) {
            // Fallback: guess from extension
            String lower = filePath.toLowerCase();
            if (lower.endsWith(".pdf"))  contentType = "application/pdf";
            else if (lower.endsWith(".png"))  contentType = "image/png";
            else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) contentType = "image/jpeg";
            else contentType = "application/octet-stream";
        }

        // 4. Return file bytes with correct Content-Type
        //    "inline" tells the browser to display it, not trigger a download dialog
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + path.getFileName().toString() + "\"")
                .body(resource);
    }


    // DELETE /api/clients/kyc/{kycId} — staff-only.
    @DeleteMapping("/kyc/{kycId}")
    public ResponseEntity<String> deleteKYCDocument(
            @PathVariable Long kycId) {

        kycService.deleteKYCDocument(kycId);
        return ResponseEntity.ok("KYC document deleted successfully"); // 200
    }
}
