package com.wealthpro.analytics.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the Analytics-Service.
 * Access at: {@code http://localhost:8082/swagger-ui.html}
 *
 * @author WealthPro Team
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI analyticsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Analytics-Service API")
                        .description("REST API for Performance Analytics, Risk Assessment, and Compliance Breach Detection")
                        .version("2.0"));
    }
}
