package com.tpanh.backend.security.permission;

import com.tpanh.backend.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("buildingPermission")
@RequiredArgsConstructor
public class BuildingPermission extends AbstractPermission {
    private final BuildingRepository buildingRepository;

    public boolean canAccessBuilding(
            final Integer buildingId, final Authentication authentication) {
        final var principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (principal.hasRole("ADMIN")) {
            return true;
        }

        return buildingRepository.existsByIdAndManagerId(buildingId, principal.getUserId());
    }
}
