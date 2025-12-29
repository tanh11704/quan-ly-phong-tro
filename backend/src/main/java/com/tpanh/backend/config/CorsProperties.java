package com.tpanh.backend.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.cors")
@Data
public class CorsProperties {
    private List<String> allowedOrigins = List.of("*");

    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    private List<String> allowedHeaders = List.of("*");

    private boolean allowCredentials = false;

    private long maxAge = 3600L;
}
