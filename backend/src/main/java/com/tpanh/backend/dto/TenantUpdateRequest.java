package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu cập nhật thông tin khách thuê")
public class TenantUpdateRequest {
    @Schema(description = "Tên khách thuê", example = "Nguyễn Văn A")
    private String name;

    @Schema(description = "Số điện thoại", example = "0901234567")
    private String phone;

    @Schema(description = "Email khách thuê", example = "tenant@example.com")
    @Email(message = "EMAIL_INVALID")
    private String email;

    @Schema(description = "Có phải người đại diện hợp đồng không", example = "true")
    private Boolean isContractHolder;
}
