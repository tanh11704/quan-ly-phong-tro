package com.tpanh.backend.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tpanh.backend.client.ZaloIdentityClient;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.ExchangeTokenRequest;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
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

    @Autowired private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private ZaloIdentityClient zaloIdentityClient;

    private static final String USERNAME = "testadmin";
    private static final String PASSWORD = "testpass123";
    private static final String FULL_NAME = "Test Admin User";

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .build();

        userRepository.deleteAll();

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
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

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
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        request.setPassword("wrong-password");

        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Hoặc isUnauthorized() tùy exception handler
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void authenticate_WithMissingFields_ShouldReturnValidationError() throws Exception {
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        // Thiếu password

        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Check validation
    }

    @Test
    void introspect_WithValidToken_ShouldReturnValid() throws Exception {
        // 1. Login lấy token
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

        // 2. Introspect
        final var introspectRequest = new IntrospectRequest();
        introspectRequest.setToken(token);

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

    @Test
    void outboundAuthenticate_WithNewZaloUser_ShouldCreateUserAndReturnToken() throws Exception {
        // Given
        final var zaloToken = "zalo-access-token";
        final var zaloUserInfo = new ZaloIdentityClient.ZaloUserInfo();
        zaloUserInfo.setId("zalo-id-123");
        zaloUserInfo.setName("Zalo User Name");

        when(zaloIdentityClient.getUserInfo(anyString())).thenReturn(zaloUserInfo);

        final var request = new ExchangeTokenRequest();
        request.setToken(zaloToken);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/outbound/authentication")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").exists())
                .andExpect(jsonPath("$.message").value("Xác thực Zalo thành công"));
    }

    @Test
    void outboundAuthenticate_WithExistingZaloUser_ShouldReturnToken() throws Exception {
        // Given - Create existing Zalo user
        final var zaloId = "zalo-id-456";
        final var existingUser =
                User.builder()
                        .zaloId(zaloId)
                        .fullName("Existing Zalo User")
                        .roles(Role.TENANT)
                        .active(true)
                        .build();
        userRepository.save(existingUser);

        final var zaloToken = "zalo-access-token";
        final var zaloUserInfo = new ZaloIdentityClient.ZaloUserInfo();
        zaloUserInfo.setId(zaloId);
        zaloUserInfo.setName("Existing Zalo User");

        when(zaloIdentityClient.getUserInfo(anyString())).thenReturn(zaloUserInfo);

        final var request = new ExchangeTokenRequest();
        request.setToken(zaloToken);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/outbound/authentication")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").exists())
                .andExpect(jsonPath("$.message").value("Xác thực Zalo thành công"));
    }

    @Test
    void outboundAuthenticate_WithInactiveZaloUser_ShouldReturnError() throws Exception {
        // Given - Create inactive Zalo user
        final var zaloId = "zalo-id-789";
        final var inactiveUser =
                User.builder()
                        .zaloId(zaloId)
                        .fullName("Inactive Zalo User")
                        .roles(Role.TENANT)
                        .active(false)
                        .build();
        userRepository.save(inactiveUser);

        final var zaloToken = "zalo-access-token";
        final var zaloUserInfo = new ZaloIdentityClient.ZaloUserInfo();
        zaloUserInfo.setId(zaloId);
        zaloUserInfo.setName("Inactive Zalo User");

        when(zaloIdentityClient.getUserInfo(anyString())).thenReturn(zaloUserInfo);

        final var request = new ExchangeTokenRequest();
        request.setToken(zaloToken);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/outbound/authentication")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists());
    }
}
