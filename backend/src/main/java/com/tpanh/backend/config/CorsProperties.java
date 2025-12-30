package com.tpanh.backend.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cors")
@Data
public class CorsProperties {
    private static final long DEFAULT_MAX_AGE_SECONDS = 3600L;

    private List<String> allowedOrigins = List.of("*");

    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    private List<String> allowedHeaders = List.of("*");

    private boolean allowCredentials = false;

    private long maxAge = DEFAULT_MAX_AGE_SECONDS;
}
