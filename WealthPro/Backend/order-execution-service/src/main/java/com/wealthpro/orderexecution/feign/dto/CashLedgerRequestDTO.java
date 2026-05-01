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
public class CashLedgerRequestDTO {
    private Long accountId;
    private String txnType;
    private Long securityId;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private String currency;
    private LocalDate txnDate;
    private String narrative;
}
