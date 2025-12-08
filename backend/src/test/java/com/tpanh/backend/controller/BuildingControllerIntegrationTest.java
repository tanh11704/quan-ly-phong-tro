package com.tpanh.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.WaterCalcMethod;
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
class BuildingControllerIntegrationTest {
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

    private static final String USERNAME = "testmanager";
    private static final String PASSWORD = "testpass123";
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        final var user =
                User.builder()
                        .username(USERNAME)
                        .password(passwordEncoder.encode(PASSWORD))
                        .fullName("Test Manager")
                        .roles(Role.MANAGER)
                        .active(true)
                        .build();
        userRepository.save(user);

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
        authToken = authResponseJson.get("result").get("token").asText();
    }

    @Test
    void createBuilding_WithValidRequest_ShouldReturnBuilding() throws Exception {
        // Given
        final var request = new BuildingCreationRequest();
        request.setName("Trọ Xanh");
        request.setOwnerName("Nguyễn Văn Chủ");
        request.setOwnerPhone("0909123456");
        request.setElecUnitPrice(3500);
        request.setWaterUnitPrice(20000);
        request.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/buildings")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.result.name").value("Trọ Xanh"))
                .andExpect(jsonPath("$.result.ownerName").value("Nguyễn Văn Chủ"))
                .andExpect(jsonPath("$.result.elecUnitPrice").value(3500))
                .andExpect(jsonPath("$.result.waterUnitPrice").value(20000))
                .andExpect(jsonPath("$.result.waterCalcMethod").value("BY_METER"))
                .andExpect(jsonPath("$.message").value("Tạo tòa nhà thành công"));
    }

    @Test
    void createBuilding_WithDefaultPrices_ShouldUseDefaults() throws Exception {
        // Given
        final var request = new BuildingCreationRequest();
        request.setName("Trọ Xanh 2");
        request.setWaterCalcMethod(WaterCalcMethod.PER_CAPITA);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/buildings")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.elecUnitPrice").value(3500))
                .andExpect(jsonPath("$.result.waterUnitPrice").value(20000));
    }

    @Test
    void createBuilding_WithoutAuth_ShouldReturnForbidden() throws Exception {
        // Given
        final var request = new BuildingCreationRequest();
        request.setName("Trọ Xanh");
        request.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/buildings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBuilding_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        final var request = new BuildingCreationRequest();
        request.setName(""); // Invalid: empty name
        request.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/buildings")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBuildingById_WithValidId_ShouldReturnBuilding() throws Exception {
        // Given - Create building first
        final var createRequest = new BuildingCreationRequest();
        createRequest.setName("Trọ Xanh");
        createRequest.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/buildings")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var buildingId = createResponseJson.get("result").get("id").asInt();

        // When & Then
        mockMvc.perform(
                        get("/api/v1/buildings/" + buildingId)
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(buildingId))
                .andExpect(jsonPath("$.result.name").value("Trọ Xanh"))
                .andExpect(jsonPath("$.message").value("Lấy thông tin tòa nhà thành công"));
    }

    @Test
    void getBuildingById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(
                        get("/api/v1/buildings/99999")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void getRoomsByBuildingId_WithValidId_ShouldReturnRoomList() throws Exception {
        // Given - Create building first
        final var createRequest = new BuildingCreationRequest();
        createRequest.setName("Trọ Xanh");
        createRequest.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/buildings")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var buildingId = createResponseJson.get("result").get("id").asInt();

        // When & Then
        mockMvc.perform(
                        get("/api/v1/buildings/" + buildingId + "/rooms")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.message").value("Lấy danh sách phòng thành công"));
    }
}
