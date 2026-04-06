package com.wealthpro.orderexecution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main application class for the Order-Execution-Service.
 * <p>
 * This microservice handles order placement, pre-trade validation,
 * order routing, execution fills, and allocation workflows.
 * In-memory caching is enabled via {@code @EnableCaching}.
 * </p>
 *
 * @author WealthPro Team
 * @version 2.0
 */
@SpringBootApplication
@EnableCaching
public class OrderExecutionServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(OrderExecutionServiceApplication.class, args);
    }
}
