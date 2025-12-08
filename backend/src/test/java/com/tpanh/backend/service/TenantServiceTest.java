package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tpanh.backend.dto.TenantCreationRequest;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.Tenant;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantRepository;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {
    private static final int ROOM_ID = 1;
    private static final int TENANT_ID = 1;
    private static final String ROOM_NO = "P.101";
    private static final String TENANT_NAME = "Nguyễn Văn A";
    private static final String TENANT_PHONE = "0901234567";
    private static final String STATUS_VACANT = "VACANT";
    private static final String STATUS_OCCUPIED = "OCCUPIED";

    @Mock private TenantRepository tenantRepository;
    @Mock private RoomRepository roomRepository;

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

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = tenantService.createTenant(request);

        // Then
        assertNotNull(response);
        assertEquals(TENANT_ID, response.getId());
        assertEquals(ROOM_ID, response.getRoomId());
        assertEquals(ROOM_NO, response.getRoomNo());
        assertEquals(TENANT_NAME, response.getName());
        assertEquals(TENANT_PHONE, response.getPhone());
        assertEquals(true, response.getIsContractHolder());
        verify(roomRepository).findById(ROOM_ID);
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

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = tenantService.createTenant(request);

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

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenantWithFalse);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = tenantService.createTenant(request);

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

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        // When
        final var response = tenantService.createTenant(request);

        // Then
        assertNotNull(response);
        verify(roomRepository).findById(ROOM_ID);
        verify(tenantRepository).save(any(Tenant.class));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void createTenant_WithInvalidRoomId_ShouldThrowException() {
        // Given
        final var request = new TenantCreationRequest();
        request.setRoomId(ROOM_ID);
        request.setName(TENANT_NAME);

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> tenantService.createTenant(request));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
        verify(roomRepository).findById(ROOM_ID);
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

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(tenantRepository.findByRoomIdOrderByStartDateDesc(ROOM_ID))
                .thenReturn(Arrays.asList(tenant, tenant2));

        // When
        final var response = tenantService.getTenantsByRoomId(ROOM_ID);

        // Then
        assertNotNull(response);
        assertEquals(2, response.size());
        verify(roomRepository).findById(ROOM_ID);
        verify(tenantRepository).findByRoomIdOrderByStartDateDesc(ROOM_ID);
    }

    @Test
    void getTenantsByRoomId_WithInvalidRoomId_ShouldThrowException() {
        // Given
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class, () -> tenantService.getTenantsByRoomId(ROOM_ID));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
        verify(roomRepository).findById(ROOM_ID);
        verify(tenantRepository, never()).findByRoomIdOrderByStartDateDesc(any(Integer.class));
    }

    @Test
    void endTenantContract_WithValidId_ShouldSetEndDate() {
        // Given
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantRepository.findByRoomIdOrderByStartDateDesc(ROOM_ID))
                .thenReturn(Arrays.asList(tenant));

        // When
        final var response = tenantService.endTenantContract(TENANT_ID);

        // Then
        assertNotNull(response);
        assertNotNull(response.getEndDate());
        verify(tenantRepository).findById(TENANT_ID);
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void endTenantContract_WithAlreadyEndedContract_ShouldThrowException() {
        // Given
        tenant.setEndDate(LocalDate.now().minusDays(5));
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class, () -> tenantService.endTenantContract(TENANT_ID));
        assertEquals(ErrorCode.TENANT_CONTRACT_ALREADY_ENDED, exception.getErrorCode());
        verify(tenantRepository).findById(TENANT_ID);
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void endTenantContract_WithNoActiveTenants_ShouldSetRoomToVACANT() {
        // Given
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantRepository.findByRoomIdOrderByStartDateDesc(ROOM_ID))
                .thenReturn(Arrays.asList(tenant));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // When
        final var response = tenantService.endTenantContract(TENANT_ID);

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

        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantRepository.findByRoomIdOrderByStartDateDesc(ROOM_ID))
                .thenReturn(Arrays.asList(tenant, activeTenant));

        // When
        final var response = tenantService.endTenantContract(TENANT_ID);

        // Then
        assertNotNull(response);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void endTenantContract_WithInvalidId_ShouldThrowException() {
        // Given
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class, () -> tenantService.endTenantContract(TENANT_ID));
        assertEquals(ErrorCode.TENANT_NOT_FOUND, exception.getErrorCode());
        verify(tenantRepository).findById(TENANT_ID);
        verify(tenantRepository, never()).save(any(Tenant.class));
    }
}
