package com.reviewservice.feign;

import com.reviewservice.feign.dto.NotificationRequestDTO;
import com.reviewservice.feign.dto.NotificationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notifications-service")
public interface NotificationFeignClient {

    @PostMapping("/api/notifications")
    NotificationResponseDTO sendNotification(@RequestBody NotificationRequestDTO request);
}
