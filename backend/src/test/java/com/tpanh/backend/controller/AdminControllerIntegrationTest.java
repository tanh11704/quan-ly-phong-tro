package com.tpanh.backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class AdminControllerIntegrationTest {

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

    private static final String ADMIN_USERNAME = "testadmin";
    private static final String ADMIN_PASSWORD = "testpass123";
    private static final String MANAGER_USERNAME = "testmanager";
    private static final String MANAGER_PASSWORD = "managerpass123";

    private String adminToken;
    private String managerId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .build();

        userRepository.deleteAll();
        userRepository.flush();

        // Create Admin user
        final var adminUser =
                User.builder()
                        .username(ADMIN_USERNAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .fullName("Test Admin")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.ADMIN)))
                        .active(true)
                        .build();
        userRepository.save(adminUser);

        // Create Manager user
        final var managerUser =
                User.builder()
                        .username(MANAGER_USERNAME)
                        .password(passwordEncoder.encode(MANAGER_PASSWORD))
                        .fullName("Test Manager")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.MANAGER)))
                        .active(true)
                        .build();
        final var savedManager = userRepository.save(managerUser);
        managerId = savedManager.getId();
        userRepository.flush();

        // Login as Admin to get token
        final var authRequest = new AuthenticationRequest();
        authRequest.setUsername(ADMIN_USERNAME);
        authRequest.setPassword(ADMIN_PASSWORD);

        final var authResponse =
                mockMvc.perform(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                        .post("/api/v1/auth/token")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(authRequest)))
                        .andReturn();

        final var authResponseJson =
                objectMapper.readTree(authResponse.getResponse().getContentAsString());
        adminToken = authResponseJson.get("result").get("token").asText();
    }

    @Test
    void getAllUsers_WithAdminToken_ShouldReturnAllUsers() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/users")
                                .with(user("testadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].fullName").exists())
                .andExpect(jsonPath("$.content[0].roles").exists())
                .andExpect(jsonPath("$.content[0].active").exists())
                .andExpect(jsonPath("$.message").value("Lấy danh sách người dùng thành công"));
    }

    @Test
    void getAllUsers_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_WithManagerToken_ShouldReturnForbidden() throws Exception {
        // Login as Manager
        final var managerAuthRequest = new AuthenticationRequest();
        managerAuthRequest.setUsername(MANAGER_USERNAME);
        managerAuthRequest.setPassword(MANAGER_PASSWORD);

        final var managerAuthResponse =
                mockMvc.perform(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                        .post("/api/v1/auth/token")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        managerAuthRequest)))
                        .andReturn();

        final var managerAuthResponseJson =
                objectMapper.readTree(managerAuthResponse.getResponse().getContentAsString());
        final var managerToken = managerAuthResponseJson.get("result").get("token").asText();

        // Try to access admin endpoint
        mockMvc.perform(
                        get("/api/v1/admin/users")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void toggleUserActive_WithAdminToken_ShouldDeactivateUser() throws Exception {
        mockMvc.perform(
                        put("/api/v1/admin/users/" + managerId + "/toggle-active")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(managerId))
                .andExpect(jsonPath("$.result.active").value(false))
                .andExpect(jsonPath("$.message").value("Đã khóa tài khoản thành công"));
    }

    @Test
    void toggleUserActive_WithAdminToken_ShouldActivateUser() throws Exception {
        // First deactivate
        mockMvc.perform(
                        put("/api/v1/admin/users/" + managerId + "/toggle-active")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Then activate again
        mockMvc.perform(
                        put("/api/v1/admin/users/" + managerId + "/toggle-active")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(managerId))
                .andExpect(jsonPath("$.result.active").value(true))
                .andExpect(jsonPath("$.message").value("Đã mở khóa tài khoản thành công"));
    }

    @Test
    void toggleUserActive_WithInvalidUserId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(
                        put("/api/v1/admin/users/invalid-user-id/toggle-active")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void toggleUserActive_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(
                        put("/api/v1/admin/users/" + managerId + "/toggle-active")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void toggleUserActive_WithManagerToken_ShouldReturnForbidden() throws Exception {
        // Login as Manager
        final var managerAuthRequest = new AuthenticationRequest();
        managerAuthRequest.setUsername(MANAGER_USERNAME);
        managerAuthRequest.setPassword(MANAGER_PASSWORD);

        final var managerAuthResponse =
                mockMvc.perform(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                        .post("/api/v1/auth/token")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        managerAuthRequest)))
                        .andReturn();

        final var managerAuthResponseJson =
                objectMapper.readTree(managerAuthResponse.getResponse().getContentAsString());
        final var managerToken = managerAuthResponseJson.get("result").get("token").asText();

        // Try to toggle user active
        mockMvc.perform(
                        put("/api/v1/admin/users/" + managerId + "/toggle-active")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
