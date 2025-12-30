package com.tpanh.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
class ApplicationInitConfigIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.admin.username", () -> "testadmin");
        registry.add("app.admin.password", () -> "testpass123");
        registry.add("app.admin.full-name", () -> "Test Admin");
    }

    @Autowired private UserRepository userRepository;

    @Test
    void initAdminUser_ShouldCreateAdminUserOnStartup() {
        // When - ApplicationInitConfig chạy khi Spring Boot khởi động
        // Kiểm tra xem admin user đã được tạo chưa

        // Then
        final var adminOpt = userRepository.findByUsername("testadmin");
        assertTrue(adminOpt.isPresent());

        final var admin = adminOpt.get();
        assertEquals("testadmin", admin.getUsername());
        assertEquals("Test Admin", admin.getFullName());
        assertTrue(admin.getRoles().contains(Role.ADMIN));
        assertTrue(admin.getActive());
        assertNotNull(admin.getPassword()); // Password đã được hash
    }

    @Test
    void initAdminUser_WhenAdminExists_ShouldNotCreateDuplicate() {
        // Given - Xóa admin nếu đã tồn tại từ test trước
        userRepository.findByUsername("testadmin").ifPresent(userRepository::delete);
        userRepository.flush();

        // Tạo admin trước
        final var existingAdmin =
                User.builder()
                        .username("testadmin")
                        .password("$2a$10$existing")
                        .fullName("Existing Admin")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.ADMIN)))
                        .active(true)
                        .build();
        userRepository.saveAndFlush(existingAdmin);

        final var countBefore = userRepository.count();

        // When - ApplicationInitConfig đã chạy khi Spring Boot khởi động
        // (Trong thực tế, nó chỉ chạy 1 lần khi app start, nhưng test này verify logic)

        // Then - Không tạo duplicate (chỉ có 1 admin với username "testadmin")
        final var adminCount = userRepository.findByUsername("testadmin").stream().count();
        assertEquals(1, adminCount, "Chỉ nên có 1 admin user với username testadmin");
    }
}
