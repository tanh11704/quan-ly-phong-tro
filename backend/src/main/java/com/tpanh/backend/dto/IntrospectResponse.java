package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả kiểm tra tính hợp lệ của token")
public class IntrospectResponse {
    @Schema(description = "Token có hợp lệ hay không", example = "true")
    private Boolean valid;
}
