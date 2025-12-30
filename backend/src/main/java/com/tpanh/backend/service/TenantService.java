package com.tpanh.backend.service;

import com.tpanh.backend.dto.PageResponse;
import com.tpanh.backend.dto.TenantCreationRequest;
import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.dto.TenantUpdateRequest;
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

        final boolean isContractHolder = Boolean.TRUE.equals(request.getIsContractHolder());
        if (isContractHolder) {
            validateNoActiveContractHolder(request.getRoomId(), null);
        }

        final var tenant = buildTenantFromRequest(request, room, isContractHolder);
        final var savedTenant = tenantRepository.save(tenant);
        updateRoomToOccupiedIfNeeded(room);
        evictTenantCaches(request.getRoomId(), savedTenant.getId());
        return tenantMapper.toResponse(savedTenant);
    }

    private Tenant buildTenantFromRequest(
            final TenantCreationRequest request,
            final com.tpanh.backend.entity.Room room,
            final boolean isContractHolder) {
        final var tenant = new Tenant();
        tenant.setRoom(room);
        tenant.setName(request.getName());
        tenant.setPhone(request.getPhone());
        tenant.setEmail(request.getEmail());
        tenant.setIsContractHolder(isContractHolder);
        tenant.setStartDate(
                request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        tenant.setContractEndDate(request.getContractEndDate());
        tenant.setEndDate(null);
        return tenant;
    }

    private void updateRoomToOccupiedIfNeeded(final com.tpanh.backend.entity.Room room) {
        if (room.getStatus() == null || room.getStatus() == RoomStatus.VACANT) {
            room.setStatus(RoomStatus.OCCUPIED);
            roomRepository.save(room);
        }
    }

    @Cacheable(value = "tenants", key = "#p0")
    public TenantResponse getTenantById(final Integer id) {
        final var tenant =
                tenantRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));
        return tenantMapper.toResponse(tenant);
    }

    @Cacheable(value = "tenantsByRoom", key = "#p0")
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

    @Transactional
    @CacheEvict(value = "tenants", key = "#p0")
    public TenantResponse updateTenant(final Integer id, final TenantUpdateRequest request) {
        final var tenant =
                tenantRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));

        validateContractHolderChange(tenant, request);
        applyTenantUpdates(tenant, request);

        final var updatedTenant = tenantRepository.save(tenant);
        evictCachesIfRoomExists(tenant, id);
        return tenantMapper.toResponse(updatedTenant);
    }

    private void validateContractHolderChange(final Tenant tenant, final TenantUpdateRequest req) {
        if (Boolean.TRUE.equals(req.getIsContractHolder())
                && !Boolean.TRUE.equals(tenant.getIsContractHolder())
                && tenant.getRoom() != null
                && tenant.getEndDate() == null) {
            validateNoActiveContractHolder(tenant.getRoom().getId(), tenant.getId());
        }
    }

    private void applyTenantUpdates(final Tenant tenant, final TenantUpdateRequest request) {
        if (request.getName() != null) {
            tenant.setName(request.getName());
        }
        if (request.getPhone() != null) {
            tenant.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            tenant.setEmail(request.getEmail());
        }
        if (request.getIsContractHolder() != null) {
            tenant.setIsContractHolder(request.getIsContractHolder());
        }
    }

    private void evictCachesIfRoomExists(final Tenant tenant, final Integer tenantId) {
        final var roomId = tenant.getRoom() != null ? tenant.getRoom().getId() : null;
        if (roomId != null) {
            evictTenantCaches(roomId, tenantId);
        }
    }

    public PageResponse<TenantResponse> getTenants(
            final Integer buildingId,
            final Integer roomId,
            final Boolean active,
            final Pageable pageable) {
        final Page<Tenant> page;
        if (buildingId != null) {
            page = tenantRepository.findByRoomBuildingId(buildingId, roomId, active, pageable);
        } else if (roomId != null) {
            page = tenantRepository.findByRoomIdWithFilter(roomId, active, pageable);
        } else {
            page = tenantRepository.findAllWithFilter(active, pageable);
        }

        final var content = page.getContent().stream().map(tenantMapper::toResponse).toList();

        return PageResponse.<TenantResponse>builder()
                .content(content)
                .page(buildPageInfo(page))
                .message("Lấy danh sách khách thuê thành công")
                .build();
    }

    private void validateNoActiveContractHolder(
            final Integer roomId, final Integer excludeTenantId) {
        tenantRepository
                .findByRoomIdAndIsContractHolderTrueAndEndDateIsNull(roomId)
                .ifPresent(
                        existingHolder -> {
                            if (excludeTenantId == null
                                    || !excludeTenantId.equals(existingHolder.getId())) {
                                throw new AppException(ErrorCode.CONTRACT_HOLDER_ALREADY_EXISTS);
                            }
                        });
    }

    @Caching(
            evict = {
                @CacheEvict(value = "tenants", key = "#p1"),
                @CacheEvict(value = "tenantsByRoom", key = "#p0")
            })
    private void evictTenantCaches(final Integer roomId, final Integer tenantId) {
        // Method chỉ để evict cache, không có logic gì
    }
}
