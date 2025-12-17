package com.tpanh.backend.service;

import com.tpanh.backend.dto.UtilityReadingCreationRequest;
import com.tpanh.backend.dto.UtilityReadingResponse;
import com.tpanh.backend.dto.UtilityReadingUpdateRequest;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.UtilityReading;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.UtilityReadingMapper;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.UtilityReadingRepository;
import com.tpanh.backend.util.PeriodUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UtilityReadingService {

    private final UtilityReadingRepository utilityReadingRepository;
    private final RoomRepository roomRepository;
    private final UtilityReadingMapper utilityReadingMapper;

    @Transactional
    @CacheEvict(value = "rooms", allEntries = true)
    public UtilityReadingResponse createUtilityReading(
            final UtilityReadingCreationRequest request) {
        final Room room = getRoomOrThrow(request.getRoomId());
        assertNotExisted(request.getRoomId(), request.getMonth());
        validateCreationRequestIndices(request);
        final UtilityReading reading = buildReading(room, request);
        return saveAndMap(reading);
    }

    @Transactional
    @CacheEvict(value = "rooms", allEntries = true)
    public UtilityReadingResponse updateUtilityReading(
            final Integer id, final UtilityReadingUpdateRequest request) {
        final UtilityReading reading = getReadingOrThrow(id);
        validateUpdateRequestIndices(reading, request);
        applyUpdate(reading, request);
        return saveAndMap(reading);
    }

    public UtilityReadingResponse getUtilityReadingById(final Integer id) {
        final UtilityReading reading =
                utilityReadingRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.UTILITY_READING_NOT_FOUND));
        return utilityReadingMapper.toResponse(reading);
    }

    public List<UtilityReadingResponse> getUtilityReadingsByRoomId(final Integer roomId) {
        final var readings = utilityReadingRepository.findByRoomIdOrderByMonthDesc(roomId);
        return readings.stream().map(utilityReadingMapper::toResponse).collect(Collectors.toList());
    }

    public List<UtilityReadingResponse> getUtilityReadingsByBuildingAndMonth(
            final Integer buildingId, final String month) {
        final var readings =
                utilityReadingRepository.findByRoomBuildingIdAndMonth(buildingId, month);
        return readings.stream().map(utilityReadingMapper::toResponse).collect(Collectors.toList());
    }

    private void validateIndices(
            final Integer roomId,
            final String month,
            final Integer electricIndex,
            final Integer waterIndex,
            final boolean isMeterReset) {
        final String previousMonth = PeriodUtils.getPreviousMonth(month);
        final Optional<UtilityReading> previousReading =
                utilityReadingRepository.findByRoomIdAndMonth(roomId, previousMonth);

        if (isMeterReset) {
            return;
        }

        // Validate electricity index
        if (electricIndex != null && previousReading.isPresent()) {
            final Integer previousElectricIndex = previousReading.get().getElectricIndex();
            if (previousElectricIndex != null && electricIndex < previousElectricIndex) {
                throw new AppException(ErrorCode.UTILITY_READING_INVALID_INDEX);
            }
        }

        // Validate water index
        if (waterIndex != null && previousReading.isPresent()) {
            final Integer previousWaterIndex = previousReading.get().getWaterIndex();
            if (previousWaterIndex != null && waterIndex < previousWaterIndex) {
                throw new AppException(ErrorCode.UTILITY_READING_INVALID_INDEX);
            }
        }
    }

    private Room getRoomOrThrow(final Integer roomId) {
        return roomRepository
                .findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
    }

    private UtilityReading getReadingOrThrow(final Integer id) {
        return utilityReadingRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.UTILITY_READING_NOT_FOUND));
    }

    private void assertNotExisted(final Integer roomId, final String month) {
        final var existingReading = utilityReadingRepository.findByRoomIdAndMonth(roomId, month);
        if (existingReading.isPresent()) {
            throw new AppException(ErrorCode.UTILITY_READING_EXISTED);
        }
    }

    private void validateCreationRequestIndices(final UtilityReadingCreationRequest request) {
        validateIndices(
                request.getRoomId(),
                request.getMonth(),
                request.getElectricIndex(),
                request.getWaterIndex(),
                Boolean.TRUE.equals(request.getIsMeterReset()));
    }

    private void validateUpdateRequestIndices(
            final UtilityReading reading, final UtilityReadingUpdateRequest request) {
        final Integer newElectricIndex =
                request.getElectricIndex() != null
                        ? request.getElectricIndex()
                        : reading.getElectricIndex();
        final Integer newWaterIndex =
                request.getWaterIndex() != null ? request.getWaterIndex() : reading.getWaterIndex();
        validateIndices(
                reading.getRoom().getId(),
                reading.getMonth(),
                newElectricIndex,
                newWaterIndex,
                Boolean.TRUE.equals(request.getIsMeterReset()));
    }

    private UtilityReading buildReading(
            final Room room, final UtilityReadingCreationRequest request) {
        final UtilityReading reading = new UtilityReading();
        reading.setRoom(room);
        reading.setMonth(request.getMonth());
        reading.setElectricIndex(request.getElectricIndex());
        reading.setWaterIndex(request.getWaterIndex());
        reading.setImageEvidence(request.getImageEvidence());
        return reading;
    }

    private void applyUpdate(
            final UtilityReading reading, final UtilityReadingUpdateRequest request) {
        if (request.getElectricIndex() != null) {
            reading.setElectricIndex(request.getElectricIndex());
        }
        if (request.getWaterIndex() != null) {
            reading.setWaterIndex(request.getWaterIndex());
        }
        if (request.getImageEvidence() != null) {
            reading.setImageEvidence(request.getImageEvidence());
        }
    }

    private UtilityReadingResponse saveAndMap(final UtilityReading reading) {
        final UtilityReading savedReading = utilityReadingRepository.save(reading);
        return utilityReadingMapper.toResponse(savedReading);
    }
}
