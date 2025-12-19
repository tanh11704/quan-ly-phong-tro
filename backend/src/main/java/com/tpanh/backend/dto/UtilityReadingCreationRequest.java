package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu ghi chỉ số điện nước")
public class UtilityReadingCreationRequest {
    @Schema(description = "ID phòng", example = "10")
    @NotNull(message = "ROOM_ID_REQUIRED")
    private Integer roomId;

    @Schema(description = "Tháng ghi chỉ số (định dạng YYYY-MM)", example = "2025-01")
    @NotBlank(message = "INVALID_PERIOD")
    private String month; // Format: 'YYYY-MM'

    @Schema(description = "Chỉ số điện (kWh)", example = "1234")
    private Integer electricIndex;

    @Schema(description = "Chỉ số nước (m³)", example = "56")
    private Integer waterIndex;

    @Schema(
            description =
                    "Tick true nếu đồng hồ điện/nước đã thay mới hoặc quay vòng (ví dụ 99999 -> 00000). "
                            + "Khi true, hệ thống sẽ cho phép chỉ số mới nhỏ hơn chỉ số cũ.",
            example = "false")
    private Boolean isMeterReset;

    @Schema(description = "URL ảnh chứng cứ đồng hồ", example = "http://example.com/image.jpg")
    private String imageEvidence; // URL or path to image
}
