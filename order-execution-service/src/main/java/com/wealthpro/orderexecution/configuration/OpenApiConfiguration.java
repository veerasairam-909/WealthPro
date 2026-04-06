package com.wealthpro.orderexecution.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the Order-Execution-Service.
 * <p>
 * Provides metadata displayed on the Swagger UI page.
 * Access at: {@code http://localhost:8081/swagger-ui.html}
 * </p>
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * Defines the OpenAPI metadata for this service.
     *
     * @return configured OpenAPI object
     */
    @Bean
    public OpenAPI orderExecutionOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order-Execution-Service API")
                        .description("REST API for Order Placement, Pre-Trade Checks, Execution Fills, and Allocations")
                        .version("2.0"));
    }
}
