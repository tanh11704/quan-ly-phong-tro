package com.tpanh.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sentry Issue information")
public class SentryIssueDTO {
    @Schema(description = "Issue ID", example = "1234567890")
    private String id;

    @Schema(description = "Short ID", example = "ABC123")
    @JsonProperty("shortId")
    private String shortId;

    @Schema(description = "Issue title", example = "Error: Cannot read property 'x' of undefined")
    private String title;

    @Schema(
            description = "Culprit (location where error occurred)",
            example = "app.tsx in handleClick")
    private String culprit;

    @Schema(
            description = "Issue level",
            example = "error",
            allowableValues = {"error", "warning", "info", "debug", "fatal"})
    private String level;

    @Schema(
            description = "Issue status",
            example = "unresolved",
            allowableValues = {"unresolved", "resolved", "ignored", "muted"})
    private String status;

    @Schema(description = "Number of occurrences", example = "100")
    private Integer count;

    @Schema(description = "Number of affected users", example = "50")
    @JsonProperty("userCount")
    private Integer userCount;

    @Schema(description = "First seen timestamp", example = "2024-01-01T00:00:00Z")
    @JsonProperty("firstSeen")
    private Instant firstSeen;

    @Schema(description = "Last seen timestamp", example = "2024-01-02T00:00:00Z")
    @JsonProperty("lastSeen")
    private Instant lastSeen;

    @Schema(
            description = "Permalink to issue in Sentry",
            example = "https://sentry.io/organizations/org/issues/1234567890/")
    private String permalink;

    @Schema(description = "Issue metadata")
    private SentryIssueMetadataDTO metadata;
}
