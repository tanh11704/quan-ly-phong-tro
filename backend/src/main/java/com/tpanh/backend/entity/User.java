package com.tpanh.backend.entity;

import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.UserStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
    private static final int EMAIL_LENGTH = 255;

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

    // Multi-role support: User can have multiple roles
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(length = EMAIL_LENGTH, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    public void addRole(final Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    public boolean hasRole(final Role role) {
        return roles != null && roles.contains(role);
    }

    public boolean hasAnyRole(final Role... checkRoles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return Arrays.stream(checkRoles).anyMatch(roles::contains);
    }

    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public boolean isLoginAllowed() {
        return Boolean.TRUE.equals(active) && status == UserStatus.ACTIVE;
    }

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
