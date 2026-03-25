package com.wealth.pbor.dto.request;

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
public class HoldingRequest {

    @NotNull(message = "{error.holding.account.required}")
    @Positive(message = "{error.holding.accountid.positive}")
    private Long accountId;

    @NotNull(message = "{error.holding.security.required}")
    @Positive(message = "{error.holding.securityid.positive}")
    private Long securityId;

    @NotNull(message = "{error.holding.quantity.required}")
    @Positive(message = "{error.holding.quantity.positive}")
    private BigDecimal quantity;

    @NotNull(message = "{error.holding.avgcost.required}")
    @Positive(message = "{error.holding.avgcost.positive}")
    private BigDecimal avgCost;

    @NotNull(message = "{error.holding.currency.required}")
    @Size(min = 3, max = 3, message = "{error.holding.currency.size}")
    private String valuationCurrency;

    @NotNull(message = "{error.holding.valuation.date.required}")
    private LocalDate lastValuationDate;
}