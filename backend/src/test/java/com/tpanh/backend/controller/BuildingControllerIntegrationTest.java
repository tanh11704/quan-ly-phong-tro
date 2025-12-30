package com.tpanh.backend.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.BuildingUpdateRequest;
import com.tpanh.backend.dto.RoomCreationRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.RoomStatus;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
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

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private static final String USERNAME = "testmanager";
    private static final String PASSWORD = "testpass123";
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .build();

        userRepository.deleteAll();

        final var user =
                User.builder()
                        .username(USERNAME)
                        .password(passwordEncoder.encode(PASSWORD))
                        .fullName("Test Manager")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.MANAGER)))
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
                .andExpect(status().isCreated())
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.elecUnitPrice").value(3500))
                .andExpect(jsonPath("$.result.waterUnitPrice").value(20000));
    }

    @Test
    void createBuilding_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // Given
        final var request = new BuildingCreationRequest();
        request.setName("Trọ Xanh");
        request.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/buildings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
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
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.message").value("Lấy danh sách phòng thành công"));
    }

    // ===== Tests for getBuildings (list) endpoint =====

    @Test
    void getBuildings_WithPagination_ShouldReturnPageResponse() throws Exception {
        // Given - Create buildings
        for (int i = 1; i <= 3; i++) {
            final var request = new BuildingCreationRequest();
            request.setName("Trọ " + i);
            request.setWaterCalcMethod(WaterCalcMethod.BY_METER);

            mockMvc.perform(
                            post("/api/v1/buildings")
                                    .header("Authorization", "Bearer " + authToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When & Then
        mockMvc.perform(
                        get("/api/v1/buildings")
                                .header("Authorization", "Bearer " + authToken)
                                .param("page", "0")
                                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.page.page").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.message").value("Lấy danh sách tòa nhà thành công"));
    }

    @Test
    void getBuildings_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/buildings")).andExpect(status().isUnauthorized());
    }

    // ===== Tests for updateBuilding endpoint =====

    @Test
    void updateBuilding_WithValidRequest_ShouldUpdateBuilding() throws Exception {
        // Given - Create building first
        final var createRequest = new BuildingCreationRequest();
        createRequest.setName("Trọ Cũ");
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

        // Update
        final var updateRequest = new BuildingUpdateRequest();
        updateRequest.setName("Trọ Mới");
        updateRequest.setOwnerName("Nguyễn Văn Mới");
        updateRequest.setElecUnitPrice(4000);
        updateRequest.setWaterUnitPrice(25000);
        updateRequest.setWaterCalcMethod(WaterCalcMethod.PER_CAPITA);

        // When & Then
        mockMvc.perform(
                        put("/api/v1/buildings/" + buildingId)
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Trọ Mới"))
                .andExpect(jsonPath("$.result.ownerName").value("Nguyễn Văn Mới"))
                .andExpect(jsonPath("$.result.elecUnitPrice").value(4000))
                .andExpect(jsonPath("$.result.waterUnitPrice").value(25000))
                .andExpect(jsonPath("$.result.waterCalcMethod").value("PER_CAPITA"))
                .andExpect(jsonPath("$.message").value("Cập nhật tòa nhà thành công"));
    }

    @Test
    void updateBuilding_WithInvalidId_ShouldReturnNotFound() throws Exception {
        final var updateRequest = new BuildingUpdateRequest();
        updateRequest.setName("Trọ Mới");

        mockMvc.perform(
                        put("/api/v1/buildings/99999")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBuilding_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        final var updateRequest = new BuildingUpdateRequest();
        updateRequest.setName("Trọ Mới");

        mockMvc.perform(
                        put("/api/v1/buildings/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ===== Tests for deleteBuilding endpoint =====

    @Test
    void deleteBuilding_WithValidId_ShouldDeleteBuilding() throws Exception {
        // Given - Create building first
        final var createRequest = new BuildingCreationRequest();
        createRequest.setName("Trọ Sẽ Xóa");
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
                        delete("/api/v1/buildings/" + buildingId)
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa tòa nhà thành công"));

        // Verify deletion
        mockMvc.perform(
                        get("/api/v1/buildings/" + buildingId)
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBuilding_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/buildings/99999")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBuilding_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/buildings/1")).andExpect(status().isUnauthorized());
    }

    // ===== Tests for getRoomsByBuildingId with status filter =====

    @Test
    void getRoomsByBuildingId_WithStatusFilter_ShouldReturnFilteredRooms() throws Exception {
        // Given - Create building
        final var createRequest = new BuildingCreationRequest();
        createRequest.setName("Trọ Test");
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

        // Create rooms with different statuses
        final var vacantRoom = new RoomCreationRequest();
        vacantRoom.setBuildingId(buildingId);
        vacantRoom.setRoomNo("P.101");
        vacantRoom.setPrice(3000000);
        vacantRoom.setStatus(RoomStatus.VACANT);

        mockMvc.perform(
                        post("/api/v1/rooms")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(vacantRoom)))
                .andExpect(status().isCreated());

        final var occupiedRoom = new RoomCreationRequest();
        occupiedRoom.setBuildingId(buildingId);
        occupiedRoom.setRoomNo("P.102");
        occupiedRoom.setPrice(3500000);
        occupiedRoom.setStatus(RoomStatus.OCCUPIED);

        mockMvc.perform(
                        post("/api/v1/rooms")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(occupiedRoom)))
                .andExpect(status().isCreated());

        // When & Then - Filter by VACANT status
        mockMvc.perform(
                        get("/api/v1/buildings/" + buildingId + "/rooms")
                                .header("Authorization", "Bearer " + authToken)
                                .param("status", "VACANT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("VACANT"));
    }

    @Test
    void getRoomsByBuildingId_WithInvalidBuildingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(
                        get("/api/v1/buildings/99999/rooms")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}
