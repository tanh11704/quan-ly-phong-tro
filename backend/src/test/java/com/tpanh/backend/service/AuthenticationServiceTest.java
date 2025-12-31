package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpanh.backend.client.ZaloIdentityClient;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.ExchangeTokenRequest;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "$2a$10$hashedpassword";
    private static final String USER_ID = "user-id-123";
    private static final String ZALO_TOKEN = "zalo-access-token";
    private static final String ZALO_ID = "zalo-id-123";
    private static final String ZALO_NAME = "Zalo User";

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private ZaloIdentityClient zaloIdentityClient;

    @InjectMocks private AuthenticationService authenticationService;

    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        activeUser =
                User.builder()
                        .id(USER_ID)
                        .username(USERNAME)
                        .password(HASHED_PASSWORD)
                        .fullName("Test User")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.ADMIN)))
                        .active(true)
                        .build();

        inactiveUser =
                User.builder()
                        .id(USER_ID)
                        .username(USERNAME)
                        .password(HASHED_PASSWORD)
                        .fullName("Test User")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.ADMIN)))
                        .active(false)
                        .build();
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnToken() {
        // Given
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(eq(USER_ID), any())).thenReturn("jwt-token");

        // When
        final var response = authenticationService.authenticate(request);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertTrue(response.getRoles().contains(Role.ADMIN));
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(PASSWORD, HASHED_PASSWORD);
        verify(jwtService).generateToken(eq(USER_ID), any());
    }

    @Test
    void authenticate_WithInvalidUsername_ShouldThrowException() {
        // Given
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> authenticationService.authenticate(request));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_WithInvalidPassword_ShouldThrowException() {
        // Given
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        request.setPassword("wrong-password");

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong-password", HASHED_PASSWORD)).thenReturn(false);

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> authenticationService.authenticate(request));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches("wrong-password", HASHED_PASSWORD);
    }

    @Test
    void authenticate_WithInactiveUser_ShouldThrowException() {
        // Given
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(inactiveUser));

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> authenticationService.authenticate(request));
        assertEquals(ErrorCode.USER_INACTIVE, exception.getErrorCode());
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void outboundAuthenticate_WithNewZaloUser_ShouldCreateUserAndReturnToken() {
        // Given
        final var request = new ExchangeTokenRequest();
        request.setToken(ZALO_TOKEN);

        final var zaloUserInfo = new ZaloIdentityClient.ZaloUserInfo();
        zaloUserInfo.setId(ZALO_ID);
        zaloUserInfo.setName(ZALO_NAME);

        when(zaloIdentityClient.getUserInfo(ZALO_TOKEN)).thenReturn(zaloUserInfo);
        when(userRepository.findByZaloId(ZALO_ID)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(
                        invocation -> {
                            final var user = invocation.getArgument(0, User.class);
                            if (user.getId() == null) {
                                user.setId("generated-id");
                            }
                            return user;
                        });
        when(jwtService.generateToken(anyString(), any())).thenReturn("jwt-token");

        // When
        final var response = authenticationService.outboundAuthenticate(request);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertTrue(response.getRoles().contains(Role.USER));
        verify(zaloIdentityClient).getUserInfo(ZALO_TOKEN);
        verify(userRepository).findByZaloId(ZALO_ID);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(anyString(), any());
    }

    @Test
    void outboundAuthenticate_WithExistingZaloUser_ShouldReturnToken() {
        // Given
        final var request = new ExchangeTokenRequest();
        request.setToken(ZALO_TOKEN);

        final var zaloUserInfo = new ZaloIdentityClient.ZaloUserInfo();
        zaloUserInfo.setId(ZALO_ID);
        zaloUserInfo.setName(ZALO_NAME);

        final var existingUser =
                User.builder()
                        .id(USER_ID)
                        .zaloId(ZALO_ID)
                        .fullName(ZALO_NAME)
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.USER)))
                        .active(true)
                        .build();

        when(zaloIdentityClient.getUserInfo(ZALO_TOKEN)).thenReturn(zaloUserInfo);
        when(userRepository.findByZaloId(ZALO_ID)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(eq(USER_ID), any())).thenReturn("jwt-token");

        // When
        final var response = authenticationService.outboundAuthenticate(request);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertTrue(response.getRoles().contains(Role.USER));
        verify(zaloIdentityClient).getUserInfo(ZALO_TOKEN);
        verify(userRepository).findByZaloId(ZALO_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService).generateToken(eq(USER_ID), any());
    }

    @Test
    void outboundAuthenticate_WithInactiveUser_ShouldThrowException() {
        // Given
        final var request = new ExchangeTokenRequest();
        request.setToken(ZALO_TOKEN);

        final var zaloUserInfo = new ZaloIdentityClient.ZaloUserInfo();
        zaloUserInfo.setId(ZALO_ID);
        zaloUserInfo.setName(ZALO_NAME);

        final var inactiveZaloUser =
                User.builder()
                        .id(USER_ID)
                        .zaloId(ZALO_ID)
                        .fullName(ZALO_NAME)
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.USER)))
                        .active(false)
                        .build();

        when(zaloIdentityClient.getUserInfo(ZALO_TOKEN)).thenReturn(zaloUserInfo);
        when(userRepository.findByZaloId(ZALO_ID)).thenReturn(Optional.of(inactiveZaloUser));

        // When & Then
        final var exception =
                assertThrows(
                        AppException.class,
                        () -> authenticationService.outboundAuthenticate(request));
        assertEquals(ErrorCode.USER_INACTIVE, exception.getErrorCode());
        verify(zaloIdentityClient).getUserInfo(ZALO_TOKEN);
        verify(userRepository).findByZaloId(ZALO_ID);
    }

    @Test
    void authenticate_WithNullPassword_ShouldThrowException() {
        // Given
        final var request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        activeUser.setPassword(null);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(activeUser));

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> authenticationService.authenticate(request));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
