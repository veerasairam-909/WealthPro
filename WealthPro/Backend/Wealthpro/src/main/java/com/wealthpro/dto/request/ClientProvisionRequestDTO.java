package com.wealthpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload posted by the API Gateway after a CLIENT self-registers.
 * Creates a stub Client row with status = PENDING_KYC linked to the login.
 * Only username/name/email are required — real KYC data is filled in later.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientProvisionRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String name;

    private String email;
    private String phone;
}
