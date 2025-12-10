package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.SentryEventDTO;
import com.tpanh.backend.dto.SentryIssueDTO;
import com.tpanh.backend.dto.SentryIssueListResponseDTO;
import com.tpanh.backend.service.SentryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api-prefix}/sentry")
@RequiredArgsConstructor
@Tag(name = "Sentry", description = "API quản lý Sentry logs (chỉ dành cho Admin)")
public class SentryController {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_EVENT_LIMIT = 10;

    private final SentryService sentryService;

    @Operation(
            summary = "Lấy danh sách Sentry issues",
            description =
                    "Lấy danh sách các issues từ Sentry với hỗ trợ phân trang, filter theo status/level và tìm kiếm")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy danh sách issues thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Chỉ Admin mới có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Lỗi khi gọi Sentry API")
            })
    @GetMapping("/issues")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SentryIssueListResponseDTO> getIssues(
            @Parameter(description = "Số trang", example = "1")
                    @RequestParam(required = false, defaultValue = "1")
                    final Integer page,
            @Parameter(description = "Số items mỗi trang", example = "20")
                    @RequestParam(required = false, defaultValue = "20")
                    final Integer pageSize,
            @Parameter(
                            description = "Trạng thái issue",
                            schema =
                                    @Schema(
                                            example = "unresolved",
                                            allowableValues = {
                                                "unresolved",
                                                "resolved",
                                                "ignored",
                                                "muted"
                                            }))
                    @RequestParam(required = false)
                    final String status,
            @Parameter(
                            description = "Mức độ issue",
                            schema =
                                    @Schema(
                                            example = "error",
                                            allowableValues = {
                                                "error", "warning", "info", "debug", "fatal"
                                            }))
                    @RequestParam(required = false)
                    final String level,
            @Parameter(description = "Từ khóa tìm kiếm", example = "Error message")
                    @RequestParam(required = false)
                    final String query) {
        final var result =
                sentryService.getIssues(
                        page != null ? page : DEFAULT_PAGE,
                        pageSize != null ? pageSize : DEFAULT_PAGE_SIZE,
                        status,
                        level,
                        query);
        return ApiResponse.<SentryIssueListResponseDTO>builder()
                .result(result)
                .message("Lấy danh sách Sentry issues thành công")
                .build();
    }

    @Operation(
            summary = "Lấy chi tiết một Sentry issue",
            description = "Lấy thông tin chi tiết của một issue cụ thể từ Sentry")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy chi tiết issue thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Chỉ Admin mới có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy issue"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Lỗi khi gọi Sentry API")
            })
    @GetMapping("/issues/{issueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SentryIssueDTO> getIssueById(
            @Parameter(description = "ID của issue", example = "1234567890") @PathVariable
                    final String issueId) {
        final var result = sentryService.getIssueById(issueId);
        return ApiResponse.<SentryIssueDTO>builder()
                .result(result)
                .message("Lấy chi tiết Sentry issue thành công")
                .build();
    }

    @Operation(
            summary = "Lấy danh sách events của một issue",
            description = "Lấy danh sách các events gần đây của một issue cụ thể")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy danh sách events thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Chỉ Admin mới có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy issue"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Lỗi khi gọi Sentry API")
            })
    @GetMapping("/issues/{issueId}/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<SentryEventDTO>> getIssueEvents(
            @Parameter(description = "ID của issue", example = "1234567890") @PathVariable
                    final String issueId,
            @Parameter(description = "Số lượng events cần lấy", example = "10")
                    @RequestParam(required = false)
                    final Integer limit) {
        final var result =
                sentryService.getIssueEvents(issueId, limit != null ? limit : DEFAULT_EVENT_LIMIT);
        return ApiResponse.<List<SentryEventDTO>>builder()
                .result(result)
                .message("Lấy danh sách Sentry events thành công")
                .build();
    }
}
