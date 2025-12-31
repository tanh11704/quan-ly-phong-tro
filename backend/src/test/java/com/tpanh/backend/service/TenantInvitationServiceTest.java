package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tpanh.backend.dto.TenantInvitationRequest;
import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.Tenant;
import com.tpanh.backend.entity.TenantInvitation;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.InvitationStatus;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantInvitationRepository;
import com.tpanh.backend.repository.TenantRepository;
import com.tpanh.backend.repository.UserRepository;
import com.tpanh.backend.security.UserPrincipal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantInvitationServiceTest {

    @Mock private TenantInvitationRepository invitationRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private TenantService tenantService;
    @Mock private UserRepository userRepository;

    @InjectMocks private TenantInvitationService invitationService;

    private Room room;
    private User user;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(1);

        user = User.builder().id("user-1").email("test@example.com").fullName("Test User").build();

        userPrincipal = new UserPrincipal(user.getId(), null);
    }

    @Test
    void inviteTenant_Success() {
        TenantInvitationRequest request = new TenantInvitationRequest();
        request.setRoomId(1);
        request.setEmail("invitee@example.com");

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(invitationRepository.existsByRoomIdAndEmailAndStatus(
                        1, "invitee@example.com", InvitationStatus.PENDING))
                .thenReturn(false);

        invitationService.inviteTenant(request, "manager-1");

        verify(invitationRepository).save(any(TenantInvitation.class));
    }

    @Test
    void inviteTenant_PendingExists_ThrowsException() {
        TenantInvitationRequest request = new TenantInvitationRequest();
        request.setRoomId(1);
        request.setEmail("invitee@example.com");

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(invitationRepository.existsByRoomIdAndEmailAndStatus(
                        1, "invitee@example.com", InvitationStatus.PENDING))
                .thenReturn(true);

        assertThrows(
                AppException.class, () -> invitationService.inviteTenant(request, "manager-1"));
    }

    @Test
    void acceptInvitation_Success() {
        UUID token = UUID.randomUUID();
        TenantInvitation invitation =
                TenantInvitation.builder()
                        .id(token)
                        .room(room)
                        .email("test@example.com")
                        .status(InvitationStatus.PENDING)
                        .expiredAt(OffsetDateTime.now().plusDays(1))
                        .build();

        when(invitationRepository.findById(token)).thenReturn(Optional.of(invitation));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(tenantService.createTenant(any(Tenant.class))).thenReturn(new TenantResponse());

        invitationService.acceptInvitation(token, userPrincipal);

        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        verify(tenantService).createTenant(any(Tenant.class));
    }

    @Test
    void acceptInvitation_EmailMismatch_ThrowsException() {
        UUID token = UUID.randomUUID();
        TenantInvitation invitation =
                TenantInvitation.builder()
                        .id(token)
                        .room(room)
                        .email("other@example.com")
                        .status(InvitationStatus.PENDING)
                        .expiredAt(OffsetDateTime.now().plusDays(1))
                        .build();

        when(invitationRepository.findById(token)).thenReturn(Optional.of(invitation));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        assertThrows(
                AppException.class, () -> invitationService.acceptInvitation(token, userPrincipal));
    }

    @Test
    void acceptInvitation_Expired_ThrowsException() {
        UUID token = UUID.randomUUID();
        TenantInvitation invitation =
                TenantInvitation.builder()
                        .id(token)
                        .room(room)
                        .email("test@example.com")
                        .status(InvitationStatus.PENDING)
                        .expiredAt(OffsetDateTime.now().minusDays(1))
                        .build();

        when(invitationRepository.findById(token)).thenReturn(Optional.of(invitation));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        assertThrows(
                AppException.class, () -> invitationService.acceptInvitation(token, userPrincipal));

        assertEquals(InvitationStatus.EXPIRED, invitation.getStatus());
        verify(invitationRepository).save(invitation);
    }
}
