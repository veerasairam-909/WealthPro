package com.reviewservice.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CashLedgerDTO {
    private Long ledgerId;
    private Long accountId;
    private String txnType;
    private BigDecimal amount;
    private LocalDate txnDate;
    private String description;
}
