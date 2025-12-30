package com.tpanh.backend.dto;

import java.time.Instant;
import java.util.List;

public record JwtPayload(String userId, List<String> roles, Instant issuedAt, Instant expiresAt) {
    public JwtPayload {
        roles = roles != null ? List.copyOf(roles) : List.of();
    }
}
