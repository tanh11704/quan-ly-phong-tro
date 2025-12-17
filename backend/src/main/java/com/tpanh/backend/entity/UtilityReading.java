package com.tpanh.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "utility_readings")
@Data
public class UtilityReading {

    private static final int IMAGE_EVIDENCE_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String month; // Format: 'YYYY-MM' (VD: '2025-01')

    @Column(name = "electric_index")
    private Integer electricIndex; // Chỉ số điện hiện tại

    @Column(name = "water_index")
    private Integer waterIndex; // Chỉ số nước hiện tại

    @Column(name = "image_evidence", length = IMAGE_EVIDENCE_MAX_LENGTH)
    private String imageEvidence; // URL hoặc path đến ảnh đồng hồ

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
