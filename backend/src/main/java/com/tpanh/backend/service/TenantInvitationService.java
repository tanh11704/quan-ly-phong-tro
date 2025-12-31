package com.tpanh.backend.service;

import com.tpanh.backend.dto.TenantInvitationRequest;
import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.entity.Tenant;
import com.tpanh.backend.entity.TenantInvitation;
import com.tpanh.backend.enums.InvitationStatus;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantInvitationRepository;
import com.tpanh.backend.repository.TenantRepository;
import com.tpanh.backend.security.UserPrincipal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantInvitationService {

    private final TenantInvitationRepository invitationRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final TenantService tenantService;
    private final com.tpanh.backend.repository.UserRepository userRepository;

    private static final int DEFAULT_EXPIRATION_DAYS = 7;

    @Transactional
    @PreAuthorize("@roomPermission.canAccessRoom(#request.roomId, authentication)")
    public void inviteTenant(final TenantInvitationRequest request, final String invitedBy) {
        final var room =
                roomRepository
                        .findById(request.getRoomId())
                        .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if (request.isContractHolder()) {
            validateNoActiveContractHolder(room.getId());
        }

        if (invitationRepository.existsByRoomIdAndEmailAndStatus(
                request.getRoomId(), request.getEmail(), InvitationStatus.PENDING)) {
            throw new AppException(ErrorCode.INVITATION_PENDING_EXISTS);
        }

        final var invitation =
                TenantInvitation.builder()
                        .id(UUID.randomUUID())
                        .room(room)
                        .email(request.getEmail())
                        .isContractHolder(request.isContractHolder())
                        .contractEndDate(request.getContractEndDate())
                        .status(InvitationStatus.PENDING)
                        .expiredAt(OffsetDateTime.now().plusDays(DEFAULT_EXPIRATION_DAYS))
                        .invitedBy(invitedBy)
                        .build();

        invitationRepository.save(invitation);
    }

    @Transactional
    public TenantResponse acceptInvitation(final UUID token, final UserPrincipal currentUser) {
        final var invitation =
                invitationRepository
                        .findById(token)
                        .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));

        final var user =
                userRepository
                        .findById(currentUser.getUserId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        validateInvitationForAcceptance(invitation, user.getEmail());

        if (invitation.isContractHolder() || Boolean.TRUE.equals(invitation.isContractHolder())) {
            validateNoActiveContractHolder(invitation.getRoom().getId());
        }

        final var tenant = buildTenantFromInvitationAndUser(invitation, user);
        final var response = tenantService.createTenant(tenant);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        return response;
    }

    private Tenant buildTenantFromInvitationAndUser(
            final TenantInvitation invitation, final com.tpanh.backend.entity.User user) {
        final var tenant = new Tenant();
        tenant.setRoom(invitation.getRoom());
        tenant.setUserId(user.getId());
        tenant.setEmail(user.getEmail());
        tenant.setName(user.getFullName());
        tenant.setPhone(user.getPhoneNumber());
        tenant.setIsContractHolder(invitation.isContractHolder());
        tenant.setStartDate(LocalDate.now());
        tenant.setContractEndDate(invitation.getContractEndDate());
        return tenant;
    }

    @Transactional
    @PreAuthorize("@tenantInvitationPermission.canCancel(#id, authentication)")
    public void cancelInvitation(final UUID id) {
        final var invitation =
                invitationRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            // Already handled or expired, but explicit cancel should likely fail if already
            // handled?
            // If expired, maybe redundant to cancel?
            // "Cannot cancel processed invitation"
            // If already accepted > error.
            if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
                throw new AppException(ErrorCode.INVITATION_ALREADY_ACCEPTED);
            }
            // If expired, can we cancel? Yes, explicit cancel is fine.
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        invitationRepository.save(invitation);
    }

    private void validateNoActiveContractHolder(final Integer roomId) {
        tenantRepository
                .findByRoomIdAndIsContractHolderTrueAndEndDateIsNull(roomId)
                .ifPresent(
                        h -> {
                            throw new AppException(ErrorCode.CONTRACT_HOLDER_ALREADY_EXISTS);
                        });
    }

    private void validateInvitationForAcceptance(
            final TenantInvitation invitation, final String userEmail) {
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
                throw new AppException(ErrorCode.INVITATION_ALREADY_ACCEPTED);
            }
            if (invitation.getStatus() == InvitationStatus.CANCELLED) {
                throw new AppException(ErrorCode.INVITATION_CANCELLED);
            }
            // EXPIRED
            throw new AppException(ErrorCode.INVITATION_EXPIRED);
        }

        if (invitation.getExpiredAt().isBefore(OffsetDateTime.now())) {
            // Lazy expiration check
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new AppException(ErrorCode.INVITATION_EXPIRED);
        }

        if (userEmail == null || !invitation.getEmail().equalsIgnoreCase(userEmail)) {
            throw new AppException(ErrorCode.INVITATION_EMAIL_MISMATCH);
        }
    }
}
