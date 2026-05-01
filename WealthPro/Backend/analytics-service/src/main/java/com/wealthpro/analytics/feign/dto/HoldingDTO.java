package com.wealthpro.analytics.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class HoldingDTO {
    private Long holdingId;
    private Long accountId;
    private Long securityId;
    private BigDecimal quantity;
    private BigDecimal avgCost;
    private BigDecimal marketValue;
}
