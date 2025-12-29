package com.tpanh.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tpanh.backend.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsProperties corsProperties;

    public SecurityConfig(
            final JwtAuthenticationFilter jwtAuthenticationFilter,
            final CorsProperties corsProperties) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsProperties = corsProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username ->
                org.springframework.security.core.userdetails.User.builder()
                        .username(username)
                        .password("")
                        .authorities(java.util.Collections.emptyList())
                        .disabled(true)
                        .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final var configuration = new CorsConfiguration();

        final var origins = corsProperties.getAllowedOrigins();
        if (origins != null && !origins.isEmpty()) {
            if (origins.size() == 1 && "*".equals(origins.get(0))) {
                configuration.setAllowedOriginPatterns(java.util.List.of("*"));
            } else {
                configuration.setAllowedOriginPatterns(origins);
            }
        }

        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(this::configureAuthorization)
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void configureAuthorization(
            final AuthorizeHttpRequestsConfigurer<HttpSecurity>
                            .AuthorizationManagerRequestMatcherRegistry
                    authorize) {
        configurePublicEndpoints(authorize);
        configureAdminEndpoints(authorize);
        configureManagerEndpoints(authorize);
        authorize.anyRequest().authenticated();
    }

    private void configurePublicEndpoints(
            final AuthorizeHttpRequestsConfigurer<HttpSecurity>
                            .AuthorizationManagerRequestMatcherRegistry
                    authorize) {
        authorize
                .requestMatchers(
                        HttpMethod.GET,
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-ui/index.html",
                        "/swagger-resources/**",
                        "/webjars/**")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/swagger-config")
                .permitAll()
                .requestMatchers("/api/v1/auth/**", "/api/v1/token")
                .permitAll();
    }

    private void configureAdminEndpoints(
            final AuthorizeHttpRequestsConfigurer<HttpSecurity>
                            .AuthorizationManagerRequestMatcherRegistry
                    authorize) {
        authorize.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
    }

    private void configureManagerEndpoints(
            final AuthorizeHttpRequestsConfigurer<HttpSecurity>
                            .AuthorizationManagerRequestMatcherRegistry
                    authorize) {
        authorize
                .requestMatchers(
                        "/api/v1/buildings/**",
                        "/api/v1/rooms/**",
                        "/api/v1/invoices/**",
                        "/api/v1/tenants/**")
                .hasAnyRole("MANAGER", "ADMIN");
    }
}
