package com.tpanh.backend.service;

import com.tpanh.backend.dto.RoomCreationRequest;
import com.tpanh.backend.dto.RoomResponse;
import com.tpanh.backend.dto.RoomUpdateRequest;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.repository.RoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;

    @Transactional
    public RoomResponse createRoom(final RoomCreationRequest request) {
        final var building =
                buildingRepository
                        .findById(request.getBuildingId())
                        .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        final var room = new Room();
        room.setBuilding(building);
        room.setRoomNo(request.getRoomNo());
        room.setPrice(request.getPrice());
        room.setStatus(request.getStatus() != null ? request.getStatus() : "VACANT");

        final var savedRoom = roomRepository.save(room);
        return toResponse(savedRoom);
    }

    @Transactional
    public RoomResponse updateRoom(final Integer id, final RoomUpdateRequest request) {
        final var room =
                roomRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if (request.getRoomNo() != null) {
            room.setRoomNo(request.getRoomNo());
        }
        if (request.getPrice() != null) {
            room.setPrice(request.getPrice());
        }
        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        final var updatedRoom = roomRepository.save(room);
        return toResponse(updatedRoom);
    }

    @Transactional
    public void deleteRoom(final Integer id) {
        final var room =
                roomRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        roomRepository.delete(room);
    }

    public List<RoomResponse> getRoomsByBuildingId(final Integer buildingId) {
        final var building =
                buildingRepository
                        .findById(buildingId)
                        .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        final var rooms = roomRepository.findByBuildingId(buildingId);
        return rooms.stream().map(this::toResponse).toList();
    }

    private RoomResponse toResponse(final Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .buildingId(room.getBuilding().getId())
                .buildingName(room.getBuilding().getName())
                .roomNo(room.getRoomNo())
                .price(room.getPrice())
                .status(room.getStatus())
                .build();
    }
}
