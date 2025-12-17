package com.tpanh.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tpanh.backend.enums.InvoiceStatus;

import lombok.Data;

@Data
public class InvoiceDetailResponse {
    private Integer id;

    private String roomNo;
    private Integer roomId;
    private String buildingName;
    private Integer buildingId;

    private String tenantName;
    private String tenantPhone;
    private Integer tenantId;

    private String period;

    // Chi tiết chỉ số điện nước
    private Integer elecPreviousValue;
    private Integer elecCurrentValue;
    private Integer elecUsage;
    private Integer elecUnitPrice;
    private Integer elecAmount;

    private Integer waterPreviousValue;
    private Integer waterCurrentValue;
    private Integer waterUsage;
    private Integer waterUnitPrice;
    private Integer waterAmount;

    // Thành phần tiền
    private Integer roomPrice;
    private Integer totalAmount;

    // Trạng thái và thời gian
    private InvoiceStatus status;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
