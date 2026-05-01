package com.wealth.pbor.feign;

import com.wealth.pbor.feign.dto.NotificationRequestDTO;
import com.wealth.pbor.feign.dto.NotificationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notifications-service")
public interface NotificationFeignClient {

    @PostMapping("/api/notifications")
    NotificationResponseDTO sendNotification(@RequestBody NotificationRequestDTO request);
}
