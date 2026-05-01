package com.wealthpro.orderexecution.feign;

import com.wealthpro.orderexecution.feign.dto.ProductTermDTO;
import com.wealthpro.orderexecution.feign.dto.SecurityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "productcatalog-service")
public interface ProductCatalogFeignClient {

    @GetMapping("/api/securities/{securityId}")
    SecurityDTO getSecurityById(@PathVariable Long securityId);

    @GetMapping("/api/securities/symbol/{symbol}")
    SecurityDTO getSecurityBySymbol(@PathVariable String symbol);

    @GetMapping("/api/product-terms/security/{securityId}")
    List<ProductTermDTO> getProductTermsBySecurityId(@PathVariable Long securityId);
}
