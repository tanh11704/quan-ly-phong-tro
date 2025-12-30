package com.tpanh.backend.service;

import com.tpanh.backend.dto.PageResponse;
import com.tpanh.backend.dto.RoomCreationRequest;
import com.tpanh.backend.dto.RoomResponse;
import com.tpanh.backend.dto.RoomUpdateRequest;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.enums.RoomStatus;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.RoomMapper;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.repository.RoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final RoomMapper roomMapper;

    @Transactional
    @CacheEvict(value = "roomsByBuilding", key = "#request.buildingId")
    public RoomResponse createRoom(final String managerId, final RoomCreationRequest request) {
        final var building =
                buildingRepository
                        .findByIdAndManagerId(request.getBuildingId(), managerId)
                        .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        final var room = new Room();
        room.setBuilding(building);
        room.setRoomNo(request.getRoomNo());
        room.setPrice(request.getPrice());
        room.setStatus(request.getStatus() != null ? request.getStatus() : RoomStatus.VACANT);

        final var savedRoom = roomRepository.save(room);
        return roomMapper.toResponse(savedRoom);
    }

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "roomById", key = "#id"),
                @CacheEvict(value = "roomsByBuilding", allEntries = true)
            })
    public RoomResponse updateRoom(
            final Integer id, final String managerId, final RoomUpdateRequest request) {
        final var room =
                roomRepository
                        .findByIdAndBuildingManagerId(id, managerId)
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
        return roomMapper.toResponse(updatedRoom);
    }

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "roomById", key = "#id"),
                @CacheEvict(value = "roomsByBuilding", allEntries = true)
            })
    public void deleteRoom(final Integer id, final String managerId) {
        final var room =
                roomRepository
                        .findByIdAndBuildingManagerId(id, managerId)
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        roomRepository.delete(room);
    }

    @Cacheable(value = "roomsByBuilding", key = "#buildingId")
    public List<RoomResponse> getRoomsByBuildingId(
            final Integer buildingId, final String managerId) {
        if (!buildingRepository.existsByIdAndManagerId(buildingId, managerId)) {
            throw new AppException(ErrorCode.BUILDING_NOT_FOUND);
        }
        final var rooms =
                roomRepository.findByBuildingIdAndBuildingManagerId(buildingId, managerId);
        return rooms.stream().map(roomMapper::toResponse).toList();
    }

    @Cacheable(value = "roomById", key = "#id")
    public RoomResponse getRoomById(final Integer id, final String managerId) {
        final var room =
                roomRepository
                        .findByIdAndBuildingManagerId(id, managerId)
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        return roomMapper.toResponse(room);
    }

    @Cacheable(value = "roomById", key = "#id")
    public RoomResponse getRoomById(final Integer id) {
        final var room =
                roomRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        return roomMapper.toResponse(room);
    }

    public PageResponse<RoomResponse> getRoomsByBuildingId(
            final Integer buildingId, final String managerId, final Pageable pageable) {
        return getRoomsByBuildingId(buildingId, null, managerId, pageable);
    }

    public PageResponse<RoomResponse> getRoomsByBuildingId(
            final Integer buildingId,
            final RoomStatus status,
            final String managerId,
            final Pageable pageable) {
        if (!buildingRepository.existsByIdAndManagerId(buildingId, managerId)) {
            throw new AppException(ErrorCode.BUILDING_NOT_FOUND);
        }

        final Page<Room> page;
        if (status != null) {
            page =
                    roomRepository.findByBuildingIdAndStatusAndBuildingManagerId(
                            buildingId, status, managerId, pageable);
        } else {
            page =
                    roomRepository.findByBuildingIdAndBuildingManagerId(
                            buildingId, managerId, pageable);
        }

        return buildPageResponse(page);
    }

    public PageResponse<RoomResponse> getRoomsByBuildingId(
            final Integer buildingId, final RoomStatus status, final Pageable pageable) {
        if (!buildingRepository.existsById(buildingId)) {
            throw new AppException(ErrorCode.BUILDING_NOT_FOUND);
        }

        final Page<Room> page;
        if (status != null) {
            page = roomRepository.findByBuildingIdAndStatus(buildingId, status, pageable);
        } else {
            page = roomRepository.findByBuildingId(buildingId, pageable);
        }

        return buildPageResponse(page);
    }

    private PageResponse<RoomResponse> buildPageResponse(final Page<Room> page) {
        final var content = page.getContent().stream().map(roomMapper::toResponse).toList();
        return PageResponse.<RoomResponse>builder()
                .content(content)
                .page(buildPageInfo(page))
                .message("Lấy danh sách phòng thành công")
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
}
