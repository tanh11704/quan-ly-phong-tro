package com.tpanh.backend.service;

import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.BuildingResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.BuildingMapper;
import com.tpanh.backend.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private static final int DEFAULT_ELEC_UNIT_PRICE = 3500;
    private static final int DEFAULT_WATER_UNIT_PRICE = 20000;

    private final BuildingRepository buildingRepository;
    private final BuildingMapper buildingMapper;

    @Transactional
    @CacheEvict(value = "buildings", allEntries = true)
    public BuildingResponse createBuilding(final BuildingCreationRequest request) {
        final var building = new Building();
        building.setName(request.getName());
        building.setOwnerName(request.getOwnerName());
        building.setOwnerPhone(request.getOwnerPhone());
        building.setElecUnitPrice(
                request.getElecUnitPrice() != null
                        ? request.getElecUnitPrice()
                        : DEFAULT_ELEC_UNIT_PRICE);
        building.setWaterUnitPrice(
                request.getWaterUnitPrice() != null
                        ? request.getWaterUnitPrice()
                        : DEFAULT_WATER_UNIT_PRICE);
        building.setWaterCalcMethod(request.getWaterCalcMethod());

        final var savedBuilding = buildingRepository.save(building);
        return buildingMapper.toResponse(savedBuilding);
    }

    @Cacheable(value = "buildings", key = "#id")
    public BuildingResponse getBuildingById(final Integer id) {
        final var building =
                buildingRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));
        return buildingMapper.toResponse(building);
    }
}
