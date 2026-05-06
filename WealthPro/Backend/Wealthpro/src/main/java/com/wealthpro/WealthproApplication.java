package com.wealthpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient(autoRegister = true)
@EnableFeignClients
@EnableScheduling
public class WealthproApplication {

    public static void main(String[] args) {
        SpringApplication.run(WealthproApplication.class, args);
    }
}
