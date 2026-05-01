package com.wealthpro.analytics.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PBOR-SERVICE")
public interface PborAccountClient {
    @GetMapping("/api/accounts/{accountId}/owner")
    Long getAccountOwner(@PathVariable("accountId") Long accountId);
}
