package com.example.fullstack.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Fullstack API",
                version = "1.0.0",
                description = "REST API for the Spring, PostgreSQL, and Vue starter"
        )
)
public class OpenApiConfig {
}
