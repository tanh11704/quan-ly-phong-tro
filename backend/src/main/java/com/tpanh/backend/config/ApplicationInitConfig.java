package com.tpanh.backend.config;

import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initAdminUser() {
        // Kiểm tra xem admin đã tồn tại chưa
        final var existingAdminOpt = userRepository.findByUsername(adminProperties.getUsername());

        if (existingAdminOpt.isPresent()) {
            log.info("Tài khoản admin đã tồn tại: {}", adminProperties.getUsername());
            return;
        }

        // Tạo admin mới nếu chưa có
        final var admin =
                User.builder()
                        .username(adminProperties.getUsername())
                        .password(passwordEncoder.encode(adminProperties.getPassword()))
                        .fullName(adminProperties.getFullName())
                        .roles(new HashSet<>(Set.of(Role.ADMIN)))
                        .active(true)
                        .build();

        userRepository.save(admin);
        log.info(
                "Đã tạo tài khoản admin mặc định - Username: {}, Full Name: {}",
                adminProperties.getUsername(),
                adminProperties.getFullName());
    }
}
