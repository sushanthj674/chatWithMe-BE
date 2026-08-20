package com.chatwithme.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI chatWithMeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("chatWithMe Backend API")
                .description("Device registry + message relay over FCM for the chatWithMe RN app.")
                .version("v1"));
    }
}
