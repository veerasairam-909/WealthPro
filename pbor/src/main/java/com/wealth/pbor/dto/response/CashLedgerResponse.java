package com.wealth.pbor.dto.response;

import com.wealth.pbor.enums.TxnType;
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
public class CashLedgerResponse {

    private Long ledgerId;
    private Long accountId;
    private TxnType txnType;
    private BigDecimal amount;
    private String currency;
    private LocalDate txnDate;
    private String narrative;
}
