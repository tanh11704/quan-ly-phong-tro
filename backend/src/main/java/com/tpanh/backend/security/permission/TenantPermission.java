package com.tpanh.backend.security.permission;

import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("tenantPermission")
@RequiredArgsConstructor
public class TenantPermission extends AbstractPermission {

    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;

    public boolean canAccessTenant(final Integer tenantId, final Authentication authentication) {
        final var principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (principal.hasRole("ADMIN")) {
            return true;
        }

        return tenantRepository.existsByIdAndRoomBuildingManagerId(tenantId, principal.getUserId());
    }

    public boolean canAccessRoomTenants(final Integer roomId, final Authentication authentication) {
        final var principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (principal.hasRole("ADMIN")) {
            return true;
        }

        return roomRepository.existsByIdAndBuildingManagerId(roomId, principal.getUserId());
    }

    public boolean canAccessBuildingTenants(
            final Integer buildingId, final Authentication authentication) {
        final var principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (principal.hasRole("ADMIN")) {
            return true;
        }

        return roomRepository.existsByBuildingIdAndBuildingManagerId(
                buildingId, principal.getUserId());
    }
}
