package com.wealthpro.orderexecution.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoldingRequestDTO {
    private Long accountId;
    private Long securityId;
    private BigDecimal quantity;
    private BigDecimal avgCost;
    private String valuationCurrency;
    private LocalDate lastValuationDate;
}
