package com.wealthpro.orderexecution.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ProductTermDTO {
    private Long termId;
    private Long securityId;
    private BigDecimal minInvestmentAmount;
    private BigDecimal maxInvestmentAmount;
    private BigDecimal lotSize;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
