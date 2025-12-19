package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu cập nhật chỉ số điện nước")
public class UtilityReadingUpdateRequest {
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
    private String imageEvidence;
}
