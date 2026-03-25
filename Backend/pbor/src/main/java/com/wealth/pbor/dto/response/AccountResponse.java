package com.wealth.pbor.dto.response;

import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import lombok.Data;

@Data
public class AccountResponse {

    private Long accountId;
    private Long clientId;
    private AccountType accountType;
    private String baseCurrency;
    private AccountStatus status;
}