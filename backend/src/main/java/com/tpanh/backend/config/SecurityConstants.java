package com.tpanh.backend.config;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String[] AUTHENTICATION_BYPASS_PATHS = {
        "/api/v1/auth/",
        "/api/v1/token",
        "/v3/api-docs",
        "/swagger-ui",
        "/swagger-resources",
        "/webjars/"
    };

    public static final String[] PERMIT_ALL_ENDPOINTS = {
        "/api/v1/auth/**",
        "/api/v1/token",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/webjars/**"
    };
}
