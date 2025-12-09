package com.tpanh.backend.service;

import com.tpanh.backend.dto.UserDTO;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserDTO getCurrentUser() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        final var userId = authentication.getName();
        final var user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return toDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        final var users = userRepository.findAll();
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public UserDTO toggleUserActive(final String userId) {
        final var user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setActive(!user.getActive());
        final var savedUser = userRepository.save(user);

        log.info(
                "Admin đã {} tài khoản: {} (ID: {})",
                savedUser.getActive() ? "mở khóa" : "khóa",
                savedUser.getFullName(),
                savedUser.getId());

        return toDTO(savedUser);
    }

    private UserDTO toDTO(final User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRoles())
                .active(user.getActive())
                .build();
    }
}
