package com.tpanh.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sentry")
@Getter
@Setter
public class SentryProperties {
    private String organization;
    private String project;
    private String authToken;
    private String baseUrl = "https://sentry.io/api/0";
}
