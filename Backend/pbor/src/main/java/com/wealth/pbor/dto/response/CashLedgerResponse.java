package com.wealth.pbor.dto.response;

import com.wealth.pbor.enums.TxnType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class CashLedgerResponse {

    private Long ledgerId;

    private Long accountId;

    private TxnType txnType;

    private String txnDirection;

    private BigDecimal amount;
    private String currency;
    private LocalDate txnDate;
    private String narrative;
}