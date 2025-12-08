package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu kiểm tra tính hợp lệ của token")
public class IntrospectRequest {
    @NotBlank(message = "TOKEN_REQUIRED")
    @Schema(
            description = "JWT token cần kiểm tra",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
}
