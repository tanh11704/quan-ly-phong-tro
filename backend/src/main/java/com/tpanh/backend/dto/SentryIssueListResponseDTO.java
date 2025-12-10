package com.tpanh.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing list of Sentry issues with pagination")
public class SentryIssueListResponseDTO {
    @Schema(description = "List of issues", example = "[]")
    private List<SentryIssueDTO> issues;

    @Schema(description = "Total number of issues", example = "100")
    private Integer total;

    @Schema(description = "Current page number", example = "1")
    private Integer page;

    @Schema(description = "Page size", example = "20")
    @JsonProperty("pageSize")
    private Integer pageSize;
}
