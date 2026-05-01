package com.wealthpro.feign;

import com.wealthpro.feign.dto.NotificationRequestDTO;
import com.wealthpro.feign.dto.NotificationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notifications-service")
public interface NotificationFeignClient {

    @PostMapping("/api/notifications")
    NotificationResponseDTO sendNotification(@RequestBody NotificationRequestDTO request);
}
