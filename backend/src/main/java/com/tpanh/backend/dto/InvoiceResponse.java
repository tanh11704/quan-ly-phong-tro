package com.tpanh.backend.dto;

import com.tpanh.backend.enums.InvoiceStatus;
import java.time.LocalDate;
import lombok.Data;

@Data
public class InvoiceResponse {
    private Integer id;

    private String roomNo;
    private String tenantName;

    private String period;

    private Integer roomPrice;
    private Integer elecAmount;
    private Integer waterAmount;
    private Integer totalAmount;

    private InvoiceStatus status;
    private LocalDate dueDate;
}
