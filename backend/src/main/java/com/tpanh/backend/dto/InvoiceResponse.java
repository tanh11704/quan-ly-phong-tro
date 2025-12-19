package com.tpanh.backend.dto;

import java.time.LocalDate;

import com.tpanh.backend.enums.InvoiceStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Thông tin hóa đơn")
public class InvoiceResponse {
    @Schema(description = "ID hóa đơn", example = "1")
    private Integer id;

    @Schema(description = "Số phòng", example = "P.101")
    private String roomNo;

    @Schema(description = "Tên khách thuê", example = "Nguyễn Văn A")
    private String tenantName;

    @Schema(description = "Kỳ thanh toán (định dạng YYYY-MM)", example = "2025-01")
    private String period;

    @Schema(description = "Tiền phòng (VNĐ)", example = "3000000")
    private Integer roomPrice;

    @Schema(description = "Tiền điện (VNĐ)", example = "500000")
    private Integer elecAmount;

    @Schema(description = "Tiền nước (VNĐ)", example = "200000")
    private Integer waterAmount;

    @Schema(description = "Tổng tiền (VNĐ)", example = "3700000")
    private Integer totalAmount;

    @Schema(
            description = "Trạng thái hóa đơn",
            example = "UNPAID",
            allowableValues = {"DRAFT", "UNPAID", "PAID"})
    private InvoiceStatus status;

    @Schema(description = "Hạn thanh toán", example = "2025-01-31")
    private LocalDate dueDate;
}
