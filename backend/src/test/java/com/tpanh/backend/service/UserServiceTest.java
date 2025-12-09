package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private static final String USER_ID = "user-id-123";
    private static final String USERNAME = "testuser";
    private static final String FULL_NAME = "Test User";

    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser =
                User.builder()
                        .id(USER_ID)
                        .username(USERNAME)
                        .fullName(FULL_NAME)
                        .roles(Role.ADMIN)
                        .active(true)
                        .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WithValidAuthentication_ShouldReturnUserDTO() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(USER_ID);
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        // When
        final var result = userService.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals(USER_ID, result.getId());
        assertEquals(USERNAME, result.getUsername());
        assertEquals(FULL_NAME, result.getFullName());
        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void getCurrentUser_WithNullAuthentication_ShouldThrowException() {
        // Given
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(null);
        SecurityContextHolder.setContext(securityContext);

        // When & Then
        final var exception = assertThrows(AppException.class, () -> userService.getCurrentUser());
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    void getCurrentUser_WithNotAuthenticated_ShouldThrowException() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // When & Then
        final var exception = assertThrows(AppException.class, () -> userService.getCurrentUser());
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    void getCurrentUser_WithUserNotFound_ShouldThrowException() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(USER_ID);
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception = assertThrows(AppException.class, () -> userService.getCurrentUser());
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void getCurrentUser_WithTenantRole_ShouldReturnUserDTO() {
        // Given
        final var tenantUser =
                User.builder()
                        .id(USER_ID)
                        .fullName("Tenant User")
                        .roles(Role.TENANT)
                        .active(true)
                        .build();

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(USER_ID);
        final var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(tenantUser));

        // When
        final var result = userService.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals(USER_ID, result.getId());
        assertEquals("Tenant User", result.getFullName());
        assertEquals(Role.TENANT, result.getRole());
        assertEquals(null, result.getUsername()); // Tenant có thể không có username
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        final var user1 =
                User.builder()
                        .id("user-1")
                        .username("user1")
                        .fullName("User 1")
                        .roles(Role.ADMIN)
                        .active(true)
                        .build();
        final var user2 =
                User.builder()
                        .id("user-2")
                        .username("user2")
                        .fullName("User 2")
                        .roles(Role.MANAGER)
                        .active(true)
                        .build();
        final var user3 =
                User.builder()
                        .id("user-3")
                        .fullName("User 3")
                        .roles(Role.TENANT)
                        .active(false)
                        .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        // When
        final var result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("user-1", result.get(0).getId());
        assertEquals("user-2", result.get(1).getId());
        assertEquals("user-3", result.get(2).getId());
        assertEquals(Role.ADMIN, result.get(0).getRole());
        assertEquals(Role.MANAGER, result.get(1).getRole());
        assertEquals(Role.TENANT, result.get(2).getRole());
        assertTrue(result.get(0).getActive());
        assertTrue(result.get(1).getActive());
        assertFalse(result.get(2).getActive());
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        final var result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void toggleUserActive_WithActiveUser_ShouldDeactivate() {
        // Given
        final var activeUser =
                User.builder()
                        .id(USER_ID)
                        .username(USERNAME)
                        .fullName(FULL_NAME)
                        .roles(Role.MANAGER)
                        .active(true)
                        .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(activeUser)).thenReturn(activeUser);

        // When
        final var result = userService.toggleUserActive(USER_ID);

        // Then
        assertNotNull(result);
        assertFalse(result.getActive());
        assertEquals(USER_ID, result.getId());
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(activeUser);
    }

    @Test
    void toggleUserActive_WithInactiveUser_ShouldActivate() {
        // Given
        final var inactiveUser =
                User.builder()
                        .id(USER_ID)
                        .username(USERNAME)
                        .fullName(FULL_NAME)
                        .roles(Role.MANAGER)
                        .active(false)
                        .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(inactiveUser)).thenReturn(inactiveUser);

        // When
        final var result = userService.toggleUserActive(USER_ID);

        // Then
        assertNotNull(result);
        assertTrue(result.getActive());
        assertEquals(USER_ID, result.getId());
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(inactiveUser);
    }

    @Test
    void toggleUserActive_WithUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        final var exception =
                assertThrows(AppException.class, () -> userService.toggleUserActive(USER_ID));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }
}
