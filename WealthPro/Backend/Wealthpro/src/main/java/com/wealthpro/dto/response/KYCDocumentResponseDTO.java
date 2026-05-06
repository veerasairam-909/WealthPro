package com.wealthpro.dto.response;

import com.wealthpro.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KYCDocumentResponseDTO {

    private Long kycId;
    private Long clientId;
    private String documentType;
    private String documentRef;
    private String documentRefNumber;
    private LocalDate verifiedDate;
    private LocalDate expiryDate;
    private KycStatus status;
}