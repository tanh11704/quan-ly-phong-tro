package com.tpanh.backend.entity;

import com.tpanh.backend.enums.InvitationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "tenant_invitations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantInvitation {

    private static final int STATUS_LENGTH = 20;

    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String email;

    @Column(name = "is_contract_holder", nullable = false)
    private boolean isContractHolder;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = STATUS_LENGTH)
    private InvitationStatus status;

    @Column(name = "expired_at", nullable = false)
    private OffsetDateTime expiredAt;

    @Column(name = "invited_by")
    private String invitedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
