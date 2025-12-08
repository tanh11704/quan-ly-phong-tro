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
import java.time.Instant;
import java.util.Date;
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

    public String generateToken(final String userId, final String role) {
        try {
            final var now = Instant.now();
            final var expiration = now.plusSeconds(EXPIRATION_TIME_SECONDS);

            final var claimsSet =
                    new JWTClaimsSet.Builder()
                            .issuer(ISSUER)
                            .subject(userId)
                            .claim("scope", role)
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

    public String extractRole(final String token) {
        try {
            final var signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getStringClaim("scope");
        } catch (final Exception e) {
            log.error("Error extracting role from token", e);
            throw new RuntimeException("Failed to extract role", e);
        }
    }

    private boolean isExpired(final JWTClaimsSet claimsSet) {
        final var expirationTime = claimsSet.getExpirationTime();
        return expirationTime != null && expirationTime.before(new Date());
    }
}
