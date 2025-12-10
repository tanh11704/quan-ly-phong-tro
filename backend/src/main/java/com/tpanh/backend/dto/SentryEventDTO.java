package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sentry Event information")
public class SentryEventDTO {
    @Schema(description = "Event wrapper")
    private SentryEventDataDTO event;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sentry Event data")
class SentryEventDataDTO {
    @Schema(description = "Event ID", example = "event-123")
    private String id;

    @Schema(description = "Issue ID", example = "1234567890")
    private String issue;

    @Schema(description = "Event message", example = "Error occurred in handleClick")
    private String message;

    @Schema(description = "Event level", example = "error")
    private String level;

    @Schema(description = "Platform", example = "javascript")
    private String platform;

    @Schema(description = "Event timestamp", example = "2024-01-01T00:00:00Z")
    private Instant timestamp;

    @Schema(description = "Event tags")
    private Map<String, String> tags;

    @Schema(description = "User information")
    private SentryEventUserDTO user;

    @Schema(description = "Context information (browser, OS, etc.)")
    private Map<String, Object> contexts;

    @Schema(description = "Exception information")
    private SentryExceptionDTO exception;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User information in event")
class SentryEventUserDTO {
    @Schema(description = "User ID", example = "user-123")
    private String id;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "User email", example = "john@example.com")
    private String email;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exception information")
class SentryExceptionDTO {
    @Schema(description = "Exception values")
    private List<SentryExceptionValueDTO> values;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exception value")
class SentryExceptionValueDTO {
    @Schema(description = "Exception type", example = "Error")
    private String type;

    @Schema(description = "Exception message", example = "Cannot read property 'x' of undefined")
    private String value;

    @Schema(description = "Stacktrace information")
    private SentryStacktraceDTO stacktrace;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stacktrace information")
class SentryStacktraceDTO {
    @Schema(description = "Stack frames")
    private List<SentryStackFrameDTO> frames;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stack frame")
class SentryStackFrameDTO {
    @Schema(description = "Filename", example = "app.tsx")
    private String filename;

    @Schema(description = "Function name", example = "handleClick")
    private String function;

    @Schema(description = "Line number", example = "42")
    private Integer lineno;

    @Schema(description = "Column number", example = "10")
    private Integer colno;
}
