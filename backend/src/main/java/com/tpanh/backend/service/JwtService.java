package com.tpanh.backend.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tpanh.backend.dto.JwtPayload;
import com.tpanh.backend.enums.Role;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtService {
    private static final String ISSUER = "com.tpanh.server";
    private static final long EXPIRATION_TIME_SECONDS = 86400L; // 1 ngày

    private final JWSSigner signer;
    private final JWSVerifier verifier;
    private final String secretKey;

    public JwtService(
            @Value("${app.jwt.secret:your-256-bit-secret-key-change-in-production}")
                    final String secretKey)
            throws JOSEException {
        this.secretKey = secretKey;
        this.signer = new MACSigner(secretKey);
        this.verifier = new MACVerifier(secretKey);
    }

    public String generateToken(final String userId, final Set<Role> roles) {
        try {
            final var now = Instant.now();
            final var expiration = now.plusSeconds(EXPIRATION_TIME_SECONDS);

            final List<String> roleStrings = roles.stream().map(r -> "ROLE_" + r.name()).toList();

            final var claimsSet =
                    new JWTClaimsSet.Builder()
                            .issuer(ISSUER)
                            .subject(userId)
                            .claim("roles", roleStrings)
                            .issueTime(Date.from(now))
                            .expirationTime(Date.from(expiration))
                            .build();

            final var signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (final JOSEException e) {
            log.error("Error generating JWT token", e);
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public boolean verifyToken(final String token) {
        try {
            final var signedJWT = SignedJWT.parse(token);
            return signedJWT.verify(verifier) && !isExpired(signedJWT.getJWTClaimsSet());
        } catch (final Exception e) {
            log.debug("Token verification failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserId(final String token) {
        try {
            final var signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (final Exception e) {
            log.error("Error extracting user ID from token", e);
            throw new RuntimeException("Failed to extract user ID", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(final String token) {
        try {
            final var signedJWT = SignedJWT.parse(token);
            final var rawRoles = signedJWT.getJWTClaimsSet().getClaim("roles");

            if (!(rawRoles instanceof List<?>)) {
                log.warn("Invalid roles claim in token");
                return List.of();
            }

            return (List<String>) rawRoles;
        } catch (final Exception e) {
            log.error("Error extracting roles from token", e);
            throw new RuntimeException("Failed to extract roles", e);
        }
    }

    private boolean isExpired(final JWTClaimsSet claimsSet) {
        final var expirationTime = claimsSet.getExpirationTime();
        return expirationTime != null && expirationTime.before(new Date());
    }

    public Optional<JwtPayload> parseAndValidate(final String token) {
        try {
            final var signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(verifier)) {
                log.debug("Token signature verification failed");
                return Optional.empty();
            }
            final var claimsSet = signedJWT.getJWTClaimsSet();
            if (isExpired(claimsSet)) {
                log.debug("Token has expired");
                return Optional.empty();
            }
            return Optional.of(extractPayload(claimsSet));
        } catch (final Exception e) {
            log.debug("Token parsing failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private JwtPayload extractPayload(final JWTClaimsSet claimsSet) {
        final var userId = claimsSet.getSubject();
        final var rawRoles = claimsSet.getClaim("roles");
        final List<String> roles =
                (rawRoles instanceof List<?>) ? (List<String>) rawRoles : List.of();
        final var issuedAt =
                claimsSet.getIssueTime() != null ? claimsSet.getIssueTime().toInstant() : null;
        final var expiresAt =
                claimsSet.getExpirationTime() != null
                        ? claimsSet.getExpirationTime().toInstant()
                        : null;
        return new JwtPayload(userId, roles, issuedAt, expiresAt);
    }
}
