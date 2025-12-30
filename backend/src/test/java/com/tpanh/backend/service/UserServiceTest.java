package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpanh.backend.dto.UserDTO;
import com.tpanh.backend.entity.User;
import com.tpanh.backend.enums.Role;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.UserMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private static final String USER_ID = "user-id-123";
    private static final String USERNAME = "testuser";
    private static final String FULL_NAME = "Test User";

    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;
    @Mock private UserMapper userMapper;

    @InjectMocks private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser =
                User.builder()
                        .id(USER_ID)
                        .username(USERNAME)
                        .fullName(FULL_NAME)
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.ADMIN)))
                        .active(true)
                        .build();

        // Mock mapper to return response based on user (lenient for tests that don't use mapper)
        lenient()
                .when(userMapper.toDTO(any(User.class)))
                .thenAnswer(
                        invocation -> {
                            final User u = invocation.getArgument(0);
                            return UserDTO.builder()
                                    .id(u.getId())
                                    .username(u.getUsername())
                                    .fullName(u.getFullName())
                                    .roles(u.getRoles())
                                    .active(u.getActive())
                                    .build();
                        });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WithValidAuthentication_ShouldReturnUserDTO() {

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
        assertTrue(result.getRoles().contains(Role.ADMIN));
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void getCurrentUser_WithUserNotFound_ShouldThrowException() {
        // Given

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
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.TENANT)))
                        .active(true)
                        .build();

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
        assertTrue(result.getRoles().contains(Role.TENANT));
        assertEquals(null, result.getUsername()); // Tenant có thể không có username
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        final var user1 =
                User.builder()
                        .id("user-1")
                        .username("user1")
                        .fullName("User 1")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.ADMIN)))
                        .active(true)
                        .build();
        final var user2 =
                User.builder()
                        .id("user-2")
                        .username("user2")
                        .fullName("User 2")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.MANAGER)))
                        .active(true)
                        .build();
        final var user3 =
                User.builder()
                        .id("user-3")
                        .fullName("User 3")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.TENANT)))
                        .active(false)
                        .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        final var result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("user-1", result.get(0).getId());
        assertEquals("user-2", result.get(1).getId());
        assertEquals("user-3", result.get(2).getId());
        assertTrue(result.get(0).getRoles().contains(Role.ADMIN));
        assertTrue(result.get(1).getRoles().contains(Role.MANAGER));
        assertTrue(result.get(2).getRoles().contains(Role.TENANT));
        assertTrue(result.get(0).getActive());
        assertTrue(result.get(1).getActive());
        assertFalse(result.get(2).getActive());
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_WithEmptyList_ShouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        final var result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void toggleUserActive_WithActiveUser_ShouldDeactivate() {
        final var activeUser =
                User.builder()
                        .id(USER_ID)
                        .username(USERNAME)
                        .fullName(FULL_NAME)
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.MANAGER)))
                        .active(true)
                        .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(activeUser)).thenReturn(activeUser);

        final var result = userService.toggleUserActive(USER_ID);

        assertNotNull(result);
        assertFalse(result.getActive());
        assertEquals(USER_ID, result.getId());
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(activeUser);
    }

    @Test
    void toggleUserActive_WithInactiveUser_ShouldActivate() {
        final var inactiveUser =
                User.builder()
                        .id(USER_ID)
                        .username(USERNAME)
                        .fullName(FULL_NAME)
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.MANAGER)))
                        .active(false)
                        .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(inactiveUser)).thenReturn(inactiveUser);

        final var result = userService.toggleUserActive(USER_ID);

        assertNotNull(result);
        assertTrue(result.getActive());
        assertEquals(USER_ID, result.getId());
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(inactiveUser);
    }

    @Test
    void toggleUserActive_WithUserNotFound_ShouldThrowException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        final var exception =
                assertThrows(AppException.class, () -> userService.toggleUserActive(USER_ID));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_WithPageable_ShouldReturnPageResponse() {
        final var user1 =
                User.builder()
                        .id("user-1")
                        .username("user1")
                        .fullName("User 1")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.ADMIN)))
                        .active(true)
                        .build();
        final var user2 =
                User.builder()
                        .id("user-2")
                        .username("user2")
                        .fullName("User 2")
                        .roles(new java.util.HashSet<>(java.util.Set.of(Role.MANAGER)))
                        .active(true)
                        .build();

        final Pageable pageable = PageRequest.of(0, 10);
        final Page<User> page = new PageImpl<>(Arrays.asList(user1, user2), pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(page);

        final var response = userService.getAllUsers(pageable);

        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(2, response.getContent().size());
        assertNotNull(response.getPage());
        assertEquals(0, response.getPage().getPage());
        assertEquals(10, response.getPage().getSize());
        assertEquals(2, response.getPage().getTotalElements());
        assertEquals(1, response.getPage().getTotalPages());
        assertTrue(response.getPage().isFirst());
        assertTrue(response.getPage().isLast());
        verify(userRepository).findAll(pageable);
    }
}
