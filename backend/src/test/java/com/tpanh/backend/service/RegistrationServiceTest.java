package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpanh.backend.dto.RegistrationRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.enums.UserStatus;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    private static final String USERNAME = "manager01";
    private static final String PASSWORD = "password123";
    private static final String FULL_NAME = "Nguyễn Văn A";
    private static final String EMAIL = "manager@example.com";
    private static final String HASHED_PASSWORD = "$2a$10$hashedpassword";
    private static final String USER_ID = "user-id-123";
    private static final String ACTIVATION_TOKEN = "activation-token-123";

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private RegistrationService registrationService;

    private RegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequest();
        registrationRequest.setUsername(USERNAME);
        registrationRequest.setPassword(PASSWORD);
        registrationRequest.setFullName(FULL_NAME);
        registrationRequest.setEmail(EMAIL);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void register_WithValidRequest_ShouldCreatePendingUser() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class)))
                .thenAnswer(
                        invocation -> {
                            final var user = invocation.getArgument(0, User.class);
                            user.setId(USER_ID);
                            return user;
                        });

        // When
        final var response = registrationService.register(registrationRequest);

        // Then
        assertNotNull(response);
        assertEquals(USER_ID, response.getUserId());
        assertNotNull(response.getMessage());

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        final var savedUser = userCaptor.getValue();

        assertEquals(USERNAME, savedUser.getUsername());
        assertEquals(HASHED_PASSWORD, savedUser.getPassword());
        assertEquals(FULL_NAME, savedUser.getFullName());
        assertEquals(EMAIL, savedUser.getEmail());
        assertTrue(savedUser.getRoles().contains(Role.MANAGER));
        assertEquals(UserStatus.PENDING, savedUser.getStatus());
        assertEquals(false, savedUser.getActive());

        verify(emailService).sendActivationEmail(eq(EMAIL), eq(FULL_NAME), anyString());
        verify(valueOperations).set(anyString(), eq(USER_ID), any());
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Given
        final var existingUser = User.builder().username(USERNAME).build();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class,
                        () -> registrationService.register(registrationRequest));
        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(emailService, never()).sendActivationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        final var existingUser = User.builder().email(EMAIL).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class,
                        () -> registrationService.register(registrationRequest));
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendActivationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void activateAccount_WithValidToken_ShouldActivateUser() {
        // Given
        final var pendingUser =
                User.builder()
                        .id(USER_ID)
                        .username(USERNAME)
                        .email(EMAIL)
                        .fullName(FULL_NAME)
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.MANAGER)))
                        .status(UserStatus.PENDING)
                        .active(false)
                        .build();

        when(valueOperations.get(ACTIVATION_TOKEN_PREFIX + ACTIVATION_TOKEN)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(User.class))).thenReturn(pendingUser);

        // When
        registrationService.activateAccount(ACTIVATION_TOKEN);

        // Then
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        final var savedUser = userCaptor.getValue();

        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
        assertEquals(true, savedUser.getActive());
        verify(redisTemplate).delete(ACTIVATION_TOKEN_PREFIX + ACTIVATION_TOKEN);
        verify(redisTemplate).delete(TOKEN_USER_PREFIX + USER_ID);
    }

    @Test
    void activateAccount_WithInvalidToken_ShouldThrowException() {
        // Given
        when(valueOperations.get(ACTIVATION_TOKEN_PREFIX + ACTIVATION_TOKEN)).thenReturn(null);

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class,
                        () -> registrationService.activateAccount(ACTIVATION_TOKEN));
        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void activateAccount_WithAlreadyActiveUser_ShouldThrowException() {
        // Given
        final var activeUser =
                User.builder()
                        .id(USER_ID)
                        .username(USERNAME)
                        .email(EMAIL)
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.MANAGER)))
                        .status(UserStatus.ACTIVE)
                        .active(true)
                        .build();

        when(valueOperations.get(ACTIVATION_TOKEN_PREFIX + ACTIVATION_TOKEN)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(activeUser));

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class,
                        () -> registrationService.activateAccount(ACTIVATION_TOKEN));
        assertEquals(ErrorCode.USER_ALREADY_ACTIVE, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void activateAccount_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(valueOperations.get(ACTIVATION_TOKEN_PREFIX + ACTIVATION_TOKEN)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class,
                        () -> registrationService.activateAccount(ACTIVATION_TOKEN));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    private static final String ACTIVATION_TOKEN_PREFIX = "activation:token:";
    private static final String TOKEN_USER_PREFIX = "activation:user:";
}
