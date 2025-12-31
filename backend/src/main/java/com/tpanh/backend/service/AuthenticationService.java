package com.tpanh.backend.service;

import com.tpanh.backend.client.ZaloIdentityClient;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.AuthenticationResponse;
import com.tpanh.backend.dto.ExchangeTokenRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.UserStatus;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ZaloIdentityClient zaloIdentityClient;

    public AuthenticationResponse authenticate(final AuthenticationRequest request) {
        final var user =
                userRepository
                        .findByUsername(request.getUsername())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        validateUserCanLogin(user);

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthenticationResponse outboundAuthenticate(final ExchangeTokenRequest request) {
        // Gọi API Zalo để lấy thông tin user
        final var zaloUserInfo = zaloIdentityClient.getUserInfo(request.getToken());

        // Kiểm tra xem user đã tồn tại chưa
        final var existingUserOpt = userRepository.findByZaloId(zaloUserInfo.getId());

        final User user;
        if (existingUserOpt.isPresent()) {
            // Trường hợp 1: Đã từng vào -> Lấy User ra và validate
            user = existingUserOpt.get();
            validateUserCanLogin(user);
        } else {
            // Trường hợp 2: Lần đầu vào -> Tự động INSERT user mới
            user = createNewZaloUser(zaloUserInfo);
            userRepository.save(user);
            log.info("Đã tạo user mới từ Zalo: {}", user.getId());
        }

        return generateAuthResponse(user);
    }

    private void validateUserCanLogin(final User user) {
        if (user.getStatus() == UserStatus.PENDING) {
            throw new AppException(ErrorCode.USER_PENDING_ACTIVATION);
        }
        if (!Boolean.TRUE.equals(user.getActive()) || !user.isLoginAllowed()) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }
    }

    private AuthenticationResponse generateAuthResponse(final User user) {
        final var token = jwtService.generateToken(user.getId(), user.getRoles());
        return new AuthenticationResponse(token, user.getRoles());
    }

    private User createNewZaloUser(final ZaloIdentityClient.ZaloUserInfo zaloUserInfo) {
        return User.builder()
                .zaloId(zaloUserInfo.getId())
                .fullName(zaloUserInfo.getName())
                .roles(new HashSet<>(Set.of(Role.USER)))
                .status(UserStatus.ACTIVE)
                .active(true)
                .build();
    }
}
