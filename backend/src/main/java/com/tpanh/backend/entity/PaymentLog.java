package com.tpanh.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "payment_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private static final int ACTION_MAX_LENGTH = 30;
    private static final int STATUS_MAX_LENGTH = 20;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /** Action type: PAID, MARKED_OVERDUE, STATUS_CHANGED */
    @Column(nullable = false, length = ACTION_MAX_LENGTH)
    private String action;

    @Column(name = "old_status", length = STATUS_MAX_LENGTH)
    private String oldStatus;

    @Column(name = "new_status", length = STATUS_MAX_LENGTH)
    private String newStatus;

    private Integer amount;

    @Column(name = "performed_by")
    private String performedBy;

    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static PaymentLog create(
            final Invoice invoice,
            final String action,
            final String oldStatus,
            final String newStatus,
            final String performedBy,
            final String note) {
        return PaymentLog.builder()
                .invoice(invoice)
                .action(action)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .amount(invoice.getTotalAmount())
                .performedBy(performedBy)
                .note(note)
                .build();
    }
}
