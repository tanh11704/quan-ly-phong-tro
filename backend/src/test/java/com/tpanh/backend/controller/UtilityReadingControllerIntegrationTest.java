package com.tpanh.backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpanh.backend.dto.UtilityReadingCreationRequest;
import com.tpanh.backend.dto.UtilityReadingUpdateRequest;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.entity.UtilityReading;
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

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String managerId;
    private Integer buildingId;
    private Integer roomId;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .build();

        utilityReadingRepository.deleteAll();
        roomRepository.deleteAll();
        buildingRepository.deleteAll();
        userRepository.deleteAll();

        final var manager =
                User.builder()
                        .username("manager")
                        .password("pass")
                        .fullName("Test Manager")
                        .roles(Role.MANAGER)
                        .active(true)
                        .build();
        final var savedManager = userRepository.save(manager);
        managerId = savedManager.getId();

        final var building = new Building();
        building.setName("Trọ Xanh");
        building.setWaterCalcMethod(WaterCalcMethod.BY_METER);
        building.setElecUnitPrice(3000);
        building.setWaterUnitPrice(20000);
        building.setManager(savedManager);
        buildingId = buildingRepository.save(building).getId();

        final var room = new Room();
        room.setBuilding(building);
        room.setRoomNo("P.101");
        room.setPrice(3000000);
        room.setStatus(RoomStatus.OCCUPIED);
        roomId = roomRepository.save(room).getId();
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
                                .with(user(managerId).roles("MANAGER"))
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
        final var reading = new UtilityReading();
        reading.setRoom(roomRepository.findById(roomId).orElseThrow());
        reading.setMonth("2025-01");
        reading.setElectricIndex(100);
        reading.setWaterIndex(50);
        final var saved = utilityReadingRepository.save(reading);

        final var req = new UtilityReadingUpdateRequest();
        req.setElectricIndex(120);

        mockMvc.perform(
                        put("/api/v1/utility-readings/" + saved.getId())
                                .with(user(managerId).roles("MANAGER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.electricIndex").value(120))
                .andExpect(jsonPath("$.message").value("Cập nhật chỉ số điện nước thành công"));
    }

    @Test
    void getUtilityReadingById_shouldReturnOk() throws Exception {
        final var reading = new UtilityReading();
        reading.setRoom(roomRepository.findById(roomId).orElseThrow());
        reading.setMonth("2025-01");
        reading.setElectricIndex(100);
        final var saved = utilityReadingRepository.save(reading);

        mockMvc.perform(
                        get("/api/v1/utility-readings/" + saved.getId())
                                .with(user(managerId).roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(saved.getId()))
                .andExpect(
                        jsonPath("$.message").value("Lấy thông tin chỉ số điện nước thành công"));
    }

    @Test
    void getUtilityReadingsByRoomId_shouldReturnOk() throws Exception {
        final var reading = new UtilityReading();
        reading.setRoom(roomRepository.findById(roomId).orElseThrow());
        reading.setMonth("2025-01");
        reading.setElectricIndex(100);
        utilityReadingRepository.save(reading);

        mockMvc.perform(
                        get("/api/v1/utility-readings/rooms/" + roomId)
                                .with(user(managerId).roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.message").value("Lấy lịch sử chỉ số điện nước thành công"));
    }

    @Test
    void getUtilityReadingsByBuildingAndMonth_shouldReturnOk() throws Exception {
        final var reading = new UtilityReading();
        reading.setRoom(roomRepository.findById(roomId).orElseThrow());
        reading.setMonth("2025-01");
        reading.setElectricIndex(100);
        utilityReadingRepository.save(reading);

        mockMvc.perform(
                        get("/api/v1/utility-readings/buildings/" + buildingId)
                                .with(user(managerId).roles("MANAGER"))
                                .param("month", "2025-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(
                        jsonPath("$.message").value("Lấy danh sách chỉ số điện nước thành công"));
    }
}
