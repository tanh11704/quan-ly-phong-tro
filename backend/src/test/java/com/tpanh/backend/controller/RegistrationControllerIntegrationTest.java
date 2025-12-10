package com.tpanh.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tpanh.backend.dto.RegistrationRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.UserStatus;
import com.tpanh.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
class RegistrationControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private RedisTemplate<String, String> redisTemplate;

    private static final String USERNAME = "newmanager";
    private static final String PASSWORD = "password123";
    private static final String FULL_NAME = "New Manager";
    private static final String EMAIL = "newmanager@example.com";

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .build();
        userRepository.deleteAll();
        // Clear Redis keys
        final var keys = redisTemplate.keys("activation:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void register_WithValidRequest_ShouldCreatePendingUser() throws Exception {
        // Given
        final var request = new RegistrationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        request.setFullName(FULL_NAME);
        request.setEmail(EMAIL);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.userId").exists())
                .andExpect(jsonPath("$.result.message").exists())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Đăng ký thành công. Vui lòng kiểm tra email để kích hoạt tài khoản."));

        // Verify user was created with PENDING status
        final var savedUser = userRepository.findByUsername(USERNAME);
        assert savedUser.isPresent();
        assertEquals(UserStatus.PENDING, savedUser.get().getStatus());
        assertEquals(false, savedUser.get().getActive());
        assertEquals(Role.MANAGER, savedUser.get().getRoles());
    }

    @Test
    void register_WithExistingUsername_ShouldReturnError() throws Exception {
        // Given - Create existing user
        final var existingUser =
                User.builder()
                        .username(USERNAME)
                        .password(passwordEncoder.encode(PASSWORD))
                        .fullName("Existing User")
                        .email("existing@example.com")
                        .roles(Role.MANAGER)
                        .status(UserStatus.ACTIVE)
                        .active(true)
                        .build();
        userRepository.save(existingUser);

        final var request = new RegistrationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        request.setFullName(FULL_NAME);
        request.setEmail(EMAIL);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(3011)); // USERNAME_ALREADY_EXISTS
    }

    @Test
    void register_WithExistingEmail_ShouldReturnError() throws Exception {
        // Given - Create existing user with same email
        final var existingUser =
                User.builder()
                        .username("otheruser")
                        .password(passwordEncoder.encode(PASSWORD))
                        .fullName("Existing User")
                        .email(EMAIL)
                        .roles(Role.MANAGER)
                        .status(UserStatus.ACTIVE)
                        .active(true)
                        .build();
        userRepository.save(existingUser);

        final var request = new RegistrationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        request.setFullName(FULL_NAME);
        request.setEmail(EMAIL);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(3012)); // EMAIL_ALREADY_EXISTS
    }

    @Test
    void register_WithInvalidEmail_ShouldReturnValidationError() throws Exception {
        // Given
        final var request = new RegistrationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        request.setFullName(FULL_NAME);
        request.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithShortPassword_ShouldReturnValidationError() throws Exception {
        // Given
        final var request = new RegistrationRequest();
        request.setUsername(USERNAME);
        request.setPassword("12345"); // Less than 6 characters
        request.setFullName(FULL_NAME);
        request.setEmail(EMAIL);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_WithValidToken_ShouldActivateUser() throws Exception {
        // Given - Register a user first
        final var request = new RegistrationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        request.setFullName(FULL_NAME);
        request.setEmail(EMAIL);

        final var registerResponse =
                mockMvc.perform(
                                post("/api/v1/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andReturn();

        final var userId =
                objectMapper
                        .readTree(registerResponse.getResponse().getContentAsString())
                        .get("result")
                        .get("userId")
                        .asText();

        // Get activation token from Redis
        final var tokenKey = "activation:user:" + userId;
        final var token = redisTemplate.opsForValue().get(tokenKey);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/activate").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.message").value("Bạn có thể đăng nhập ngay bây giờ"));

        // Verify user is now ACTIVE
        final var activatedUser = userRepository.findById(userId);
        assert activatedUser.isPresent();
        assertEquals(UserStatus.ACTIVE, activatedUser.get().getStatus());
        assertEquals(true, activatedUser.get().getActive());
    }

    @Test
    void activate_WithInvalidToken_ShouldReturnError() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/activate").param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(3009)); // INVALID_TOKEN
    }

    @Test
    void activate_WithAlreadyActiveUser_ShouldReturnError() throws Exception {
        // Given - Create and activate a user
        final var activeUser =
                User.builder()
                        .username(USERNAME)
                        .password(passwordEncoder.encode(PASSWORD))
                        .fullName(FULL_NAME)
                        .email(EMAIL)
                        .roles(Role.MANAGER)
                        .status(UserStatus.ACTIVE)
                        .active(true)
                        .build();
        final var savedUser = userRepository.save(activeUser);

        // Create a fake token in Redis
        final var fakeToken = "fake-token-123";
        redisTemplate.opsForValue().set("activation:token:" + fakeToken, savedUser.getId());

        // When & Then
        mockMvc.perform(get("/api/v1/auth/activate").param("token", fakeToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(3019)); // USER_ALREADY_ACTIVE
    }
}
