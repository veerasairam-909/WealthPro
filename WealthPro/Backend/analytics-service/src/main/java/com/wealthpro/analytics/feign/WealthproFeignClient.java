package com.wealthpro.analytics.feign;

import com.wealthpro.analytics.feign.dto.RiskProfileDTO;
import com.wealthpro.analytics.feign.dto.SuitabilityRuleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "wealthpro-service")
public interface WealthproFeignClient {

    @GetMapping("/api/clients/{clientId}/risk-profile")
    RiskProfileDTO getRiskProfileByClientId(@PathVariable Long clientId);

    @GetMapping("/api/suitability-rules")
    List<SuitabilityRuleDTO> getAllSuitabilityRules();

    @GetMapping("/api/suitability-rules/{ruleId}")
    SuitabilityRuleDTO getSuitabilityRuleById(@PathVariable Long ruleId);
}
