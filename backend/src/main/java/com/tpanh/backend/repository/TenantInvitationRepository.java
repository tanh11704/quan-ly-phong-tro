package com.tpanh.backend.repository;

import com.tpanh.backend.entity.TenantInvitation;
import com.tpanh.backend.enums.InvitationStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantInvitationRepository extends JpaRepository<TenantInvitation, UUID> {
    boolean existsByRoomIdAndEmailAndStatus(Integer roomId, String email, InvitationStatus status);
}
