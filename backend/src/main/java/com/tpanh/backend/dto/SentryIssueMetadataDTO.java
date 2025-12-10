package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sentry Issue metadata")
public class SentryIssueMetadataDTO {
    @Schema(description = "Error type", example = "Error")
    private String type;

    @Schema(description = "Error message", example = "Cannot read property 'x' of undefined")
    private String value;

    @Schema(description = "Filename where error occurred", example = "app.tsx")
    private String filename;

    @Schema(description = "Function name where error occurred", example = "handleClick")
    private String function;
}
