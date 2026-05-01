package com.wealth.goalsadvisory.feign;

import com.wealth.goalsadvisory.feign.dto.ClientDTO;
import com.wealth.goalsadvisory.feign.dto.RiskProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "wealthpro-service")
public interface WealthproFeignClient {

    @GetMapping("/api/clients/{clientId}")
    ClientDTO getClientById(@PathVariable Long clientId);

    @GetMapping("/api/clients/{clientId}/risk-profile")
    RiskProfileDTO getRiskProfileByClientId(@PathVariable Long clientId);
}
