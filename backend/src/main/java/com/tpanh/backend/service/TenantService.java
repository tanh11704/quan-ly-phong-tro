package com.tpanh.backend.service;

import com.tpanh.backend.dto.PageResponse;
import com.tpanh.backend.dto.TenantCreationRequest;
import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.entity.Tenant;
import com.tpanh.backend.enums.RoomStatus;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.TenantMapper;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantRepository;
import java.time.LocalDate;
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
public class TenantService {
    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;
    private final TenantMapper tenantMapper;

    @Transactional
    public TenantResponse createTenant(final TenantCreationRequest request) {
        final var room =
                roomRepository
                        .findById(request.getRoomId())
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        final var tenant = new Tenant();
        tenant.setRoom(room);
        tenant.setName(request.getName());
        tenant.setPhone(request.getPhone());
        tenant.setIsContractHolder(
                request.getIsContractHolder() != null ? request.getIsContractHolder() : false);
        tenant.setStartDate(
                request.getStartDate() != null ? request.getStartDate() : LocalDate.now());

        final var savedTenant = tenantRepository.save(tenant);

        if (room.getStatus() == null || room.getStatus() == RoomStatus.VACANT) {
            room.setStatus(RoomStatus.OCCUPIED);
            roomRepository.save(room);
        }

        evictTenantCaches(request.getRoomId(), savedTenant.getId());
        return tenantMapper.toResponse(savedTenant);
    }

    @Cacheable(value = "tenants", key = "#id")
    public TenantResponse getTenantById(final Integer id) {
        final var tenant =
                tenantRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));
        return tenantMapper.toResponse(tenant);
    }

    @Cacheable(value = "tenantsByRoom", key = "#roomId")
    public List<TenantResponse> getTenantsByRoomId(final Integer roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new AppException(ErrorCode.ROOM_NOT_FOUND);
        }

        final var tenants = tenantRepository.findByRoomIdOrderByStartDateDesc(roomId);
        return tenants.stream().map(tenantMapper::toResponse).toList();
    }

    public PageResponse<TenantResponse> getTenantsByRoomId(
            final Integer roomId, final Pageable pageable) {
        if (!roomRepository.existsById(roomId)) {
            throw new AppException(ErrorCode.ROOM_NOT_FOUND);
        }

        final var page = tenantRepository.findByRoomIdOrderByStartDateDesc(roomId, pageable);
        final var content = page.getContent().stream().map(tenantMapper::toResponse).toList();

        return PageResponse.<TenantResponse>builder()
                .content(content)
                .page(buildPageInfo(page))
                .message("Lấy danh sách khách thuê thành công")
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
    public TenantResponse endTenantContract(final Integer id) {
        final var tenant =
                tenantRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));

        if (tenant.getEndDate() != null) {
            throw new AppException(ErrorCode.TENANT_CONTRACT_ALREADY_ENDED);
        }

        tenant.setEndDate(LocalDate.now());
        final var updatedTenant = tenantRepository.save(tenant);

        updateRoomStatusIfNeeded(tenant);

        final var roomId = tenant.getRoom() != null ? tenant.getRoom().getId() : null;
        if (roomId != null) {
            evictTenantCaches(roomId, id);
        }
        return tenantMapper.toResponse(updatedTenant);
    }

    private void updateRoomStatusIfNeeded(final Tenant tenant) {
        final var room = tenant.getRoom();
        if (room == null) {
            return;
        }

        final var activeTenantsCount =
                tenantRepository.findByRoomIdOrderByStartDateDesc(room.getId()).stream()
                        .filter(t -> t.getEndDate() == null)
                        .count();

        if (activeTenantsCount == 0) {
            room.setStatus(RoomStatus.VACANT);
            roomRepository.save(room);
        }
    }

    @Caching(
            evict = {
                @CacheEvict(value = "tenants", key = "#tenantId"),
                @CacheEvict(value = "tenantsByRoom", key = "#roomId")
            })
    private void evictTenantCaches(final Integer roomId, final Integer tenantId) {
        // Method chỉ để evict cache, không có logic gì
    }
}
