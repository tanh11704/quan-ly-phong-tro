package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu thêm khách thuê mới")
public class TenantCreationRequest {
    @NotNull(message = "ROOM_ID_REQUIRED")
    @Schema(description = "ID phòng", example = "1")
    private Integer roomId;

    @NotBlank(message = "TENANT_NAME_REQUIRED")
    @Schema(description = "Tên khách thuê", example = "Nguyễn Văn A")
    private String name;

    @Schema(description = "Số điện thoại", example = "0901234567")
    private String phone;

    @Schema(description = "Email khách thuê", example = "tenant@example.com")
    private String email;

    @Schema(description = "Có phải người đại diện hợp đồng không", example = "true")
    private Boolean isContractHolder;

    @Schema(description = "Ngày bắt đầu hợp đồng", example = "2025-01-01")
    private LocalDate startDate;

    @Schema(
            description = "Ngày hết hạn hợp đồng dự kiến (để null nếu vô thời hạn)",
            example = "2025-12-31")
    private LocalDate contractEndDate;
}
