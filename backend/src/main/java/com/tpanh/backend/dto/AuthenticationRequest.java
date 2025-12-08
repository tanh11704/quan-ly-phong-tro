package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đăng nhập bằng tên đăng nhập và mật khẩu")
public class AuthenticationRequest {
    @NotBlank(message = "USERNAME_REQUIRED")
    @Schema(description = "Tên đăng nhập cho admin", example = "admin")
    private String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Schema(description = "Mật khẩu người dùng", example = "password123")
    private String password;
}
