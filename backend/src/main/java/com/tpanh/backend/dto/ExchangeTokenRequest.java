package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu trao đổi token Zalo lấy token hệ thống")
public class ExchangeTokenRequest {
    @NotBlank(message = "ZALO_TOKEN_REQUIRED")
    @Schema(description = "Access token từ Zalo SDK ở client", example = "zalo_access_token_here")
    private String token;
}
