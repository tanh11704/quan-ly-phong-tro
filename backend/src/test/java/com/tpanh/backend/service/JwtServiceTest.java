package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;
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

    @Test
    void verifyToken_WithExpiredToken_ShouldReturnFalse() throws JOSEException {
        // Given - Create expired token
        final var now = Instant.now();
        final var expiredTime = now.minusSeconds(86400L); // 1 day ago

        final var claimsSet =
                new JWTClaimsSet.Builder()
                        .issuer("com.tpanh.server")
                        .subject(USER_ID)
                        .claim("scope", ROLE)
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
        final var tokenFromDifferentSecret = differentJwtService.generateToken(USER_ID, ROLE);

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
    void extractRole_WithNullToken_ShouldThrowException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractRole(null));
    }

    @Test
    void extractUserId_WithEmptyToken_ShouldThrowException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractUserId(""));
    }

    @Test
    void extractRole_WithEmptyToken_ShouldThrowException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> jwtService.extractRole(""));
    }

    @Test
    void verifyToken_WithTokenMissingExpiration_ShouldReturnTrue() throws JOSEException {
        // Given - Create token without expiration (isExpired returns false when expiration is null)
        final var now = Instant.now();
        final var claimsSet =
                new JWTClaimsSet.Builder()
                        .issuer("com.tpanh.server")
                        .subject(USER_ID)
                        .claim("scope", ROLE)
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
