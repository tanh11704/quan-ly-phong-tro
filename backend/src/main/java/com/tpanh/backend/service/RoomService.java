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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final RoomMapper roomMapper;

    @Transactional
    @PreAuthorize("@buildingPermission.canAccessBuilding(#request.buildingId, authentication)")
    @CacheEvict(value = "roomsByBuilding", key = "'building:' + #request.buildingId")
    public RoomResponse createRoom(final RoomCreationRequest request) {
        final var building =
                buildingRepository
                        .findById(request.getBuildingId())
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
    @PreAuthorize("@roomPermission.canAccessRoom(#id, authentication)")
    @Caching(
            evict = {
                @CacheEvict(value = "roomById", key = "#id"),
                @CacheEvict(value = "roomsByBuilding", allEntries = true)
            })
    public RoomResponse updateRoom(final Integer id, final RoomUpdateRequest request) {
        final var room =
                roomRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        roomMapper.updateEntity(room, request);

        final var savedRoom = roomRepository.save(room);
        return roomMapper.toResponse(savedRoom);
    }

    @Transactional
    @PreAuthorize("@roomPermission.canAccessRoom(#id, authentication)")
    @Caching(
            evict = {
                @CacheEvict(value = "roomById", key = "#id"),
                @CacheEvict(value = "roomsByBuilding", allEntries = true)
            })
    public void deleteRoom(final Integer id) {
        final var room =
                roomRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        roomRepository.delete(room);
    }

    @Cacheable(value = "roomsByBuilding", key = "'building:' + #buildingId")
    @PreAuthorize("@buildingPermission.canAccessBuilding(#buildingId, authentication)")
    public List<RoomResponse> getRoomsByBuildingId(final Integer buildingId) {
        final var rooms = roomRepository.findByBuildingId(buildingId);
        return rooms.stream().map(roomMapper::toResponse).toList();
    }

    @Cacheable(value = "roomById", key = "#id")
    @PreAuthorize("@roomPermission.canAccessRoom(#id, authentication)")
    public RoomResponse getRoomById(final Integer id) {
        final var room =
                roomRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        return roomMapper.toResponse(room);
    }

    @PreAuthorize("@buildingPermission.canAccessBuilding(#buildingId, authentication)")
    public PageResponse<RoomResponse> getRoomsByBuildingId(
            final Integer buildingId, final Pageable pageable) {
        return getRoomsByBuildingId(buildingId, null, pageable);
    }

    @PreAuthorize("@buildingPermission.canAccessBuilding(#buildingId, authentication)")
    public PageResponse<RoomResponse> getRoomsByBuildingId(
            final Integer buildingId, final RoomStatus status, final Pageable pageable) {

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
