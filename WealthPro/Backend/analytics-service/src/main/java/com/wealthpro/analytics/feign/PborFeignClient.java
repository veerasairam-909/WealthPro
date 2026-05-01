package com.wealthpro.analytics.feign;

import com.wealthpro.analytics.feign.dto.AccountDTO;
import com.wealthpro.analytics.feign.dto.HoldingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "pbor-service")
public interface PborFeignClient {

    @GetMapping("/api/accounts/{accountId}")
    AccountDTO getAccountById(@PathVariable Long accountId);

    @GetMapping("/api/accounts/client/{clientId}")
    List<AccountDTO> getAccountsByClientId(@PathVariable Long clientId);

    @GetMapping("/api/holdings/account/{accountId}")
    List<HoldingDTO> getHoldingsByAccountId(@PathVariable Long accountId);
}
