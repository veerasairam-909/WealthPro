package com.wealthpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AmlFlagRequestDTO {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Flag type is required")
    private String flagType;

    @NotBlank(message = "Description is required")
    private String description;

    private String notes;
}
