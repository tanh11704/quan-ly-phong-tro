package com.tpanh.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

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
        final Room room =
                roomRepository
                        .findById(request.getRoomId())
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        // Check if reading already exists for this room and month
        final var existingReading =
                utilityReadingRepository.findByRoomIdAndMonth(request.getRoomId(), request.getMonth());
        if (existingReading.isPresent()) {
            throw new AppException(ErrorCode.UTILITY_READING_EXISTED);
        }

        // Validate indices: new index must be >= previous month's index
        validateIndices(request.getRoomId(), request.getMonth(), request.getElectricIndex(), request.getWaterIndex());

        final UtilityReading reading = new UtilityReading();
        reading.setRoom(room);
        reading.setMonth(request.getMonth());
        reading.setElectricIndex(request.getElectricIndex());
        reading.setWaterIndex(request.getWaterIndex());
        reading.setImageEvidence(request.getImageEvidence());

        final UtilityReading savedReading = utilityReadingRepository.save(reading);
        return utilityReadingMapper.toResponse(savedReading);
    }

    @Transactional
    @CacheEvict(value = "rooms", allEntries = true)
    public UtilityReadingResponse updateUtilityReading(
            final Integer id, final UtilityReadingUpdateRequest request) {
        final UtilityReading reading =
                utilityReadingRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.UTILITY_READING_NOT_FOUND));

        // Validate indices if being updated
        final Integer newElectricIndex = request.getElectricIndex() != null 
                ? request.getElectricIndex() 
                : reading.getElectricIndex();
        final Integer newWaterIndex = request.getWaterIndex() != null 
                ? request.getWaterIndex() 
                : reading.getWaterIndex();

        validateIndices(reading.getRoom().getId(), reading.getMonth(), newElectricIndex, newWaterIndex);

        if (request.getElectricIndex() != null) {
            reading.setElectricIndex(request.getElectricIndex());
        }
        if (request.getWaterIndex() != null) {
            reading.setWaterIndex(request.getWaterIndex());
        }
        if (request.getImageEvidence() != null) {
            reading.setImageEvidence(request.getImageEvidence());
        }

        final UtilityReading savedReading = utilityReadingRepository.save(reading);
        return utilityReadingMapper.toResponse(savedReading);
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
        return readings.stream()
                .map(utilityReadingMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<UtilityReadingResponse> getUtilityReadingsByBuildingAndMonth(
            final Integer buildingId, final String month) {
        final var readings = utilityReadingRepository.findByRoomBuildingIdAndMonth(buildingId, month);
        return readings.stream()
                .map(utilityReadingMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateIndices(
            final Integer roomId,
            final String month,
            final Integer electricIndex,
            final Integer waterIndex) {
        final String previousMonth = getPreviousMonth(month);
        final Optional<UtilityReading> previousReading =
                utilityReadingRepository.findByRoomIdAndMonth(roomId, previousMonth);

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

    private String getPreviousMonth(final String month) {
        // Format: "YYYY-MM" -> "YYYY-MM" (previous month)
        try {
            final var parts = month.split("-");
            final int year = Integer.parseInt(parts[0]);
            final int monthValue = Integer.parseInt(parts[1]);

            int prevYear = year;
            int prevMonth = monthValue - 1;

            if (prevMonth < 1) {
                prevMonth = 12;
                prevYear = year - 1;
            }

            return String.format("%04d-%02d", prevYear, prevMonth);
        } catch (final Exception e) {
            // If parsing fails, return empty string (will result in no previous reading)
            return "";
        }
    }
}
