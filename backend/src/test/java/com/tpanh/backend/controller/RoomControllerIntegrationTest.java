package com.tpanh.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.RoomCreationRequest;
import com.tpanh.backend.dto.RoomUpdateRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
class RoomControllerIntegrationTest {
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
    private Integer buildingId;

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

        // Create building for room tests
        final var buildingRequest = new BuildingCreationRequest();
        buildingRequest.setName("Trọ Xanh");
        buildingRequest.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        final var buildingResponse =
                mockMvc.perform(
                                post("/api/v1/buildings")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(buildingRequest)))
                        .andReturn();

        final var buildingResponseJson =
                objectMapper.readTree(buildingResponse.getResponse().getContentAsString());
        buildingId = buildingResponseJson.get("result").get("id").asInt();
    }

    @Test
    void createRoom_WithValidRequest_ShouldReturnRoom() throws Exception {
        // Given
        final var request = new RoomCreationRequest();
        request.setBuildingId(buildingId);
        request.setRoomNo("P.101");
        request.setPrice(3000000);
        request.setStatus("VACANT");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/rooms")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.result.buildingId").value(buildingId))
                .andExpect(jsonPath("$.result.roomNo").value("P.101"))
                .andExpect(jsonPath("$.result.price").value(3000000))
                .andExpect(jsonPath("$.result.status").value("VACANT"))
                .andExpect(jsonPath("$.message").value("Thêm phòng thành công"));
    }

    @Test
    void createRoom_WithNullStatus_ShouldUseDefaultVACANT() throws Exception {
        // Given
        final var request = new RoomCreationRequest();
        request.setBuildingId(buildingId);
        request.setRoomNo("P.102");
        request.setPrice(3000000);
        request.setStatus(null);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/rooms")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("VACANT"));
    }

    @Test
    void createRoom_WithInvalidBuildingId_ShouldReturnNotFound() throws Exception {
        // Given
        final var request = new RoomCreationRequest();
        request.setBuildingId(99999);
        request.setRoomNo("P.101");
        request.setPrice(3000000);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/rooms")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void updateRoom_WithValidRequest_ShouldReturnUpdatedRoom() throws Exception {
        // Given - Create room first
        final var createRequest = new RoomCreationRequest();
        createRequest.setBuildingId(buildingId);
        createRequest.setRoomNo("P.101");
        createRequest.setPrice(3000000);

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/rooms")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var roomId = createResponseJson.get("result").get("id").asInt();

        final var updateRequest = new RoomUpdateRequest();
        updateRequest.setPrice(3500000);
        updateRequest.setStatus("OCCUPIED");

        // When & Then
        mockMvc.perform(
                        put("/api/v1/rooms/" + roomId)
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.price").value(3500000))
                .andExpect(jsonPath("$.result.status").value("OCCUPIED"))
                .andExpect(jsonPath("$.message").value("Cập nhật phòng thành công"));
    }

    @Test
    void updateRoom_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        final var updateRequest = new RoomUpdateRequest();
        updateRequest.setPrice(3500000);

        // When & Then
        mockMvc.perform(
                        put("/api/v1/rooms/99999")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void deleteRoom_WithValidId_ShouldDeleteRoom() throws Exception {
        // Given - Create room first
        final var createRequest = new RoomCreationRequest();
        createRequest.setBuildingId(buildingId);
        createRequest.setRoomNo("P.101");
        createRequest.setPrice(3000000);

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/rooms")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var roomId = createResponseJson.get("result").get("id").asInt();

        // When & Then
        mockMvc.perform(
                        delete("/api/v1/rooms/" + roomId)
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRoom_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(
                        delete("/api/v1/rooms/99999")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void getTenantsByRoomId_WithValidRoomId_ShouldReturnTenantList() throws Exception {
        // Given - Create room first
        final var createRequest = new RoomCreationRequest();
        createRequest.setBuildingId(buildingId);
        createRequest.setRoomNo("P.101");
        createRequest.setPrice(3000000);

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/rooms")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var roomId = createResponseJson.get("result").get("id").asInt();

        // When & Then
        mockMvc.perform(
                        get("/api/v1/rooms/" + roomId + "/tenants")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.message").value("Lấy lịch sử khách thành công"));
    }
}
