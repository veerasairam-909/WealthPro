package com.wealth.goalsadvisory.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RiskProfileDTO {
    private Long profileId;
    private Long clientId;
    private String riskClass;
    private BigDecimal riskScore;
    private BigDecimal maxEquityPct;
    private BigDecimal minCashPct;
}
