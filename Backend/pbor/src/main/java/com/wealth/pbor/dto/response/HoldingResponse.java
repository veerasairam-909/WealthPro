package com.wealth.pbor.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class HoldingResponse {

    private Long holdingId;
    private Long accountId;
    private Long securityId;
    private BigDecimal quantity;
    private BigDecimal avgCost;
    private String valuationCurrency;
    private LocalDate lastValuationDate;
}