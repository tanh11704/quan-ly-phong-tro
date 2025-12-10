package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.BuildingResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.BuildingMapper;
import com.tpanh.backend.repository.BuildingRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks private BuildingService buildingService;

    private Building savedBuilding;

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

    @Test
    void createBuilding_WithAllFields_ShouldReturnBuildingResponse() {
        // Given
        final var request = new BuildingCreationRequest();
        request.setName(BUILDING_NAME);
        request.setOwnerName(OWNER_NAME);
        request.setOwnerPhone(OWNER_PHONE);
        request.setElecUnitPrice(CUSTOM_ELEC_PRICE);
        request.setWaterUnitPrice(CUSTOM_WATER_PRICE);
        request.setWaterCalcMethod(WaterCalcMethod.BY_METER);

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
    }

    @Test
    void createBuilding_WithDefaultPrices_ShouldUseDefaultValues() {
        // Given
        final var request = new BuildingCreationRequest();
        request.setName(BUILDING_NAME);
        request.setWaterCalcMethod(WaterCalcMethod.PER_CAPITA);

        final var buildingWithDefaults = new Building();
        buildingWithDefaults.setId(BUILDING_ID);
        buildingWithDefaults.setName(BUILDING_NAME);
        buildingWithDefaults.setWaterCalcMethod(WaterCalcMethod.PER_CAPITA);
        buildingWithDefaults.setElecUnitPrice(3500);
        buildingWithDefaults.setWaterUnitPrice(20000);

        when(buildingRepository.save(any(Building.class))).thenReturn(buildingWithDefaults);

        // When
        final var response = buildingService.createBuilding(request);

        // Then
        assertNotNull(response);
        assertEquals(3500, response.getElecUnitPrice());
        assertEquals(20000, response.getWaterUnitPrice());
        verify(buildingRepository).save(any(Building.class));
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
}
