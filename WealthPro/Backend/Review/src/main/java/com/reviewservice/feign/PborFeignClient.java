package com.reviewservice.feign;

import com.reviewservice.feign.dto.AccountDTO;
import com.reviewservice.feign.dto.CashLedgerDTO;
import com.reviewservice.feign.dto.HoldingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "pbor-service")
public interface PborFeignClient {

    @GetMapping("/api/accounts/{accountId}")
    AccountDTO getAccountById(@PathVariable Long accountId);

    @GetMapping("/api/accounts/client/{clientId}")
    List<AccountDTO> getAccountsByClientId(@PathVariable Long clientId);

    @GetMapping("/api/holdings/account/{accountId}")
    List<HoldingDTO> getHoldingsByAccountId(@PathVariable Long accountId);

    @GetMapping("/api/cash-ledger/account/{accountId}")
    List<CashLedgerDTO> getCashLedgerEntriesByAccountId(@PathVariable Long accountId);

    @GetMapping("/api/cash-ledger/account/{accountId}/balance")
    BigDecimal getCashBalanceByAccountId(@PathVariable Long accountId);
}
