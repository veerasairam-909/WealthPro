package com.wealth.pbor.dto.request;

import com.wealth.pbor.enums.TxnType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class CashLedgerRequest {

    @NotNull(message = "{error.cashledger.account.required}")
    @Positive(message = "{error.cashledger.accountid.positive}")
    private Long accountId;

    @NotNull(message = "{error.cashledger.txntype.required}")
    private TxnType txnType;

    @Positive(message = "{error.cashledger.securityid.positive}")
    private Long securityId;

    @Positive(message = "{error.cashledger.quantity.positive}")
    private BigDecimal quantity;

    @Positive(message = "{error.cashledger.price.positive}")
    private BigDecimal price;

    @Positive(message = "{error.cashledger.amount.positive}")
    private BigDecimal amount;

    @NotNull(message = "{error.cashledger.currency.required}")
    @Size(min = 3, max = 3, message = "{error.cashledger.currency.size}")
    private String currency;

    @NotNull(message = "{error.cashledger.txndate.required}")
    private LocalDate txnDate;

    @NotBlank(message = "{error.cashledger.narrative.required}")
    @Size(max = 500, message = "{error.cashledger.narrative.size}")
    private String narrative;
}
