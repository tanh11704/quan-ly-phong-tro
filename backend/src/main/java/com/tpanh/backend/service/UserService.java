package com.tpanh.backend.service;

import com.tpanh.backend.dto.PageResponse;
import com.tpanh.backend.dto.UserDTO;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.UserMapper;
import com.tpanh.backend.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public UserDTO getCurrentUser() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = authentication.getName();
        final var user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        final var users = userRepository.findAll();
        return users.stream().map(userMapper::toDTO).toList();
    }

    public PageResponse<UserDTO> getAllUsers(final Pageable pageable) {
        final var page = userRepository.findAll(pageable);
        final var content = page.getContent().stream().map(userMapper::toDTO).toList();

        return PageResponse.<UserDTO>builder()
                .content(content)
                .page(buildPageInfo(page))
                .message("Lấy danh sách người dùng thành công")
                .build();
    }

    private PageResponse.PageInfo buildPageInfo(final Page<?> page) {
        return PageResponse.PageInfo.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
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

        return userMapper.toDTO(savedUser);
    }
}
