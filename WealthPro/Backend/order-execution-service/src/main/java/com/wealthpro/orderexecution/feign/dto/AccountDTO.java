package com.wealthpro.orderexecution.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AccountDTO {
    private Long accountId;
    private Long clientId;
    private String accountType;
    private String baseCurrency;
    private String status;
    private BigDecimal cashBalance;
}
