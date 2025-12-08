package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String TEST_SECRET_KEY = "test-secret-key-minimum-32-characters-long";
    private static final String USER_ID = "test-user-id-123";
    private static final String ROLE = "ROLE_ADMIN";

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws JOSEException {
        jwtService = new JwtService(TEST_SECRET_KEY);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // When
        final var token = jwtService.generateToken(USER_ID, ROLE);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void verifyToken_WithValidToken_ShouldReturnTrue() {
        // Given
        final var token = jwtService.generateToken(USER_ID, ROLE);

        // When
        final var isValid = jwtService.verifyToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void verifyToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        final var invalidToken = "invalid.token.here";

        // When
        final var isValid = jwtService.verifyToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void extractUserId_WithValidToken_ShouldReturnUserId() {
        // Given
        final var token = jwtService.generateToken(USER_ID, ROLE);

        // When
        final var extractedUserId = jwtService.extractUserId(token);

        // Then
        assertEquals(USER_ID, extractedUserId);
    }

    @Test
    void extractRole_WithValidToken_ShouldReturnRole() {
        // Given
        final var token = jwtService.generateToken(USER_ID, ROLE);

        // When
        final var extractedRole = jwtService.extractRole(token);

        // Then
        assertEquals(ROLE, extractedRole);
    }

    @Test
    void extractUserId_WithInvalidToken_ShouldThrowException() {
        // Given
        final var invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractUserId(invalidToken));
    }

    @Test
    void extractRole_WithInvalidToken_ShouldThrowException() {
        // Given
        final var invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractRole(invalidToken));
    }
}
