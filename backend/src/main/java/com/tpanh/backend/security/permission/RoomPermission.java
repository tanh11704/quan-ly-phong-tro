package com.tpanh.backend.security.permission;

import com.tpanh.backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("roomPermission")
@RequiredArgsConstructor
public class RoomPermission extends AbstractPermission {

    private final RoomRepository roomRepository;

    public boolean canAccessRoom(final Integer roomId, final Authentication authentication) {
        final var principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (principal.hasRole("ADMIN")) {
            return true;
        }

        return roomRepository.existsByIdAndBuildingManagerId(roomId, principal.getUserId());
    }
}
