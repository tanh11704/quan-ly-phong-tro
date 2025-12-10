package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đăng ký tài khoản Manager")
public class RegistrationRequest {
    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 50;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 100;
    private static final int FULL_NAME_MAX_LENGTH = 100;

    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = USERNAME_MIN_LENGTH, max = USERNAME_MAX_LENGTH, message = "USERNAME_INVALID_LENGTH")
    @Schema(
            description = "Tên đăng nhập",
            example = "manager01",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH, message = "PASSWORD_INVALID_LENGTH")
    @Schema(
            description = "Mật khẩu (tối thiểu 6 ký tự)",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "FULL_NAME_REQUIRED")
    @Size(max = FULL_NAME_MAX_LENGTH, message = "FULL_NAME_INVALID_LENGTH")
    @Schema(
            description = "Họ và tên",
            example = "Nguyễn Văn A",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    @Schema(
            description = "Email để nhận link kích hoạt",
            example = "manager@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
