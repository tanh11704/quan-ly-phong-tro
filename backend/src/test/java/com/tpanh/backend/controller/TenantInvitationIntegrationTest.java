package com.tpanh.backend.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.RoomCreationRequest;
import com.tpanh.backend.dto.TenantInvitationRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.RoomStatus;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.repository.TenantInvitationRepository;
import com.tpanh.backend.repository.TenantRepository;
import com.tpanh.backend.repository.UserRepository;
import java.time.LocalDate;
import java.util.UUID;
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
class TenantInvitationIntegrationTest {

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
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TenantInvitationRepository invitationRepository;
    @Autowired private TenantRepository tenantRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private String managerToken;
    private String userToken;
    private Integer roomId;
    private User normalUser;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .build();

        userRepository.deleteAll();
        invitationRepository.deleteAll();
        tenantRepository.deleteAll();

        // 1. Create Manager
        var manager =
                User.builder()
                        .username("manager")
                        .password(passwordEncoder.encode("password"))
                        .fullName("Manager User")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.MANAGER)))
                        .active(true)
                        .build();
        userRepository.save(manager);
        managerToken = login("manager", "password");

        // 2. Create Normal User (Invitee)
        normalUser =
                User.builder()
                        .username("tenant")
                        .password(passwordEncoder.encode("password"))
                        .fullName("Tenant User")
                        .email("tenant@example.com")
                        .phoneNumber("0999999999")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.USER)))
                        .active(true)
                        .build();
        userRepository.save(normalUser);
        userToken = login("tenant", "password");

        // 3. Create Building & Room
        roomId = createBuildingAndRoom();
    }

    private String login(String username, String password) throws Exception {
        var authRequest = new AuthenticationRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);

        var response =
                mockMvc.perform(
                                post("/api/v1/auth/token")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(authRequest)))
                        .andReturn();

        var json = objectMapper.readTree(response.getResponse().getContentAsString());
        return json.get("result").get("token").asText();
    }

    private Integer createBuildingAndRoom() throws Exception {
        // Create Building
        var buildingRequest = new BuildingCreationRequest();
        buildingRequest.setName("Test Building");
        buildingRequest.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        var buildingRes =
                mockMvc.perform(
                                post("/api/v1/buildings")
                                        .header("Authorization", "Bearer " + managerToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(buildingRequest)))
                        .andReturn();

        var buildingJson = objectMapper.readTree(buildingRes.getResponse().getContentAsString());
        int buildingId = buildingJson.get("result").get("id").asInt();

        // Create Room
        var roomRequest = new RoomCreationRequest();
        roomRequest.setBuildingId(buildingId);
        roomRequest.setRoomNo("101");
        roomRequest.setPrice(5000000);
        roomRequest.setStatus(RoomStatus.VACANT);

        var roomRes =
                mockMvc.perform(
                                post("/api/v1/rooms")
                                        .header("Authorization", "Bearer " + managerToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(roomRequest)))
                        .andReturn();

        var roomJson = objectMapper.readTree(roomRes.getResponse().getContentAsString());
        return roomJson.get("result").get("id").asInt();
    }

    @Test
    void testFullInvitationFlow() throws Exception {
        // 1. Manager invites tenant
        var inviteRequest = new TenantInvitationRequest();
        inviteRequest.setRoomId(roomId);
        inviteRequest.setEmail("tenant@example.com");
        inviteRequest.setContractHolder(true);
        inviteRequest.setContractEndDate(LocalDate.now().plusMonths(6));

        mockMvc.perform(
                        post("/api/tenant-invitations")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Gửi lời mời thành công"));

        // Verify invitation exists in DB
        var invitations = invitationRepository.findAll();
        assert (invitations.size() == 1);
        UUID token = invitations.get(0).getId();

        // 2. Tenant accepts invitation
        mockMvc.perform(
                        post("/api/tenant-invitations/" + token + "/accept")
                                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.result.email").value("tenant@example.com"))
                .andExpect(jsonPath("$.result.name").value("Tenant User"))
                .andExpect(jsonPath("$.result.phone").value("0999999999"));

        // Verify invitation status is ACCEPTED
        var updatedInvitation = invitationRepository.findById(token).orElseThrow();
        assert (updatedInvitation.getStatus() == com.tpanh.backend.enums.InvitationStatus.ACCEPTED);
    }
}
