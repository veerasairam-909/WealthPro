package com.wealthpro.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient(autoRegister = true)
public class WealthproNotifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(WealthproNotifyApplication.class, args);
    }

}
