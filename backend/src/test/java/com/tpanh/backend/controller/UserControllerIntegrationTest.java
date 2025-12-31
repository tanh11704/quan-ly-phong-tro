package com.tpanh.backend.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tpanh.backend.dto.AuthenticationRequest;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
class UserControllerIntegrationTest {

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
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.ADMIN)))
                        .active(true)
                        .build();
        userRepository.save(user);
    }

    @Test
    void getMyInfo_WithValidToken_ShouldReturnUserInfo() throws Exception {
        // 1. Login để lấy token
        final var authRequest = new AuthenticationRequest();
        authRequest.setUsername(USERNAME);
        authRequest.setPassword(PASSWORD);

        final var authResponse =
                mockMvc.perform(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                        .post("/api/v1/auth/token")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(authRequest)))
                        .andReturn();

        final var authResponseJson =
                objectMapper.readTree(authResponse.getResponse().getContentAsString());
        final var token = authResponseJson.get("result").get("token").asText();

        // 2. Gọi API my-info với token
        mockMvc.perform(
                        get("/api/v1/users/my-info")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.result.username").value(USERNAME))
                .andExpect(jsonPath("$.result.fullName").value(FULL_NAME))
                .andExpect(jsonPath("$.result.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.message").value("Lấy thông tin người dùng thành công"));
    }

    @Test
    void getMyInfo_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/my-info").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyInfo_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/my-info")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyInfo_WithTenantUser_ShouldReturnTenantInfo() throws Exception {
        // 1. Tạo tenant user
        final var tenantUser =
                User.builder()
                        .zaloId("zalo-id-123")
                        .fullName("Tenant User")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.USER)))
                        .active(true)
                        .build();
        final var savedTenant = userRepository.save(tenantUser);

        // 2. Tạo token cho tenant (giả lập bằng cách tạo user với username để login)
        // Vì tenant không có username, ta sẽ tạo một user tạm để test
        final var tempUser =
                User.builder()
                        .username("tenantuser")
                        .password(passwordEncoder.encode("tenantpass"))
                        .fullName("Tenant User")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.USER)))
                        .active(true)
                        .build();
        userRepository.save(tempUser);

        final var authRequest = new AuthenticationRequest();
        authRequest.setUsername("tenantuser");
        authRequest.setPassword("tenantpass");

        final var authResponse =
                mockMvc.perform(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                        .post("/api/v1/auth/token")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(authRequest)))
                        .andReturn();

        final var authResponseJson =
                objectMapper.readTree(authResponse.getResponse().getContentAsString());
        final var token = authResponseJson.get("result").get("token").asText();

        // 3. Gọi API my-info với token
        mockMvc.perform(
                        get("/api/v1/users/my-info")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.result.username").value("tenantuser"))
                .andExpect(jsonPath("$.result.fullName").value("Tenant User"))
                .andExpect(jsonPath("$.result.roles[0]").value("USER"))
                .andExpect(jsonPath("$.message").value("Lấy thông tin người dùng thành công"));
    }
}
