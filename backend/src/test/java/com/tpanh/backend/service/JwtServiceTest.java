package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tpanh.backend.enums.Role;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String TEST_SECRET_KEY = "test-secret-key-minimum-32-characters-long";
    private static final String USER_ID = "test-user-id-123";
    private static final Set<Role> ROLES = new HashSet<>(Set.of(Role.ADMIN));

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws JOSEException {
        jwtService = new JwtService(TEST_SECRET_KEY);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // When
        final var token = jwtService.generateToken(USER_ID, ROLES);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void verifyToken_WithValidToken_ShouldReturnTrue() {
        // Given
        final var token = jwtService.generateToken(USER_ID, ROLES);

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
        final var token = jwtService.generateToken(USER_ID, ROLES);

        // When
        final var extractedUserId = jwtService.extractUserId(token);

        // Then
        assertEquals(USER_ID, extractedUserId);
    }

    @Test
    void extractRoles_WithValidToken_ShouldReturnRoles() {
        // Given
        final var token = jwtService.generateToken(USER_ID, ROLES);

        // When
        final List<String> extractedRoles = jwtService.extractRoles(token);

        // Then
        assertEquals(1, extractedRoles.size());
        assertTrue(extractedRoles.contains("ROLE_ADMIN"));
    }

    @Test
    void extractRoles_WithMultipleRoles_ShouldReturnAllRoles() {
        // Given
        final Set<Role> multipleRoles = new HashSet<>(Set.of(Role.ADMIN, Role.MANAGER));
        final var token = jwtService.generateToken(USER_ID, multipleRoles);

        // When
        final List<String> extractedRoles = jwtService.extractRoles(token);

        // Then
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("ROLE_ADMIN"));
        assertTrue(extractedRoles.contains("ROLE_MANAGER"));
    }

    @Test
    void extractUserId_WithInvalidToken_ShouldThrowException() {
        // Given
        final var invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractUserId(invalidToken));
    }

    @Test
    void extractRoles_WithInvalidToken_ShouldThrowException() {
        // Given
        final var invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractRoles(invalidToken));
    }

    @Test
    void verifyToken_WithExpiredToken_ShouldReturnFalse() throws JOSEException {
        // Given - Create expired token
        final var now = Instant.now();
        final var expiredTime = now.minusSeconds(86400L); // 1 day ago

        final var claimsSet =
                new JWTClaimsSet.Builder()
                        .issuer("com.tpanh.server")
                        .subject(USER_ID)
                        .claim("roles", List.of("ROLE_ADMIN"))
                        .issueTime(Date.from(expiredTime))
                        .expirationTime(Date.from(expiredTime.plusSeconds(86400L)))
                        .build();

        final var signedJWT =
                new SignedJWT(
                        new com.nimbusds.jose.JWSHeader(com.nimbusds.jose.JWSAlgorithm.HS256),
                        claimsSet);
        signedJWT.sign(new com.nimbusds.jose.crypto.MACSigner(TEST_SECRET_KEY));
        final var expiredToken = signedJWT.serialize();

        // When
        final var isValid = jwtService.verifyToken(expiredToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void verifyToken_WithTokenFromDifferentSecret_ShouldReturnFalse() throws JOSEException {
        // Given - Create token with different secret
        final var differentSecret = "different-secret-key-minimum-32-characters-long";
        final var differentJwtService = new JwtService(differentSecret);
        final var tokenFromDifferentSecret = differentJwtService.generateToken(USER_ID, ROLES);

        // When - Verify with original secret
        final var isValid = jwtService.verifyToken(tokenFromDifferentSecret);

        // Then
        assertFalse(isValid);
    }

    @Test
    void verifyToken_WithNullToken_ShouldReturnFalse() {
        // When
        final var isValid = jwtService.verifyToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void verifyToken_WithEmptyToken_ShouldReturnFalse() {
        // When
        final var isValid = jwtService.verifyToken("");

        // Then
        assertFalse(isValid);
    }

    @Test
    void extractUserId_WithNullToken_ShouldThrowException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractUserId(null));
    }

    @Test
    void extractRoles_WithNullToken_ShouldThrowException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractRoles(null));
    }

    @Test
    void extractUserId_WithEmptyToken_ShouldThrowException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractUserId(""));
    }

    @Test
    void extractRoles_WithEmptyToken_ShouldThrowException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractRoles(""));
    }

    @Test
    void verifyToken_WithTokenMissingExpiration_ShouldReturnTrue() throws JOSEException {
        // Given - Create token without expiration (isExpired returns false when expiration is null)
        final var now = Instant.now();
        final var claimsSet =
                new JWTClaimsSet.Builder()
                        .issuer("com.tpanh.server")
                        .subject(USER_ID)
                        .claim("roles", List.of("ROLE_ADMIN"))
                        .issueTime(Date.from(now))
                        .build(); // No expiration time

        final var signedJWT =
                new SignedJWT(
                        new com.nimbusds.jose.JWSHeader(com.nimbusds.jose.JWSAlgorithm.HS256),
                        claimsSet);
        signedJWT.sign(new com.nimbusds.jose.crypto.MACSigner(TEST_SECRET_KEY));
        final var tokenWithoutExpiration = signedJWT.serialize();

        // When
        final var isValid = jwtService.verifyToken(tokenWithoutExpiration);

        // Then - Token without expiration is considered valid (not expired)
        assertTrue(isValid);
    }
}
