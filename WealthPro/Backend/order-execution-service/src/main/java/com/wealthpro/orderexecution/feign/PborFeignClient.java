package com.wealthpro.orderexecution.feign;

import com.wealthpro.orderexecution.feign.dto.AccountDTO;
import com.wealthpro.orderexecution.feign.dto.CashLedgerRequestDTO;
import com.wealthpro.orderexecution.feign.dto.HoldingDTO;
import com.wealthpro.orderexecution.feign.dto.HoldingRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    @GetMapping("/api/cash-ledger/account/{accountId}/balance")
    BigDecimal getCashBalanceByAccountId(@PathVariable Long accountId);

    @PostMapping("/api/holdings")
    HoldingDTO createHolding(@RequestBody HoldingRequestDTO request);

    @PostMapping("/api/cash-ledger")
    Object createCashLedgerEntry(@RequestBody CashLedgerRequestDTO request);
}
