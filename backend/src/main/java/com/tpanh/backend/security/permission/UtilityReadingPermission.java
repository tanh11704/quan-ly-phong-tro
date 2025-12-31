package com.tpanh.backend.security.permission;

import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.UtilityReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("utilityReadingPermission")
@RequiredArgsConstructor
public class UtilityReadingPermission extends AbstractPermission {

    private final UtilityReadingRepository utilityReadingRepository;
    private final RoomRepository roomRepository;

    public boolean canAccessUtilityReading(
            final Integer readingId, final Authentication authentication) {
        final var principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (principal.hasRole("ADMIN")) {
            return true;
        }

        return utilityReadingRepository.existsByIdAndRoomBuildingManagerId(
                readingId, principal.getUserId());
    }

    public boolean canAccessRoomUtilityReadings(
            final Integer roomId, final Authentication authentication) {
        final var principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (principal.hasRole("ADMIN")) {
            return true;
        }

        return roomRepository.existsByIdAndBuildingManagerId(roomId, principal.getUserId());
    }

    public boolean canAccessBuildingUtilityReadings(
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
