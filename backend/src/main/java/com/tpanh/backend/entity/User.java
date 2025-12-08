package com.tpanh.backend.entity;

import com.tpanh.backend.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private static final int ID_LENGTH = 50;
    private static final int USERNAME_LENGTH = 50;
    private static final int PASSWORD_LENGTH = 255;
    private static final int ZALO_ID_LENGTH = 50;
    private static final int FULL_NAME_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = ID_LENGTH)
    private String id;

    @Column(length = USERNAME_LENGTH, unique = true)
    private String username; // Có thể NULL nếu là user Zalo

    @Column(length = PASSWORD_LENGTH)
    private String password; // Hash BCrypt, có thể NULL nếu login bằng Zalo

    @Column(name = "zalo_id", length = ZALO_ID_LENGTH, unique = true)
    private String zaloId; // ID từ Zalo, có thể NULL nếu là Admin

    @Column(name = "full_name", length = FULL_NAME_LENGTH, nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role roles;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
