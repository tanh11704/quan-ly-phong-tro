package com.tpanh.backend.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpanh.backend.dto.RegistrationRequest;
import com.tpanh.backend.dto.RegistrationResponse;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.UserStatus;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegistrationService {
    private static final String ACTIVATION_TOKEN_PREFIX = "activation:token:";
    private static final String TOKEN_USER_PREFIX = "activation:user:";
    private static final long TOKEN_EXPIRATION_SECONDS = 86400L; // 24 hours

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    public RegistrationService(
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final EmailService emailService,
            @Qualifier("customStringRedisTemplate") final RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public RegistrationResponse register(final RegistrationRequest request) {
        validateRegistrationRequest(request);

        final var user = createPendingUser(request);
        final var savedUser = userRepository.save(user);

        final var activationToken = generateActivationToken();
        storeActivationToken(savedUser.getId(), activationToken);

        emailService.sendActivationEmail(
                savedUser.getEmail(), savedUser.getFullName(), activationToken);

        log.info(
                "User registered successfully: userId={}, email={}",
                savedUser.getId(),
                savedUser.getEmail());

        return RegistrationResponse.builder()
                .userId(savedUser.getId())
                .message("Đăng ký thành công. Vui lòng kiểm tra email để kích hoạt tài khoản.")
                .build();
    }

    @Transactional
    public void activateAccount(final String token) {
        final var userId = getUserIdFromToken(token);
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        final var user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setActive(true);
        userRepository.save(user);

        // Xóa token sau khi kích hoạt thành công
        deleteActivationToken(token, userId);

        log.info("User activated successfully: userId={}", userId);
    }

    private void validateRegistrationRequest(final RegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private User createPendingUser(final RegistrationRequest request) {
        return User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .roles(Role.MANAGER)
                .status(UserStatus.PENDING)
                .active(false)
                .build();
    }

    private String generateActivationToken() {
        return UUID.randomUUID().toString();
    }

    private void storeActivationToken(final String userId, final String token) {
        redisTemplate
                .opsForValue()
                .set(
                        ACTIVATION_TOKEN_PREFIX + token,
                        userId,
                        java.time.Duration.ofSeconds(TOKEN_EXPIRATION_SECONDS));
        redisTemplate
                .opsForValue()
                .set(
                        TOKEN_USER_PREFIX + userId,
                        token,
                        java.time.Duration.ofSeconds(TOKEN_EXPIRATION_SECONDS));
    }

    private String getUserIdFromToken(final String token) {
        return redisTemplate.opsForValue().get(ACTIVATION_TOKEN_PREFIX + token);
    }

    private void deleteActivationToken(final String token, final String userId) {
        redisTemplate.delete(ACTIVATION_TOKEN_PREFIX + token);
        redisTemplate.delete(TOKEN_USER_PREFIX + userId);
    }
}
