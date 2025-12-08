package com.tpanh.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.IntrospectRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
class AuthenticationControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    private static final String USERNAME = "testadmin";
    private static final String PASSWORD = "testpass123";
    private static final String FULL_NAME = "Test Admin User";

    @BeforeEach
    void setUp() {
        // Tạo user test
        final var user =
                User.builder()
                        .username(USERNAME)
                        .password(passwordEncoder.encode(PASSWORD))
                        .fullName(FULL_NAME)
                        .roles(Role.ADMIN)
                        .active(true)
                        .build();
        userRepository.save(user);
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Given
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").exists())
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"));
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldReturnError() throws Exception {
        // Given
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        request.setPassword("wrong-password");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void authenticate_WithMissingFields_ShouldReturnValidationError() throws Exception {
        // Given
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        // password is missing

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void introspect_WithValidToken_ShouldReturnValid() throws Exception {
        // Given - Tạo token hợp lệ bằng cách đăng nhập
        final var authRequest = new AuthenticationRequest();
        authRequest.setUsername(USERNAME);
        authRequest.setPassword(PASSWORD);

        final var authResponse =
                mockMvc.perform(
                                post("/api/v1/auth/token")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(authRequest)))
                        .andReturn();

        final var authResponseJson =
                objectMapper.readTree(authResponse.getResponse().getContentAsString());
        final var token = authResponseJson.get("result").get("token").asText();

        // When - Kiểm tra token
        final var introspectRequest = new IntrospectRequest();
        introspectRequest.setToken(token);

        // Then
        mockMvc.perform(
                        post("/api/v1/auth/introspect")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(introspectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.valid").value(true))
                .andExpect(jsonPath("$.message").value("Token hợp lệ"));
    }

    @Test
    void introspect_WithInvalidToken_ShouldReturnInvalid() throws Exception {
        // Given
        final var introspectRequest = new IntrospectRequest();
        introspectRequest.setToken("invalid.token.here");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/introspect")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(introspectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.valid").value(false))
                .andExpect(jsonPath("$.message").value("Token không hợp lệ hoặc đã hết hạn"));
    }
}
