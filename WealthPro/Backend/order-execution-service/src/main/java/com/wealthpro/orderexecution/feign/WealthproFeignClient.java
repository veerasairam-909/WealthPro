package com.wealthpro.orderexecution.feign;

import com.wealthpro.orderexecution.feign.dto.ClientDTO;
import com.wealthpro.orderexecution.feign.dto.RiskProfileDTO;
import com.wealthpro.orderexecution.feign.dto.SuitabilityRuleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "wealthpro-service")
public interface WealthproFeignClient {

    @GetMapping("/api/clients/{clientId}")
    ClientDTO getClientById(@PathVariable Long clientId);

    @GetMapping("/api/clients/{clientId}/risk-profile")
    RiskProfileDTO getRiskProfileByClientId(@PathVariable Long clientId);

    @GetMapping("/api/suitability-rules")
    List<SuitabilityRuleDTO> getAllSuitabilityRules();
}
