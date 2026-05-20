package com.fleet.maintenance.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    OpenAPI fleetMaintenanceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Smart Fleet Maintenance and Service Authorization API")
                .version("1.0.0"));
    }
}
