package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {
    private static final int ROOM_ID = 1;
    private static final int TENANT_ID = 1;
    private static final String ROOM_NO = "P.101";
    private static final String TENANT_NAME = "Nguyễn Văn A";
    private static final String TENANT_PHONE = "0901234567";
    private static final RoomStatus STATUS_VACANT = RoomStatus.VACANT;
    private static final RoomStatus STATUS_OCCUPIED = RoomStatus.OCCUPIED;
    private static final String MANAGER_ID = "manager-123";

    @Mock private TenantRepository tenantRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private TenantMapper tenantMapper;

    @InjectMocks private TenantService tenantService;

    private Room room;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(ROOM_ID);
        room.setRoomNo(ROOM_NO);
        room.setStatus(STATUS_VACANT);

        tenant = new Tenant();
        tenant.setId(TENANT_ID);
        tenant.setRoom(room);
        tenant.setName(TENANT_NAME);
        tenant.setPhone(TENANT_PHONE);
        tenant.setIsContractHolder(true);
        tenant.setStartDate(LocalDate.now());

        // Mock mapper to return response based on tenant (lenient for tests that don't use mapper)
        lenient()
                .when(tenantMapper.toResponse(any(Tenant.class)))
                .thenAnswer(
                        invocation -> {
                            final Tenant t = invocation.getArgument(0);
                            return TenantResponse.builder()
                                    .id(t.getId())
                                    .roomId(t.getRoom() != null ? t.getRoom().getId() : null)
                                    .roomNo(t.getRoom() != null ? t.getRoom().getRoomNo() : null)
                                    .name(t.getName())
                                    .phone(t.getPhone())
                                    .isContractHolder(t.getIsContractHolder())
                                    .startDate(t.getStartDate())
                                    .endDate(t.getEndDate())
                                    .build();
                        });
    }

    @Test
    void createTenant_WithValidRequest_ShouldReturnTenantResponse() {
        // Given
        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);
        request.setName(TENANT_NAME);
        request.setPhone(TENANT_PHONE);
        request.setIsContractHolder(true);
        request.setStartDate(LocalDate.now());

        when(roomRepository.findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID))
                .thenReturn(Optional.of(room));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = tenantService.createTenant(MANAGER_ID, request);

        // Then
        assertNotNull(response);
        assertEquals(TENANT_ID, response.getId());
        assertEquals(ROOM_ID, response.getRoomId());
        verify(roomRepository).findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID);
        verify(tenantRepository).save(any(Tenant.class));
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void createTenant_WithNullStartDate_ShouldUseCurrentDate() {
        // Given
        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);
        request.setName(TENANT_NAME);
        request.setStartDate(null);

        when(roomRepository.findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID))
                .thenReturn(Optional.of(room));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = tenantService.createTenant(MANAGER_ID, request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getStartDate());
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void createTenant_WithNullIsContractHolder_ShouldDefaultToFalse() {
        // Given
        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);
        request.setName(TENANT_NAME);
        request.setIsContractHolder(null);

        final var tenantWithFalse = new Tenant();
        tenantWithFalse.setId(TENANT_ID);
        tenantWithFalse.setRoom(room);
        tenantWithFalse.setName(TENANT_NAME);
        tenantWithFalse.setIsContractHolder(false);

        when(roomRepository.findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID))
                .thenReturn(Optional.of(room));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenantWithFalse);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = tenantService.createTenant(MANAGER_ID, request);

        // Then
        assertNotNull(response);
        assertEquals(false, response.getIsContractHolder());
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void createTenant_WithOccupiedRoom_ShouldNotChangeRoomStatus() {
        // Given
        room.setStatus(STATUS_OCCUPIED);
        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);
        request.setName(TENANT_NAME);

        when(roomRepository.findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID))
                .thenReturn(Optional.of(room));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        // When
        final var response = tenantService.createTenant(MANAGER_ID, request);

        // Then
        assertNotNull(response);
        verify(roomRepository).findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID);
        verify(tenantRepository).save(any(Tenant.class));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void createTenant_WithNullRoomStatus_ShouldSetRoomToOCCUPIED() {
        // Given
        room.setStatus(null);
        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);
        request.setName(TENANT_NAME);

        when(roomRepository.findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID))
                .thenReturn(Optional.of(room));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = tenantService.createTenant(MANAGER_ID, request);

        // Then
        assertNotNull(response);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void createTenant_WithInvalidRoomId_ShouldThrowException() {
        // Given
        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);
        request.setName(TENANT_NAME);

        when(roomRepository.findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID))
                .thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class, () -> tenantService.createTenant(MANAGER_ID, request));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
        verify(roomRepository).findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID);
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void getTenantById_WithValidId_ShouldReturnTenantResponse() {
        // Given
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));

        // When
        final var response = tenantService.getTenantById(TENANT_ID);

        // Then
        assertNotNull(response);
        assertEquals(TENANT_ID, response.getId());
        assertEquals(TENANT_NAME, response.getName());
        verify(tenantRepository).findById(TENANT_ID);
    }

    @Test
    void getTenantById_WithInvalidId_ShouldThrowException() {
        // Given
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> tenantService.getTenantById(TENANT_ID));
        assertEquals(ErrorCode.TENANT_NOT_FOUND, exception.getErrorCode());
        verify(tenantRepository).findById(TENANT_ID);
    }

    @Test
    void getTenantsByRoomId_WithValidRoomId_ShouldReturnTenantList() {
        // Given
        final var tenant2 = new Tenant();
        tenant2.setId(2);
        tenant2.setRoom(room);
        tenant2.setName("Nguyễn Văn B");
        tenant2.setStartDate(LocalDate.now().minusDays(10));

        when(roomRepository.existsById(ROOM_ID)).thenReturn(true);
        when(tenantRepository.findByRoomIdOrderByStartDateDesc(ROOM_ID))
                .thenReturn(Arrays.asList(tenant, tenant2));

        // When
        final var response = tenantService.getTenantsByRoomId(ROOM_ID);

        // Then
        assertNotNull(response);
        assertEquals(2, response.size());
        verify(roomRepository).existsById(ROOM_ID);
        verify(tenantRepository).findByRoomIdOrderByStartDateDesc(ROOM_ID);
    }

    @Test
    void getTenantsByRoomId_WithInvalidRoomId_ShouldThrowException() {
        // Given
        when(roomRepository.existsById(ROOM_ID)).thenReturn(false);

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> tenantService.getTenantsByRoomId(ROOM_ID));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
        verify(roomRepository).existsById(ROOM_ID);
        verify(tenantRepository, never()).findByRoomIdOrderByStartDateDesc(any(Integer.class));
    }

    @Test
    void endTenantContract_WithValidId_ShouldSetEndDate() {
        // Given
        when(tenantRepository.findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID))
                .thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantRepository.findByRoomIdOrderByStartDateDesc(ROOM_ID))
                .thenReturn(Arrays.asList(tenant));

        // When
        final var response = tenantService.endTenantContract(TENANT_ID, MANAGER_ID);

        // Then
        assertNotNull(response);
        assertNotNull(response.getEndDate());
        verify(tenantRepository).findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID);
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void endTenantContract_WithAlreadyEndedContract_ShouldThrowException() {
        // Given
        tenant.setEndDate(LocalDate.now().minusDays(5));
        when(tenantRepository.findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID))
                .thenReturn(Optional.of(tenant));

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class,
                        () -> tenantService.endTenantContract(TENANT_ID, MANAGER_ID));
        assertEquals(ErrorCode.TENANT_CONTRACT_ALREADY_ENDED, exception.getErrorCode());
        verify(tenantRepository).findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID);
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void endTenantContract_WithNoActiveTenants_ShouldSetRoomToVACANT() {
        // Given
        when(tenantRepository.findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID))
                .thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantRepository.findByRoomIdOrderByStartDateDesc(ROOM_ID))
                .thenReturn(Arrays.asList(tenant));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = tenantService.endTenantContract(TENANT_ID, MANAGER_ID);

        // Then
        assertNotNull(response);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void endTenantContract_WithOtherActiveTenants_ShouldNotChangeRoomStatus() {
        // Given
        final var activeTenant = new Tenant();
        activeTenant.setId(2);
        activeTenant.setRoom(room);
        activeTenant.setEndDate(null);

        when(tenantRepository.findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID))
                .thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantRepository.findByRoomIdOrderByStartDateDesc(ROOM_ID))
                .thenReturn(Arrays.asList(tenant, activeTenant));

        // When
        final var response = tenantService.endTenantContract(TENANT_ID, MANAGER_ID);

        // Then
        assertNotNull(response);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void endTenantContract_WithInvalidId_ShouldThrowException() {
        // Given
        when(tenantRepository.findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID))
                .thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class,
                        () -> tenantService.endTenantContract(TENANT_ID, MANAGER_ID));
        assertEquals(ErrorCode.TENANT_NOT_FOUND, exception.getErrorCode());
        verify(tenantRepository).findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID);
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void endTenantContract_WithNullRoom_ShouldNotUpdateRoomStatus() {
        // Given
        tenant.setRoom(null);
        when(tenantRepository.findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID))
                .thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        // When
        final var response = tenantService.endTenantContract(TENANT_ID, MANAGER_ID);

        // Then
        assertNotNull(response);
        verify(tenantRepository).findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID);
        verify(tenantRepository).save(any(Tenant.class));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void getTenantById_WithNullRoom_ShouldReturnTenantWithNullRoomFields() {
        // Given
        tenant.setRoom(null);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));

        // When
        final var response = tenantService.getTenantById(TENANT_ID);

        // Then
        assertNotNull(response);
        assertEquals(TENANT_ID, response.getId());
        assertEquals(TENANT_NAME, response.getName());
        assertEquals(null, response.getRoomId());
        assertEquals(null, response.getRoomNo());
        verify(tenantRepository).findById(TENANT_ID);
    }

    @Test
    void getTenantsByRoomId_WithPageable_ShouldReturnPageResponse() {
        // Given
        final var tenant2 = new Tenant();
        tenant2.setId(2);
        tenant2.setRoom(room);
        tenant2.setName("Nguyễn Văn B");
        tenant2.setStartDate(LocalDate.now().minusDays(10));

        when(roomRepository.existsById(ROOM_ID)).thenReturn(true);

        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Tenant> page = new PageImpl<>(Arrays.asList(tenant, tenant2), pageable, 2);

        when(tenantRepository.findByRoomIdOrderByStartDateDesc(ROOM_ID, pageable)).thenReturn(page);

        // When
        final var response = tenantService.getTenantsByRoomId(ROOM_ID, pageable);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(2, response.getContent().size());
        assertNotNull(response.getPage());
        assertEquals(0, response.getPage().getPage());
        assertEquals(10, response.getPage().getSize());
        assertEquals(2, response.getPage().getTotalElements());
        assertEquals(1, response.getPage().getTotalPages());
        assertTrue(response.getPage().isFirst());
        assertTrue(response.getPage().isLast());
        verify(roomRepository).existsById(ROOM_ID);
        verify(tenantRepository).findByRoomIdOrderByStartDateDesc(ROOM_ID, pageable);
    }

    @Test
    void getTenantsByRoomId_WithPageable_InvalidRoomId_ShouldThrowException() {
        // Given
        final Pageable pageable = PageRequest.of(0, 10);
        when(roomRepository.existsById(ROOM_ID)).thenReturn(false);

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class,
                        () -> tenantService.getTenantsByRoomId(ROOM_ID, pageable));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
        verify(roomRepository).existsById(ROOM_ID);
        verify(tenantRepository, never())
                .findByRoomIdOrderByStartDateDesc(any(Integer.class), any(Pageable.class));
    }

    @Test
    void createTenant_WithExistingActiveContractHolder_ShouldThrowException() {
        // Given
        final var existingContractHolder = new Tenant();
        existingContractHolder.setId(99);
        existingContractHolder.setRoom(room);
        existingContractHolder.setIsContractHolder(true);
        existingContractHolder.setEndDate(null); // Active

        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);
        request.setName("New Tenant");
        request.setIsContractHolder(true);

        when(roomRepository.findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID))
                .thenReturn(Optional.of(room));
        when(tenantRepository.findByRoomIdAndIsContractHolderTrueAndEndDateIsNull(ROOM_ID))
                .thenReturn(Optional.of(existingContractHolder));

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class, () -> tenantService.createTenant(MANAGER_ID, request));
        assertEquals(ErrorCode.CONTRACT_HOLDER_ALREADY_EXISTS, exception.getErrorCode());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void createTenant_WithExpiredContractHolder_ShouldSucceed() {
        // Given - existing contract holder has ended their contract
        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);
        request.setName(TENANT_NAME);
        request.setIsContractHolder(true);

        when(roomRepository.findByIdAndBuildingManagerId(ROOM_ID, MANAGER_ID))
                .thenReturn(Optional.of(room));
        when(tenantRepository.findByRoomIdAndIsContractHolderTrueAndEndDateIsNull(ROOM_ID))
                .thenReturn(Optional.empty()); // No active contract holder
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = tenantService.createTenant(MANAGER_ID, request);

        // Then
        assertNotNull(response);
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void updateTenant_SetContractHolderWhenExisting_ShouldThrowException() {
        // Given
        final var existingContractHolder = new Tenant();
        existingContractHolder.setId(99);
        existingContractHolder.setRoom(room);
        existingContractHolder.setIsContractHolder(true);
        existingContractHolder.setEndDate(null); // Active

        // Current tenant is NOT a contract holder
        tenant.setIsContractHolder(false);
        tenant.setEndDate(null); // Still active

        final var request = new TenantUpdateRequest();
        request.setIsContractHolder(true); // Trying to become contract holder

        when(tenantRepository.findByIdAndRoomBuildingManagerId(TENANT_ID, MANAGER_ID))
                .thenReturn(Optional.of(tenant));
        when(tenantRepository.findByRoomIdAndIsContractHolderTrueAndEndDateIsNull(ROOM_ID))
                .thenReturn(Optional.of(existingContractHolder));

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class,
                        () -> tenantService.updateTenant(TENANT_ID, MANAGER_ID, request));
        assertEquals(ErrorCode.CONTRACT_HOLDER_ALREADY_EXISTS, exception.getErrorCode());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }
}
