package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.BuildingResponse;
import com.tpanh.backend.dto.BuildingUpdateRequest;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.BuildingMapper;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {
    private static final String BUILDING_NAME = "Trọ Xanh";
    private static final String OWNER_NAME = "Nguyễn Văn Chủ";
    private static final String OWNER_PHONE = "0909123456";
    private static final int CUSTOM_ELEC_PRICE = 4000;
    private static final int CUSTOM_WATER_PRICE = 25000;
    private static final int BUILDING_ID = 1;

    @Mock private BuildingRepository buildingRepository;
    @Mock private BuildingMapper buildingMapper;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks private BuildingService buildingService;

    private Building savedBuilding;
    private static final String MANAGER_ID = "manager-id-123";

    @BeforeEach
    void setUp() {
        savedBuilding = new Building();
        savedBuilding.setId(BUILDING_ID);
        savedBuilding.setName(BUILDING_NAME);
        savedBuilding.setOwnerName(OWNER_NAME);
        savedBuilding.setOwnerPhone(OWNER_PHONE);
        savedBuilding.setElecUnitPrice(CUSTOM_ELEC_PRICE);
        savedBuilding.setWaterUnitPrice(CUSTOM_WATER_PRICE);
        savedBuilding.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        // Mock mapper to return response based on building (lenient for tests that don't use
        // mapper)
        lenient()
                .when(buildingMapper.toResponse(any(Building.class)))
                .thenAnswer(
                        invocation -> {
                            final Building b = invocation.getArgument(0);
                            return BuildingResponse.builder()
                                    .id(b.getId())
                                    .name(b.getName())
                                    .ownerName(b.getOwnerName())
                                    .ownerPhone(b.getOwnerPhone())
                                    .elecUnitPrice(b.getElecUnitPrice())
                                    .waterUnitPrice(b.getWaterUnitPrice())
                                    .waterCalcMethod(b.getWaterCalcMethod())
                                    .build();
                        });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext() {
        final var auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(MANAGER_ID);
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createBuilding_WithAllFields_ShouldReturnBuildingResponse() {
        // Given
        final var manager = User.builder().id(MANAGER_ID).roles(Role.MANAGER).build();
        final var authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(MANAGER_ID);
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        final var request = new BuildingCreationRequest();
        request.setName(BUILDING_NAME);
        request.setOwnerName(OWNER_NAME);
        request.setOwnerPhone(OWNER_PHONE);
        request.setElecUnitPrice(CUSTOM_ELEC_PRICE);
        request.setWaterUnitPrice(CUSTOM_WATER_PRICE);
        request.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        when(userRepository.findById(MANAGER_ID)).thenReturn(Optional.of(manager));
        when(buildingRepository.save(any(Building.class))).thenReturn(savedBuilding);

        // When
        final var response = buildingService.createBuilding(request);

        // Then
        assertNotNull(response);
        assertEquals(BUILDING_ID, response.getId());
        assertEquals(BUILDING_NAME, response.getName());
        assertEquals(OWNER_NAME, response.getOwnerName());
        assertEquals(OWNER_PHONE, response.getOwnerPhone());
        assertEquals(CUSTOM_ELEC_PRICE, response.getElecUnitPrice());
        assertEquals(CUSTOM_WATER_PRICE, response.getWaterUnitPrice());
        assertEquals(WaterCalcMethod.BY_METER, response.getWaterCalcMethod());
        verify(buildingRepository).save(any(Building.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    void createBuilding_WithDefaultPrices_ShouldUseDefaultValues() {
        // Given
        final var manager = User.builder().id(MANAGER_ID).roles(Role.MANAGER).build();
        final var authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(MANAGER_ID);
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        final var request = new BuildingCreationRequest();
        request.setName(BUILDING_NAME);
        request.setWaterCalcMethod(WaterCalcMethod.PER_CAPITA);

        final var buildingWithDefaults = new Building();
        buildingWithDefaults.setId(BUILDING_ID);
        buildingWithDefaults.setName(BUILDING_NAME);
        buildingWithDefaults.setWaterCalcMethod(WaterCalcMethod.PER_CAPITA);
        buildingWithDefaults.setElecUnitPrice(3500);
        buildingWithDefaults.setWaterUnitPrice(20000);

        when(userRepository.findById(MANAGER_ID)).thenReturn(Optional.of(manager));
        when(buildingRepository.save(any(Building.class))).thenReturn(buildingWithDefaults);

        // When
        final var response = buildingService.createBuilding(request);

        // Then
        assertNotNull(response);
        assertEquals(3500, response.getElecUnitPrice());
        assertEquals(20000, response.getWaterUnitPrice());
        verify(buildingRepository).save(any(Building.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    void getBuildingById_WithValidId_ShouldReturnBuildingResponse() {
        // Given
        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.of(savedBuilding));

        // When
        final var response = buildingService.getBuildingById(BUILDING_ID);

        // Then
        assertNotNull(response);
        assertEquals(BUILDING_ID, response.getId());
        assertEquals(BUILDING_NAME, response.getName());
        verify(buildingRepository).findById(BUILDING_ID);
    }

    @Test
    void getBuildingById_WithInvalidId_ShouldThrowException() {
        // Given
        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class, () -> buildingService.getBuildingById(BUILDING_ID));
        assertEquals(ErrorCode.BUILDING_NOT_FOUND, exception.getErrorCode());
        verify(buildingRepository).findById(BUILDING_ID);
    }

    @Test
    void getBuildingsByCurrentManager_WithPageable_ShouldReturnPageResponse() {
        // Given
        final var building2 = new Building();
        building2.setId(2);
        building2.setName("Trọ Vàng");
        building2.setManager(User.builder().id(MANAGER_ID).build());

        savedBuilding.setManager(User.builder().id(MANAGER_ID).build());

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(MANAGER_ID);
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Building> page = new PageImpl<>(Arrays.asList(savedBuilding, building2), pageable, 2);

        when(buildingRepository.findByManagerId(MANAGER_ID, pageable)).thenReturn(page);

        // When
        final var response = buildingService.getBuildingsByCurrentManager(pageable);

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
        verify(buildingRepository).findByManagerId(MANAGER_ID, pageable);
    }

    @Test
    void getBuildingsByCurrentManager_NonPageable_ShouldReturnList() {
        // Given
        setupSecurityContext();

        final var building2 = new Building();
        building2.setId(2);
        building2.setName("Trọ Vàng");
        building2.setElecUnitPrice(3500);
        building2.setWaterUnitPrice(20000);
        building2.setManager(User.builder().id(MANAGER_ID).build());

        savedBuilding.setManager(User.builder().id(MANAGER_ID).build());

        when(buildingRepository.findByManagerId(MANAGER_ID))
                .thenReturn(Arrays.asList(savedBuilding, building2));

        // When
        final List<BuildingResponse> result = buildingService.getBuildingsByCurrentManager();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(BUILDING_NAME, result.get(0).getName());
        assertEquals("Trọ Vàng", result.get(1).getName());
        verify(buildingRepository).findByManagerId(MANAGER_ID);
    }

    @Test
    void getBuildingsByCurrentManager_NonPageable_WithNoBuildings_ShouldReturnEmptyList() {
        // Given
        setupSecurityContext();

        when(buildingRepository.findByManagerId(MANAGER_ID)).thenReturn(Collections.emptyList());

        // When
        final List<BuildingResponse> result = buildingService.getBuildingsByCurrentManager();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ===== Tests for updateBuilding =====

    @Test
    void updateBuilding_WithAllFields_ShouldReturnUpdatedBuilding() {
        // Given
        setupSecurityContext();

        final var request = new BuildingUpdateRequest();
        request.setName("Trọ Mới");
        request.setOwnerName("Nguyễn Văn Mới");
        request.setOwnerPhone("0912345678");
        request.setElecUnitPrice(5000);
        request.setWaterUnitPrice(30000);
        request.setWaterCalcMethod(WaterCalcMethod.PER_CAPITA);

        final var updatedBuilding = new Building();
        updatedBuilding.setId(BUILDING_ID);
        updatedBuilding.setName("Trọ Mới");
        updatedBuilding.setOwnerName("Nguyễn Văn Mới");
        updatedBuilding.setOwnerPhone("0912345678");
        updatedBuilding.setElecUnitPrice(5000);
        updatedBuilding.setWaterUnitPrice(30000);
        updatedBuilding.setWaterCalcMethod(WaterCalcMethod.PER_CAPITA);

        when(buildingRepository.findByIdAndManagerId(BUILDING_ID, MANAGER_ID))
                .thenReturn(Optional.of(savedBuilding));
        when(buildingRepository.save(any(Building.class))).thenReturn(updatedBuilding);

        // When
        final var response = buildingService.updateBuilding(BUILDING_ID, request);

        // Then
        assertNotNull(response);
        assertEquals("Trọ Mới", response.getName());
        assertEquals("Nguyễn Văn Mới", response.getOwnerName());
        assertEquals("0912345678", response.getOwnerPhone());
        assertEquals(5000, response.getElecUnitPrice());
        assertEquals(30000, response.getWaterUnitPrice());
        assertEquals(WaterCalcMethod.PER_CAPITA, response.getWaterCalcMethod());
        verify(buildingRepository).save(any(Building.class));
    }

    @Test
    void updateBuilding_WithPartialFields_ShouldOnlyUpdateProvidedFields() {
        // Given
        setupSecurityContext();

        final var request = new BuildingUpdateRequest();
        request.setName("Trọ Cập Nhật");
        // Other fields are null

        final var updatedBuilding = new Building();
        updatedBuilding.setId(BUILDING_ID);
        updatedBuilding.setName("Trọ Cập Nhật");
        updatedBuilding.setOwnerName(OWNER_NAME); // Unchanged
        updatedBuilding.setOwnerPhone(OWNER_PHONE); // Unchanged
        updatedBuilding.setElecUnitPrice(CUSTOM_ELEC_PRICE); // Unchanged
        updatedBuilding.setWaterUnitPrice(CUSTOM_WATER_PRICE); // Unchanged
        updatedBuilding.setWaterCalcMethod(WaterCalcMethod.BY_METER); // Unchanged

        when(buildingRepository.findByIdAndManagerId(BUILDING_ID, MANAGER_ID))
                .thenReturn(Optional.of(savedBuilding));
        when(buildingRepository.save(any(Building.class))).thenReturn(updatedBuilding);

        // When
        final var response = buildingService.updateBuilding(BUILDING_ID, request);

        // Then
        assertNotNull(response);
        assertEquals("Trọ Cập Nhật", response.getName());
        assertEquals(OWNER_NAME, response.getOwnerName());
        assertEquals(OWNER_PHONE, response.getOwnerPhone());
        assertEquals(CUSTOM_ELEC_PRICE, response.getElecUnitPrice());
    }

    @Test
    void updateBuilding_WithInvalidId_ShouldThrowException() {
        // Given
        setupSecurityContext();

        final var request = new BuildingUpdateRequest();
        request.setName("Trọ Mới");

        when(buildingRepository.findByIdAndManagerId(999, MANAGER_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception = assertThrows(
                AppException.class, () -> buildingService.updateBuilding(999, request));
        assertEquals(ErrorCode.BUILDING_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateBuilding_WithEmptyRequest_ShouldReturnUnchangedBuilding() {
        // Given
        setupSecurityContext();

        final var request = new BuildingUpdateRequest();
        // All fields are null - no updates

        when(buildingRepository.findByIdAndManagerId(BUILDING_ID, MANAGER_ID))
                .thenReturn(Optional.of(savedBuilding));
        when(buildingRepository.save(any(Building.class))).thenReturn(savedBuilding);

        // When
        final var response = buildingService.updateBuilding(BUILDING_ID, request);

        // Then
        assertNotNull(response);
        assertEquals(BUILDING_NAME, response.getName());
        assertEquals(OWNER_NAME, response.getOwnerName());
    }

    // ===== Tests for deleteBuilding =====

    @Test
    void deleteBuilding_WithValidId_ShouldDeleteBuilding() {
        // Given
        setupSecurityContext();

        when(buildingRepository.findByIdAndManagerId(BUILDING_ID, MANAGER_ID))
                .thenReturn(Optional.of(savedBuilding));
        doNothing().when(buildingRepository).delete(savedBuilding);

        // When & Then
        assertDoesNotThrow(() -> buildingService.deleteBuilding(BUILDING_ID));
        verify(buildingRepository).delete(savedBuilding);
    }

    @Test
    void deleteBuilding_WithInvalidId_ShouldThrowException() {
        // Given
        setupSecurityContext();

        when(buildingRepository.findByIdAndManagerId(999, MANAGER_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception = assertThrows(
                AppException.class, () -> buildingService.deleteBuilding(999));
        assertEquals(ErrorCode.BUILDING_NOT_FOUND, exception.getErrorCode());
    }

    // ===== Tests for getCurrentUserId edge cases =====

    @Test
    void createBuilding_WithNullAuthentication_ShouldThrowUnauthorized() {
        // Given
        SecurityContextHolder.clearContext();

        final var request = new BuildingCreationRequest();
        request.setName(BUILDING_NAME);
        request.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        // When & Then
        final var exception = assertThrows(
                AppException.class, () -> buildingService.createBuilding(request));
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void createBuilding_WithUnauthenticatedUser_ShouldThrowUnauthorized() {
        // Given
        final var auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        final var request = new BuildingCreationRequest();
        request.setName(BUILDING_NAME);
        request.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        // When & Then
        final var exception = assertThrows(
                AppException.class, () -> buildingService.createBuilding(request));
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void createBuilding_WithUserNotFound_ShouldThrowUserNotFound() {
        // Given
        setupSecurityContext();

        final var request = new BuildingCreationRequest();
        request.setName(BUILDING_NAME);
        request.setWaterCalcMethod(WaterCalcMethod.BY_METER);

        when(userRepository.findById(MANAGER_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception = assertThrows(
                AppException.class, () -> buildingService.createBuilding(request));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getBuildingsByCurrentManager_Pageable_WithEmptyResult_ShouldReturnEmptyPageResponse() {
        // Given
        setupSecurityContext();

        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Building> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(buildingRepository.findByManagerId(MANAGER_ID, pageable)).thenReturn(emptyPage);

        // When
        final var response = buildingService.getBuildingsByCurrentManager(pageable);

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getPage().getTotalElements());
        assertEquals("Lấy danh sách tòa nhà thành công", response.getMessage());
    }
}
