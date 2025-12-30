package com.tpanh.backend.config;

/**
 * Centralized security constants to ensure consistency across SecurityConfig and
 * JwtAuthenticationFilter. This is the single source of truth for public endpoints configuration.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Utility class - prevent instantiation
    }

    /**
     * Public endpoints that do not require authentication. Used by both SecurityConfig (URL-based
     * security) and JwtAuthenticationFilter (shouldNotFilter).
     */
    public static final String[] PUBLIC_ENDPOINTS = {"/api/v1/auth/**", "/api/v1/token"};

    /** Swagger/OpenAPI documentation endpoints. */
    public static final String[] SWAGGER_ENDPOINTS = {
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/v3/api-docs.yaml",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/swagger-ui/index.html",
        "/swagger-resources/**",
        "/webjars/**"
    };

    /** All public paths combined (for filter use). */
    public static final String[] ALL_PUBLIC_PATHS = {
        "/api/v1/auth/",
        "/api/v1/token",
        "/v3/api-docs",
        "/swagger-ui",
        "/swagger-resources",
        "/webjars/"
    };
}
