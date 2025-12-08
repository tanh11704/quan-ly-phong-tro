package com.tpanh.backend.service;

import com.tpanh.backend.dto.TenantCreationRequest;
import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.entity.Tenant;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;

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

        if (room.getStatus() == null || "VACANT".equals(room.getStatus())) {
            room.setStatus("OCCUPIED");
            roomRepository.save(room);
        }

        return toResponse(savedTenant);
    }

    public TenantResponse getTenantById(final Integer id) {
        final var tenant =
                tenantRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));
        return toResponse(tenant);
    }

    public List<TenantResponse> getTenantsByRoomId(final Integer roomId) {
        final var room =
                roomRepository
                        .findById(roomId)
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        final var tenants = tenantRepository.findByRoomIdOrderByStartDateDesc(roomId);
        return tenants.stream().map(this::toResponse).toList();
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

        final var room = tenant.getRoom();
        if (room != null) {
            final var activeTenantsCount =
                    tenantRepository.findByRoomIdOrderByStartDateDesc(room.getId()).stream()
                            .filter(t -> t.getEndDate() == null)
                            .count();

            if (activeTenantsCount == 0) {
                room.setStatus("VACANT");
                roomRepository.save(room);
            }
        }

        return toResponse(updatedTenant);
    }

    private TenantResponse toResponse(final Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .roomId(tenant.getRoom() != null ? tenant.getRoom().getId() : null)
                .roomNo(tenant.getRoom() != null ? tenant.getRoom().getRoomNo() : null)
                .name(tenant.getName())
                .phone(tenant.getPhone())
                .isContractHolder(tenant.getIsContractHolder())
                .startDate(tenant.getStartDate())
                .endDate(tenant.getEndDate())
                .build();
    }
}
