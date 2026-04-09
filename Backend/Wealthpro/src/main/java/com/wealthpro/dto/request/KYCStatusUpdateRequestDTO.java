package com.wealthpro.dto.request;

import com.wealthpro.enums.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KYCStatusUpdateRequestDTO {

    // Used for: PUT /api/kyc/{kycId}/status
    // Only the status field is needed — nothing else should change

    @NotNull(message = "Status is required (Pending / Verified / Expired)")
    private KycStatus status;
}