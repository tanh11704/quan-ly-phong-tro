package com.tpanh.backend.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Thông tin chỉ số điện nước")
public class UtilityReadingResponse {
    @Schema(description = "ID bản ghi chỉ số điện nước", example = "1")
    private Integer id;

    @Schema(description = "ID phòng", example = "10")
    private Integer roomId;

    @Schema(description = "Số phòng", example = "P.101")
    private String roomNo;

    @Schema(description = "Tháng ghi chỉ số (định dạng YYYY-MM)", example = "2025-01")
    private String month;

    @Schema(description = "Chỉ số điện (kWh)", example = "1234")
    private Integer electricIndex;

    @Schema(description = "Chỉ số nước (m³)", example = "56")
    private Integer waterIndex;

    @Schema(description = "URL ảnh chứng cứ đồng hồ", example = "http://example.com/image.jpg")
    private String imageEvidence;

    @Schema(description = "Thời gian tạo bản ghi", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;
}
