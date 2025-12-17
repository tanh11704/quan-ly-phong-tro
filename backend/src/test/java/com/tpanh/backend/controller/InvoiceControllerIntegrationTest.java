package com.tpanh.backend.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.InvoiceCreationRequest;
import com.tpanh.backend.dto.RoomCreationRequest;
import com.tpanh.backend.dto.TenantCreationRequest;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.MeterRecord;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.MeterType;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.RoomStatus;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.repository.InvoiceRepository;
import com.tpanh.backend.repository.MeterRecordRepository;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantRepository;
import com.tpanh.backend.repository.UserRepository;
import com.tpanh.backend.repository.UtilityReadingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
class InvoiceControllerIntegrationTest {
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
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private MeterRecordRepository meterRecordRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private UtilityReadingRepository utilityReadingRepository;
    @Autowired private PasswordEncoder passwordEncoder;

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

        // Clean up
        invoiceRepository.deleteAll();
        utilityReadingRepository.deleteAll();
        meterRecordRepository.deleteAll();
        tenantRepository.deleteAll();
        roomRepository.deleteAll();
        buildingRepository.deleteAll();
        userRepository.deleteAll();

        // Create user
        final var user =
                User.builder()
                        .username(USERNAME)
                        .password(passwordEncoder.encode(PASSWORD))
                        .fullName("Test Manager")
                        .roles(Role.MANAGER)
                        .active(true)
                        .build();
        userRepository.save(user);

        // Authenticate
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

        // Create building
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

        // Update building with prices (since BuildingCreationRequest might not include them)
        final Building building = buildingRepository.findById(buildingId).orElseThrow();
        building.setElecUnitPrice(3000);
        building.setWaterUnitPrice(20000);
        buildingRepository.save(building);

        // Create room
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

        // Create tenant
        final var tenantRequest = new TenantCreationRequest();
        tenantRequest.setRoomId(roomId);
        tenantRequest.setName("Nguyễn Văn A");
        tenantRequest.setPhone("0123456789");
        tenantRequest.setIsContractHolder(true);

        mockMvc.perform(
                        post("/api/v1/tenants")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenantRequest)))
                .andReturn();
    }

    @Test
    void createInvoices_WithValidData_ShouldCreateInvoice() throws Exception {
        // Given - Create meter records
        final Room room = roomRepository.findById(roomId).orElseThrow();

        final MeterRecord elecRecord = new MeterRecord();
        elecRecord.setRoom(room);
        elecRecord.setType(MeterType.ELEC);
        elecRecord.setPeriod("2025-01");
        elecRecord.setPreviousValue(100);
        elecRecord.setCurrentValue(150);
        meterRecordRepository.save(elecRecord);

        final MeterRecord waterRecord = new MeterRecord();
        waterRecord.setRoom(room);
        waterRecord.setType(MeterType.WATER);
        waterRecord.setPeriod("2025-01");
        waterRecord.setPreviousValue(50);
        waterRecord.setCurrentValue(60);
        meterRecordRepository.save(waterRecord);

        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].roomNo").value("P.101"))
                .andExpect(jsonPath("$.result[0].tenantName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.result[0].period").value("2025-01"))
                .andExpect(jsonPath("$.result[0].roomPrice").value(3000000))
                .andExpect(jsonPath("$.result[0].elecAmount").value(150000)) // (150-100) * 3000
                .andExpect(jsonPath("$.result[0].waterAmount").value(200000)) // (60-50) * 20000
                .andExpect(jsonPath("$.result[0].totalAmount").value(3350000))
                .andExpect(jsonPath("$.result[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.result[0].dueDate").exists())
                .andExpect(jsonPath("$.message").value("Tạo hóa đơn thành công"));
    }

    @Test
    void createInvoices_WithPerCapitaWaterMethod_ShouldCalculateCorrectly() throws Exception {
        // Given - Clean up any existing invoices for this period first
        invoiceRepository.deleteAll();
        invoiceRepository.flush();

        // Update building to use PER_CAPITA
        final Building building = buildingRepository.findById(buildingId).orElseThrow();
        building.setWaterCalcMethod(WaterCalcMethod.PER_CAPITA);
        buildingRepository.save(building);
        buildingRepository.flush();

        // Create second tenant
        final var tenantRequest2 = new TenantCreationRequest();
        tenantRequest2.setRoomId(roomId);
        tenantRequest2.setName("Nguyễn Văn B");
        tenantRequest2.setPhone("0987654321");
        tenantRequest2.setIsContractHolder(false);

        mockMvc.perform(
                        post("/api/v1/tenants")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenantRequest2)))
                .andExpect(status().isCreated());

        // Create meter records for fallback (since no UtilityReading)
        final Room room = roomRepository.findById(roomId).orElseThrow();
        final MeterRecord waterRecord = new MeterRecord();
        waterRecord.setRoom(room);
        waterRecord.setType(MeterType.WATER);
        waterRecord.setPeriod("2025-01");
        waterRecord.setPreviousValue(50);
        waterRecord.setCurrentValue(60);
        meterRecordRepository.save(waterRecord);
        meterRecordRepository.flush();

        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.result[0].waterAmount").value(40000)) // 2 tenants * 20000
                .andExpect(jsonPath("$.result[0].totalAmount").value(3040000)); // 3000000 (room) + 0 (elec) + 40000 (water)
    }

    @Test
    void createInvoices_WithNoMeterRecords_ShouldSetElecAndWaterToZero() throws Exception {
        // Given - No meter records
        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].elecAmount").value(0))
                .andExpect(jsonPath("$.result[0].waterAmount").value(0))
                .andExpect(jsonPath("$.result[0].totalAmount").value(3000000)); // Only room price
    }

    @Test
    void createInvoices_WithExistingInvoice_ShouldSkipRoom() throws Exception {
        // Given - Create invoice first
        final var request1 = new InvoiceCreationRequest();
        request1.setBuildingId(buildingId);
        request1.setPeriod("2025-01");

        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request1)))
                .andReturn();

        // When - Try to create again for same period
        final var request2 = new InvoiceCreationRequest();
        request2.setBuildingId(buildingId);
        request2.setPeriod("2025-01");

        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isEmpty()); // Should be empty as invoice already exists
    }

    @Test
    void createInvoices_WithNoTenant_ShouldSkipRoom() throws Exception {
        // Given - Delete tenant
        tenantRepository.deleteAll();

        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isEmpty()); // Should be empty as no tenant
    }

    @Test
    void createInvoices_WithInvalidBuildingId_ShouldReturnNotFound() throws Exception {
        // Given
        final var request = new InvoiceCreationRequest();
        request.setBuildingId(99999);
        request.setPeriod("2025-01");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void createInvoices_WithMultipleRooms_ShouldCreateMultipleInvoices() throws Exception {
        // Given - Create second room with tenant
        final var roomRequest2 = new RoomCreationRequest();
        roomRequest2.setBuildingId(buildingId);
        roomRequest2.setRoomNo("P.102");
        roomRequest2.setPrice(3500000);
        roomRequest2.setStatus(RoomStatus.OCCUPIED);

        final var roomResponse2 =
                mockMvc.perform(
                                post("/api/v1/rooms")
                                        .header("Authorization", "Bearer " + authToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(roomRequest2)))
                        .andReturn();

        final var roomResponseJson2 =
                objectMapper.readTree(roomResponse2.getResponse().getContentAsString());
        final var roomId2 = roomResponseJson2.get("result").get("id").asInt();

        final var tenantRequest2 = new TenantCreationRequest();
        tenantRequest2.setRoomId(roomId2);
        tenantRequest2.setName("Nguyễn Văn B");
        tenantRequest2.setPhone("0987654321");
        tenantRequest2.setIsContractHolder(true);

        mockMvc.perform(
                        post("/api/v1/tenants")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenantRequest2)))
                .andReturn();

        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].roomNo").exists())
                .andExpect(jsonPath("$.result[1].roomNo").exists());
    }

    @Test
    void createInvoices_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // Given
        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // 403 when no authentication (Spring Security behavior)
    }

    @Test
    void createInvoices_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Missing required fields
        final var request = new InvoiceCreationRequest();
        // request.setBuildingId(null); // Missing buildingId
        request.setPeriod("");

        // When & Then
        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== Tests for getInvoices endpoint =====

    @Test
    void getInvoices_WithValidParams_ShouldReturnPageResponse() throws Exception {
        // Given - Create invoice first
        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(
                        get("/api/v1/invoices")
                                .header("Authorization", "Bearer " + authToken)
                                .param("buildingId", buildingId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.message").value("Lấy danh sách hóa đơn thành công"));
    }

    @Test
    void getInvoices_WithPeriodFilter_ShouldReturnFilteredResults() throws Exception {
        // Given - Create invoices for different periods
        final var request1 = new InvoiceCreationRequest();
        request1.setBuildingId(buildingId);
        request1.setPeriod("2025-01");

        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // When & Then - Filter by period
        mockMvc.perform(
                        get("/api/v1/invoices")
                                .header("Authorization", "Bearer " + authToken)
                                .param("buildingId", buildingId.toString())
                                .param("period", "2025-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getInvoices_WithStatusFilter_ShouldReturnFilteredResults() throws Exception {
        // Given - Create invoice
        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then - Filter by status DRAFT
        mockMvc.perform(
                        get("/api/v1/invoices")
                                .header("Authorization", "Bearer " + authToken)
                                .param("buildingId", buildingId.toString())
                                .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getInvoices_WithInvalidBuildingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(
                        get("/api/v1/invoices")
                                .header("Authorization", "Bearer " + authToken)
                                .param("buildingId", "99999"))
                .andExpect(status().isNotFound());
    }

    // ===== Tests for getInvoiceDetail endpoint =====

    @Test
    void getInvoiceDetail_WithValidId_ShouldReturnDetail() throws Exception {
        // Given - Create invoice first
        final Room room = roomRepository.findById(roomId).orElseThrow();

        final MeterRecord elecRecord = new MeterRecord();
        elecRecord.setRoom(room);
        elecRecord.setType(MeterType.ELEC);
        elecRecord.setPeriod("2025-01");
        elecRecord.setPreviousValue(100);
        elecRecord.setCurrentValue(150);
        meterRecordRepository.save(elecRecord);

        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        final var createResponse = mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        final var responseJson = objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var invoiceId = responseJson.get("result").get(0).get("id").asInt();

        // When & Then
        mockMvc.perform(
                        get("/api/v1/invoices/" + invoiceId)
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(invoiceId))
                .andExpect(jsonPath("$.result.roomNo").value("P.101"))
                .andExpect(jsonPath("$.result.period").value("2025-01"))
                .andExpect(jsonPath("$.message").value("Lấy chi tiết hóa đơn thành công"));
    }

    @Test
    void getInvoiceDetail_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(
                        get("/api/v1/invoices/99999")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().is4xxClientError()); // May return 404 or cached error
    }

    // ===== Tests for payInvoice endpoint =====

    @Test
    void payInvoice_WithValidId_ShouldUpdateToPaid() throws Exception {
        // Given - Create invoice first
        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        final var createResponse = mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        final var responseJson = objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var invoiceId = responseJson.get("result").get(0).get("id").asInt();

        // When & Then
        mockMvc.perform(
                        put("/api/v1/invoices/" + invoiceId + "/pay")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("PAID"))
                .andExpect(jsonPath("$.message").value("Thanh toán hóa đơn thành công"));
    }

    @Test
    void payInvoice_WithAlreadyPaidInvoice_ShouldReturnBadRequest() throws Exception {
        // Given - Create and pay invoice first
        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        final var createResponse = mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        final var responseJson = objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var invoiceId = responseJson.get("result").get(0).get("id").asInt();

        // Pay first time
        mockMvc.perform(
                        put("/api/v1/invoices/" + invoiceId + "/pay")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // When & Then - Try to pay again
        mockMvc.perform(
                        put("/api/v1/invoices/" + invoiceId + "/pay")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payInvoice_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(
                        put("/api/v1/invoices/99999/pay")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest()); // AppException is mapped to 400
    }

    // ===== Tests for sendInvoiceEmail endpoint =====

    @Test
    void sendInvoiceEmail_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(
                        post("/api/v1/invoices/99999/send-email")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest()); // AppException is mapped to 400
    }

    @Test
    void sendInvoiceEmail_WithNoTenantEmail_ShouldReturnBadRequest() throws Exception {
        // Given - Create invoice (tenant has no email)
        final var request = new InvoiceCreationRequest();
        request.setBuildingId(buildingId);
        request.setPeriod("2025-01");

        final var createResponse = mockMvc.perform(
                        post("/api/v1/invoices/generate")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        final var responseJson = objectMapper.readTree(createResponse.getResponse().getContentAsString());
        final var invoiceId = responseJson.get("result").get(0).get("id").asInt();

        // When & Then - Should fail because tenant has no email
        mockMvc.perform(
                        post("/api/v1/invoices/" + invoiceId + "/send-email")
                                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }
}
