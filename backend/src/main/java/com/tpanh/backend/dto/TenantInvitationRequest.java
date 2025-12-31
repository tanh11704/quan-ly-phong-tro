package com.tpanh.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TenantInvitationRequest {
    @NotNull(message = "ROOM_ID_REQUIRED")
    private Integer roomId;

    @NotNull(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    private String email;

    private boolean isContractHolder;

    @Future(message = "CONTRACT_END_DATE_MUST_BE_FUTURE")
    private LocalDate contractEndDate;
}
