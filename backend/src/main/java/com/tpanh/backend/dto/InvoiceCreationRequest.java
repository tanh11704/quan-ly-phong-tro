package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo hóa đơn cho tòa nhà")
public class InvoiceCreationRequest {
    @Schema(description = "ID tòa nhà", example = "1")
    @NotNull(message = "INVALID_BUILDING_ID")
    private Integer buildingId;

    @Schema(description = "Kỳ thanh toán (định dạng YYYY-MM)", example = "2025-01")
    @NotBlank(message = "INVALID_PERIOD")
    private String period;
}
