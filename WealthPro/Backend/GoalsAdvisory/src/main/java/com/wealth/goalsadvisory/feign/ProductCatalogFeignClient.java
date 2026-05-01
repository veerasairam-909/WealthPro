package com.wealth.goalsadvisory.feign;

import com.wealth.goalsadvisory.feign.dto.SecurityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "productcatalog-service")
public interface ProductCatalogFeignClient {

    @GetMapping("/api/securities/{securityId}")
    SecurityDTO getSecurityById(@PathVariable Long securityId);

    @GetMapping("/api/securities/symbol/{symbol}")
    SecurityDTO getSecurityBySymbol(@PathVariable String symbol);
}
