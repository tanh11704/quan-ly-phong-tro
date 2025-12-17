package com.tpanh.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "INVALID_INVOICE_ID")
    private Integer invoiceId;
}
