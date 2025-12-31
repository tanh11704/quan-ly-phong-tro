package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpanh.backend.dto.TenantCreationRequest;
import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.dto.TenantUpdateRequest;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.Tenant;
import com.tpanh.backend.enums.RoomStatus;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.TenantMapper;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantRepository;
import com.tpanh.backend.security.CurrentUser;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {
    private static final int ROOM_ID = 1;
    private static final int TENANT_ID = 100;
    private static final String MANAGER_ID = "manager-123";
    private static final String TENANT_NAME = "Nguyễn Văn A";
    private static final String TENANT_PHONE = "0987654321";
    private static final String TENANT_EMAIL = "a@example.com";

    @Mock private TenantRepository tenantRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private TenantMapper tenantMapper;
    @Mock private CurrentUser currentUser;

    @InjectMocks private TenantService tenantService;

    private Room room;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(ROOM_ID);
        room.setRoomNo("101");
        room.setStatus(RoomStatus.VACANT);

        tenant = new Tenant();
        tenant.setId(TENANT_ID);
        tenant.setName(TENANT_NAME);
        tenant.setPhone(TENANT_PHONE);
        tenant.setEmail(TENANT_EMAIL);
        tenant.setRoom(room);
        tenant.setStartDate(LocalDate.now());

        // Mock mapper leniently
        lenient()
                .when(tenantMapper.toResponse(any(Tenant.class)))
                .thenAnswer(
                        invocation -> {
                            final Tenant t = invocation.getArgument(0);
                            return TenantResponse.builder()
                                    .id(t.getId())
                                    .name(t.getName())
                                    .phone(t.getPhone())
                                    .email(t.getEmail())
                                    .roomId(t.getRoom() != null ? t.getRoom().getId() : null)
                                    .startDate(t.getStartDate())
                                    .endDate(t.getEndDate())
                                    .build();
                        });

        // Mock CurrentUser leniently as some tests might not trigger it (e.g. read tests)
        lenient().when(currentUser.getUserId()).thenReturn(MANAGER_ID);
    }

    // Create Tenant Tests

    @Test
    void createTenant_WithValidRequest_ShouldReturnResponse() {
        // Given
        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);
        request.setName(TENANT_NAME);
        request.setPhone(TENANT_PHONE);
        request.setEmail(TENANT_EMAIL);
        request.setIsContractHolder(true);

        when(roomRepository.findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID))
                .thenReturn(Optional.of(room));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        // When
        final var response = tenantService.createTenant(request);

        // Then
        assertNotNull(response);
        assertEquals(TENANT_ID, response.getId());
        assertEquals(TENANT_NAME, response.getName());
        verify(roomRepository).findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID);
        verify(tenantRepository).save(any(Tenant.class));
        verify(roomRepository).save(room); // Should update room status
        assertEquals(RoomStatus.OCCUPIED, room.getStatus());
    }

    @Test
    void createTenant_WithInvalidRoom_ShouldThrowException() {
        // Given
        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);

        when(roomRepository.findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID))
                .thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> tenantService.createTenant(request));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    // Update Tenant Tests

    @Test
    void updateTenant_WithValidRequest_ShouldReturnUpdatedResponse() {
        // Given
        final var request = new TenantUpdateRequest();
        request.setName("Nguyễn Văn B");

        final var updatedTenant = new Tenant();
        updatedTenant.setId(TENANT_ID);
        updatedTenant.setName("Nguyễn Văn B");
        updatedTenant.setPhone(TENANT_PHONE);
        updatedTenant.setRoom(room);

        when(tenantRepository.findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID))
                .thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(updatedTenant);

        // When
        final var response = tenantService.updateTenant(TENANT_ID, request);

        // Then
        assertNotNull(response);
        assertEquals("Nguyễn Văn B", response.getName());
        verify(tenantRepository).findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID);
        verify(tenantRepository).save(tenant);
    }

    // End Contract Tests

    @Test
    void endTenantContract_WithValidId_ShouldUpdateEndDate() {
        // Given
        when(tenantRepository.findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID))
                .thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        // Mock active tenants check for room status update (e.g. still 1 active left)
        when(tenantRepository.findByRoomIdOrderByStartDateDesc(ROOM_ID))
                .thenReturn(
                        Arrays.asList(tenant)); // tenant just ended, so result has end date now?
        // Actually save happens first, so tenant object is modified.

        // When
        final var response = tenantService.endTenantContract(TENANT_ID);

        // Then
        assertNotNull(response.getEndDate());
        verify(tenantRepository).save(tenant);
        verify(tenantRepository).findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID);
    }

    @Test
    void endTenantContract_AlreadyEnded_ShouldThrowException() {
        // Given
        tenant.setEndDate(LocalDate.now().minusDays(1));
        when(tenantRepository.findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID))
                .thenReturn(Optional.of(tenant));

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> tenantService.endTenantContract(TENANT_ID));
        assertEquals(ErrorCode.TENANT_CONTRACT_ALREADY_ENDED, exception.getErrorCode());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }
}
