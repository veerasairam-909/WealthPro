package com.reviewservice.feign;

import com.reviewservice.feign.dto.ClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "wealthpro-service")
public interface WealthproFeignClient {

    @GetMapping("/api/clients/{clientId}")
    ClientDTO getClientById(@PathVariable Long clientId);
}
