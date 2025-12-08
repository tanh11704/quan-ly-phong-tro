package com.tpanh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoiceCreationRequest {
    @NotNull(message = "INVALID_BUILDING_ID")
    private Integer buildingId;

    @NotBlank(message = "INVALID_PERIOD")
    private String period;
}
