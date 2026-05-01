package com.wealthpro.dto.request;

import com.wealthpro.enums.RuleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuitabilityRuleRequestDTO {

    @NotBlank(message = "Description is required")
    @Size(max = 250, message = "Description must not exceed 250 characters")
    private String description;

    // The rule logic expression
    // e.g. "riskClass == Conservative AND assetClass != Equity"
    @NotBlank(message = "Expression is required")
    private String expression;

    @NotNull(message = "Status is required (Active / Inactive)")
    private RuleStatus status;
}