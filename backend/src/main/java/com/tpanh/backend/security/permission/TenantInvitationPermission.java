package com.tpanh.backend.security.permission;

import com.tpanh.backend.repository.TenantInvitationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("tenantInvitationPermission")
@RequiredArgsConstructor
public class TenantInvitationPermission extends AbstractPermission {

    private final TenantInvitationRepository invitationRepository;
    private final RoomPermission roomPermission;

    public boolean canCancel(final UUID invitationId, final Authentication authentication) {
        final var invitation = invitationRepository.findById(invitationId).orElse(null);
        if (invitation == null) {
            return false;
        }
        return roomPermission.canAccessRoom(invitation.getRoom().getId(), authentication);
    }
}
