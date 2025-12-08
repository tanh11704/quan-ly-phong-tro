package com.tpanh.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "Phòng Trọ Số API",
                        version = "v1",
                        description =
                                "Tài liệu API cho ứng dụng Quản lý Phòng Trọ - Mini App Zalo"),
        servers = {
            @Server(url = "http://localhost:8080", description = "Local Development Server")
        })
public class OpenApiConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .packagesToScan("com.tpanh.backend.controller")
                .build();
    }
}
