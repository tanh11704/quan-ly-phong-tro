package com.tpanh.backend.service;

import com.tpanh.backend.config.BuildingProperties;
import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.BuildingResponse;
import com.tpanh.backend.dto.BuildingUpdateRequest;
import com.tpanh.backend.dto.PageResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.BuildingMapper;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingService {
    private final BuildingRepository buildingRepository;
    private final BuildingMapper buildingMapper;
    private final UserRepository userRepository;
    private final BuildingProperties buildingProperties;

    @Transactional
    @CacheEvict(value = "buildings", allEntries = true)
    public BuildingResponse createBuilding(
            final String managerId, final BuildingCreationRequest request) {
        final var manager =
                userRepository
                        .findById(managerId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        final var building = new Building();
        building.setName(request.getName());
        building.setOwnerName(request.getOwnerName());
        building.setOwnerPhone(request.getOwnerPhone());
        building.setElecUnitPrice(
                request.getElecUnitPrice() != null
                        ? request.getElecUnitPrice()
                        : buildingProperties.getDefaultElecUnitPrice());
        building.setWaterUnitPrice(
                request.getWaterUnitPrice() != null
                        ? request.getWaterUnitPrice()
                        : buildingProperties.getDefaultWaterUnitPrice());
        building.setWaterCalcMethod(request.getWaterCalcMethod());
        building.setManager(manager);

        final var savedBuilding = buildingRepository.save(building);
        log.info("Building created: id={}, managerId={}", savedBuilding.getId(), managerId);
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

    public List<BuildingResponse> getBuildingsByManagerId(final String managerId) {
        final var buildings = buildingRepository.findByManagerId(managerId);
        return buildings.stream().map(buildingMapper::toResponse).toList();
    }

    public PageResponse<BuildingResponse> getBuildingsByManagerId(
            final String managerId, final Pageable pageable) {
        final var page = buildingRepository.findByManagerId(managerId, pageable);
        final var content = page.getContent().stream().map(buildingMapper::toResponse).toList();
        return PageResponse.<BuildingResponse>builder()
                .content(content)
                .page(buildPageInfo(page))
                .message("Lấy danh sách tòa nhà thành công")
                .build();
    }

    private PageResponse.PageInfo buildPageInfo(final Page<?> page) {
        return PageResponse.PageInfo.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional
    @CacheEvict(value = "buildings", allEntries = true)
    public BuildingResponse updateBuilding(
            final Integer id, final String managerId, final BuildingUpdateRequest request) {
        final var building =
                buildingRepository
                        .findByIdAndManagerId(id, managerId)
                        .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        if (request.getName() != null) {
            building.setName(request.getName());
        }
        if (request.getOwnerName() != null) {
            building.setOwnerName(request.getOwnerName());
        }
        if (request.getOwnerPhone() != null) {
            building.setOwnerPhone(request.getOwnerPhone());
        }
        if (request.getElecUnitPrice() != null) {
            building.setElecUnitPrice(request.getElecUnitPrice());
        }
        if (request.getWaterUnitPrice() != null) {
            building.setWaterUnitPrice(request.getWaterUnitPrice());
        }
        if (request.getWaterCalcMethod() != null) {
            building.setWaterCalcMethod(request.getWaterCalcMethod());
        }

        final var updatedBuilding = buildingRepository.save(building);
        log.info("Building updated: id={}, managerId={}", id, managerId);
        return buildingMapper.toResponse(updatedBuilding);
    }

    @Transactional
    @CacheEvict(value = "buildings", allEntries = true)
    public void deleteBuilding(final Integer id, final String managerId) {
        final var building =
                buildingRepository
                        .findByIdAndManagerId(id, managerId)
                        .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        buildingRepository.delete(building);
        log.info("Building deleted: id={}, managerId={}", id, managerId);
    }
}
