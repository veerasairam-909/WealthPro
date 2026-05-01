package com.wealthpro.orderexecution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
@SpringBootApplication
@EnableCaching
@EnableDiscoveryClient(autoRegister = true)
@EnableFeignClients
public class OrderExecutionServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(OrderExecutionServiceApplication.class, args);
    }
}
