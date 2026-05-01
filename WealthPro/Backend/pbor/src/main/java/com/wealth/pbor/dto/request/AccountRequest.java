package com.wealth.pbor.dto.request;

import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotNull(message = "{error.account.client.required}")
    @Positive(message = "{error.account.clientid.positive}")
    private Long clientId;

    @NotNull(message = "{error.account.type.required}")
    private AccountType accountType;

    @NotNull(message = "{error.account.currency.required}")
    @Size(min = 3, max = 3, message = "{error.account.currency.size}")
    private String baseCurrency;

    @NotNull(message = "{error.account.status.required}")
    private AccountStatus status;
}