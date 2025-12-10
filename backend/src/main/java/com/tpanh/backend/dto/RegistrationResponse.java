package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response sau khi đăng ký thành công")
public class RegistrationResponse {
    @Schema(
            description = "ID của user vừa đăng ký",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String userId;

    @Schema(
            description = "Thông báo",
            example = "Đăng ký thành công. Vui lòng kiểm tra email để kích hoạt tài khoản.")
    private String message;
}
