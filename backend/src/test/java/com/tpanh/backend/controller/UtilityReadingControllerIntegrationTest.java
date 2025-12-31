package com.tpanh.backend.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.RoomCreationRequest;
import com.tpanh.backend.dto.UtilityReadingCreationRequest;
import com.tpanh.backend.dto.UtilityReadingUpdateRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.RoomStatus;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.UserRepository;
import com.tpanh.backend.repository.UtilityReadingRepository;
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
class UtilityReadingControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UserRepository userRepository;
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private UtilityReadingRepository utilityReadingRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .disable(
                            com.fasterxml.jackson.databind.SerializationFeature
                                    .WRITE_DATES_AS_TIMESTAMPS);

    private static final String USERNAME = "testmanager";
    private static final String PASSWORD = "testpass123";
    private String authToken;
    private Integer buildingId;
    private Integer roomId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .build();

        utilityReadingRepository.deleteAll();
        roomRepository.deleteAll();
        buildingRepository.deleteAll();
        userRepository.deleteAll();

        // Create user with encoded password
        final var manager =
                User.builder()
                        .username(USERNAME)
                        .password(passwordEncoder.encode(PASSWORD))
                        .fullName("Test Manager")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.MANAGER)))
                        .active(true)
                        .build();
        userRepository.save(manager);

        // Authenticate and get JWT token
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

        // Create building via API to set up proper manager relationship
        final var buildingRequest = new BuildingCreationRequest();
        buildingRequest.setName("Trọ Xanh");
        buildingRequest.setWaterCalcMethod(WaterCalcMethod.BY_METER);
        buildingRequest.setElecUnitPrice(3000);
        buildingRequest.setWaterUnitPrice(20000);

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

        // Create room via API
        final var roomRequest = new RoomCreationRequest();
        roomRequest.setBuildingId(buildingId);
        roomRequest.setRoomNo("P.101");
        roomRequest.setPrice(3000000);
        roomRequest.setStatus(RoomStatus.OCCUPIED);

        final var roomResponse =
                mockMvc.perform(
                                post("/api/v1/rooms")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(roomRequest)))
                        .andReturn();

        final var roomResponseJson =
                objectMapper.readTree(roomResponse.getResponse().getContentAsString());
        roomId = roomResponseJson.get("result").get("id").asInt();
    }

    @Test
    void createUtilityReading_shouldReturnCreated() throws Exception {
        final var req = new UtilityReadingCreationRequest();
        req.setRoomId(roomId);
        req.setMonth("2025-01");
        req.setElectricIndex(100);
        req.setWaterIndex(50);
        req.setImageEvidence("http://img");

        mockMvc.perform(
                        post("/api/v1/utility-readings")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.result.roomId").value(roomId))
                .andExpect(jsonPath("$.result.month").value("2025-01"))
                .andExpect(jsonPath("$.message").value("Ghi chỉ số điện nước thành công"));
    }

    @Test
    void updateUtilityReading_shouldReturnOk() throws Exception {
        // Create via API first
        final var createReq = new UtilityReadingCreationRequest();
        createReq.setRoomId(roomId);
        createReq.setMonth("2025-01");
        createReq.setElectricIndex(100);
        createReq.setWaterIndex(50);

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/utility-readings")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createReq)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var readingId = createResponseJson.get("result").get("id").asInt();

        final var req = new UtilityReadingUpdateRequest();
        req.setElectricIndex(120);

        mockMvc.perform(
                        put("/api/v1/utility-readings/" + readingId)
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.electricIndex").value(120))
                .andExpect(jsonPath("$.message").value("Cập nhật chỉ số điện nước thành công"));
    }

    @Test
    void getUtilityReadingById_shouldReturnOk() throws Exception {
        // Create via API first
        final var createReq = new UtilityReadingCreationRequest();
        createReq.setRoomId(roomId);
        createReq.setMonth("2025-01");
        createReq.setElectricIndex(100);
        createReq.setWaterIndex(50);

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/utility-readings")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createReq)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var readingId = createResponseJson.get("result").get("id").asInt();

        mockMvc.perform(
                        get("/api/v1/utility-readings/" + readingId)
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(readingId))
                .andExpect(
                        jsonPath("$.message").value("Lấy thông tin chỉ số điện nước thành công"));
    }

    @Test
    void getUtilityReadingsByRoomId_shouldReturnOk() throws Exception {
        // Create via API first
        final var createReq = new UtilityReadingCreationRequest();
        createReq.setRoomId(roomId);
        createReq.setMonth("2025-01");
        createReq.setElectricIndex(100);
        createReq.setWaterIndex(50);

        mockMvc.perform(
                        post("/api/v1/utility-readings")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/utility-readings/rooms/" + roomId)
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.message").value("Lấy lịch sử chỉ số điện nước thành công"));
    }

    @Test
    void getUtilityReadingsByBuildingAndMonth_shouldReturnOk() throws Exception {
        // Create via API first
        final var createReq = new UtilityReadingCreationRequest();
        createReq.setRoomId(roomId);
        createReq.setMonth("2025-01");
        createReq.setElectricIndex(100);
        createReq.setWaterIndex(50);

        mockMvc.perform(
                        post("/api/v1/utility-readings")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/utility-readings/buildings/" + buildingId)
                                .header("Authorization", "Bearer " + authToken)
                                .param("month", "2025-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(
                        jsonPath("$.message").value("Lấy danh sách chỉ số điện nước thành công"));
    }
}
