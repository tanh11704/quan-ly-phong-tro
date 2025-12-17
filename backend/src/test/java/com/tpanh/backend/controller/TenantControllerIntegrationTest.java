package com.tpanh.backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
import com.tpanh.backend.dto.TenantCreationRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.repository.TenantRepository;
import com.tpanh.backend.repository.UserRepository;
import java.time.LocalDate;
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
class TenantControllerIntegrationTest {
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
            new ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .disable(
                            com.fasterxml.jackson.databind.SerializationFeature
                                    .WRITE_DATES_AS_TIMESTAMPS);

    @Autowired private UserRepository userRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String USERNAME = "testmanager";
    private static final String PASSWORD = "testpass123";
    private String authToken;
    private Integer roomId;

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

        // Create building and room for tenant tests
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
        final var buildingId = buildingResponseJson.get("result").get("id").asInt();

        final var roomRequest = new RoomCreationRequest();
        roomRequest.setBuildingId(buildingId);
        roomRequest.setRoomNo("P.101");
        roomRequest.setPrice(3000000);

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
    void createTenant_WithValidRequest_ShouldReturnTenant() throws Exception {
        // Given
        final var request = new TenantCreationRequest();
        request.setRoomId(roomId);
        request.setName("Nguyễn Văn A");
        request.setPhone("0901234567");
        request.setIsContractHolder(true);
        request.setStartDate(LocalDate.now());

        // When & Then
        mockMvc.perform(
                        post("/api/v1/tenants")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.result.roomId").value(roomId))
                .andExpect(jsonPath("$.result.name").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.result.phone").value("0901234567"))
                .andExpect(jsonPath("$.result.isContractHolder").value(true))
                .andExpect(jsonPath("$.result.startDate").exists())
                .andExpect(jsonPath("$.message").value("Thêm khách thuê thành công"));
    }

    @Test
    void createTenant_WithNullStartDate_ShouldUseCurrentDate() throws Exception {
        // Given
        final var request = new TenantCreationRequest();
        request.setRoomId(roomId);
        request.setName("Nguyễn Văn B");
        request.setStartDate(null);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/tenants")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.startDate").exists());
    }

    @Test
    void createTenant_WithInvalidRoomId_ShouldReturnNotFound() throws Exception {
        // Given
        final var request = new TenantCreationRequest();
        request.setRoomId(99999);
        request.setName("Nguyễn Văn A");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/tenants")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void createTenant_WithVACANTRoom_ShouldUpdateRoomStatusToOCCUPIED() throws Exception {
        // Given - Clean up existing tenants first
        tenantRepository.deleteAll();
        tenantRepository.flush();

        final var request = new TenantCreationRequest();
        request.setRoomId(roomId);
        request.setName("Nguyễn Văn A");

        // When
        mockMvc.perform(
                        post("/api/v1/tenants")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then - Verify room status is updated
        mockMvc.perform(
                        get("/api/v1/rooms/" + roomId + "/tenants")
                                .with(user("testmanager").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[?(@.name == 'Nguyễn Văn A')]").exists());
    }

    @Test
    void getTenantById_WithValidId_ShouldReturnTenant() throws Exception {
        // Given - Create tenant first
        final var createRequest = new TenantCreationRequest();
        createRequest.setRoomId(roomId);
        createRequest.setName("Nguyễn Văn A");

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/tenants")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var tenantId = createResponseJson.get("result").get("id").asInt();

        // When & Then
        mockMvc.perform(
                        get("/api/v1/tenants/" + tenantId)
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(tenantId))
                .andExpect(jsonPath("$.result.name").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.message").value("Lấy thông tin khách thành công"));
    }

    @Test
    void getTenantById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tenants/99999").header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void getTenantsByRoomId_WithValidRoomId_ShouldReturnTenantList() throws Exception {
        // Given - Clean up existing tenants first
        tenantRepository.deleteAll();
        tenantRepository.flush();

        // Create tenants first
        final var tenant1Request = new TenantCreationRequest();
        tenant1Request.setRoomId(roomId);
        tenant1Request.setName("Nguyễn Văn A");
        tenant1Request.setStartDate(LocalDate.now());

        final var tenant2Request = new TenantCreationRequest();
        tenant2Request.setRoomId(roomId);
        tenant2Request.setName("Nguyễn Văn B");
        tenant2Request.setStartDate(LocalDate.now().minusDays(10));

        mockMvc.perform(
                        post("/api/v1/tenants")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenant1Request)))
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/tenants")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenant2Request)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(
                        get("/api/v1/rooms/" + roomId + "/tenants")
                                .with(user("testmanager").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(
                        jsonPath("$.content[0].name")
                                .value("Nguyễn Văn A")) // Sorted by startDate desc
                .andExpect(jsonPath("$.message").value("Lấy danh sách khách thuê thành công"));
    }

    @Test
    void endTenantContract_WithValidId_ShouldSetEndDate() throws Exception {
        // Given - Create tenant first
        final var createRequest = new TenantCreationRequest();
        createRequest.setRoomId(roomId);
        createRequest.setName("Nguyễn Văn A");

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/tenants")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var tenantId = createResponseJson.get("result").get("id").asInt();

        // When & Then
        mockMvc.perform(
                        put("/api/v1/tenants/" + tenantId + "/end")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.endDate").exists())
                .andExpect(jsonPath("$.message").value("Kết thúc hợp đồng thành công"));
    }

    @Test
    void endTenantContract_WithAlreadyEndedContract_ShouldReturnBadRequest() throws Exception {
        // Given - Create and end tenant first
        final var createRequest = new TenantCreationRequest();
        createRequest.setRoomId(roomId);
        createRequest.setName("Nguyễn Văn A");

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/tenants")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var tenantId = createResponseJson.get("result").get("id").asInt();

        // End contract first time
        mockMvc.perform(
                put("/api/v1/tenants/" + tenantId + "/end")
                        .header("Authorization", "Bearer " + authToken));

        // When & Then - Try to end again
        mockMvc.perform(
                        put("/api/v1/tenants/" + tenantId + "/end")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void endTenantContract_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(
                        put("/api/v1/tenants/99999/end")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void endTenantContract_WithLastTenant_ShouldUpdateRoomStatusToVACANT() throws Exception {
        // Given - Create tenant
        final var createRequest = new TenantCreationRequest();
        createRequest.setRoomId(roomId);
        createRequest.setName("Nguyễn Văn A");

        final var createResponse =
                mockMvc.perform(
                                post("/api/v1/tenants")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                        .andReturn();

        final var createResponseJson =
                objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var tenantId = createResponseJson.get("result").get("id").asInt();

        // When - End contract
        mockMvc.perform(
                        put("/api/v1/tenants/" + tenantId + "/end")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Then - Verify tenant has end date
        mockMvc.perform(
                        get("/api/v1/tenants/" + tenantId)
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.endDate").exists());
    }
}
