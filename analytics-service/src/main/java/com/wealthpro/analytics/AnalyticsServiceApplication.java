package com.wealthpro.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main application class for the Analytics-Service.
 * <p>
 * Provides performance analytics (daily/monthly returns), risk assessment
 * (volatility, drawdown, VaR), and compliance breach detection.
 * </p>
 *
 * @author WealthPro Team
 * @version 2.0
 */
@SpringBootApplication
@EnableCaching
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
