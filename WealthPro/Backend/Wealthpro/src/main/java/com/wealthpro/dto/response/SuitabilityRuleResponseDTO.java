package com.wealthpro.dto.response;

import com.wealthpro.enums.RuleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuitabilityRuleResponseDTO {

    private Long ruleId;
    private String description;
    private String expression;
    private RuleStatus status;
}