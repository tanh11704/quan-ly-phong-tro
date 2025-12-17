package com.tpanh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UtilityReadingCreationRequest {
    @NotNull(message = "ROOM_ID_REQUIRED")
    private Integer roomId;

    @NotBlank(message = "INVALID_PERIOD")
    private String month; // Format: 'YYYY-MM'

    private Integer electricIndex;

    private Integer waterIndex;

    private String imageEvidence; // URL or path to image
}
