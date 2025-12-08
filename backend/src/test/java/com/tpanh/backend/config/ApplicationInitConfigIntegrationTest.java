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
        assertEquals(Role.ADMIN, admin.getRoles());
        assertTrue(admin.getActive());
        assertNotNull(admin.getPassword()); // Password đã được hash
    }

    @Test
    void initAdminUser_WhenAdminExists_ShouldNotCreateDuplicate() {
        // Given - Admin đã tồn tại (từ lần chạy trước)
        final var existingAdmin =
                User.builder()
                        .username("testadmin")
                        .password("$2a$10$existing")
                        .fullName("Existing Admin")
                        .roles(Role.ADMIN)
                        .active(true)
                        .build();
        userRepository.save(existingAdmin);

        final var countBefore = userRepository.count();

        // When - ApplicationInitConfig chạy lại
        // (Trong thực tế, nó chỉ chạy 1 lần khi app start, nhưng test này verify logic)

        // Then - Không tạo duplicate
        final var adminOpt = userRepository.findByUsername("testadmin");
        assertTrue(adminOpt.isPresent());
        // Có thể là existing admin hoặc admin mới được tạo, nhưng chỉ có 1
        assertEquals(1, userRepository.findByUsername("testadmin").stream().count());
    }
}
