package com.wealthpro.analytics.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SuitabilityRuleDTO {
    private Long ruleId;
    private String riskClass;
    private BigDecimal maxEquityPct;
    private BigDecimal minCashPct;
    private BigDecimal maxSingleStockPct;
    private String status;
}
