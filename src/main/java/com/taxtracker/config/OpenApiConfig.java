package com.taxtracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taxTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaxTracker REST API")
                        .description("Tax filing and management platform for individual taxpayers in India")
                        .version("0.0.1")
                        .license(new License().name("Infosys Capstone").url("http://localhost:8080")));
    }
}
