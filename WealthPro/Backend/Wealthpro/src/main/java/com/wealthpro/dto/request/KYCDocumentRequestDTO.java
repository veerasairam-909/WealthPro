package com.wealthpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KYCDocumentRequestDTO {

    // clientId comes from the URL path variable
    // e.g. POST /api/clients/{clientId}/kyc

    @NotBlank(message = "Document type is required (e.g. PAN, Aadhaar, Passport)")
    @Size(max = 50, message = "Document type must not exceed 50 characters")
    private String documentType;

    // The actual image file uploaded from Postman (form-data)
    // This gets uploaded to Cloudinary — the returned URL is saved as documentRef in DB
    @NotNull(message = "Document image is required")
    private MultipartFile document;

    // documentRef is NOT here anymore — it gets set by the service
    // after uploading to Cloudinary and getting back the URL
}