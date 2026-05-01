package com.wealthpro.orderexecution.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SuitabilityRuleDTO {
    private Long ruleId;
    private String description;
    private String expression;
    private String status;
}
