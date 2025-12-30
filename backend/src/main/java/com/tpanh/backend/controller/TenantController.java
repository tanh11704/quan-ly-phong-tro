package com.tpanh.backend.controller;

import com.tpanh.backend.config.PaginationConfig;
import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.PageResponse;
import com.tpanh.backend.dto.TenantCreationRequest;
import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.dto.TenantUpdateRequest;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api-prefix}/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "API quản lý khách thuê (dành cho Manager)")
public class TenantController {
    private final TenantService tenantService;
    private final BuildingRepository buildingRepository;

    @Operation(
            summary = "Thêm khách thuê",
            description = "Thêm khách thuê mới vào phòng (chỉ Manager)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        description = "Thêm khách thuê thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Dữ liệu không hợp lệ"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy phòng")
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<TenantResponse> createTenant(
            @RequestBody @Valid final TenantCreationRequest request) {
        final var response = tenantService.createTenant(request);
        return ApiResponse.<TenantResponse>builder()
                .result(response)
                .message("Thêm khách thuê thành công")
                .build();
    }

    @Operation(
            summary = "Danh sách khách thuê",
            description =
                    "Lấy danh sách khách thuê với filter (có phân trang). "
                            + "Có thể filter theo buildingId, roomId, active (true = đang thuê, false = đã kết thúc). "
                            + "Sử dụng query parameters: page, size, sort, buildingId, roomId, active")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy danh sách khách thuê thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy tòa nhà")
            })
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public PageResponse<TenantResponse> getTenants(
            @Parameter(description = "ID tòa nhà", example = "1")
                    @RequestParam(value = "buildingId", required = false)
                    final Integer buildingId,
            @Parameter(description = "ID phòng", example = "1")
                    @RequestParam(value = "roomId", required = false)
                    final Integer roomId,
            @Parameter(
                            description =
                                    "Lọc theo trạng thái hợp đồng (true = đang thuê, false = đã kết thúc)",
                            example = "true")
                    @RequestParam(value = "active", required = false)
                    final Boolean active,
            @Parameter(description = "Thông tin phân trang (page, size, sort)")
                    @PageableDefault(
                            size = PaginationConfig.DEFAULT_PAGE_SIZE,
                            sort = "startDate",
                            direction = Sort.Direction.DESC)
                    final Pageable pageable) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var currentUserId = authentication.getName();

        // Verify building belongs to current manager if buildingId is provided
        if (buildingId != null) {
            buildingRepository
                    .findByIdAndManagerId(buildingId, currentUserId)
                    .orElseThrow(
                            () ->
                                    new com.tpanh.backend.exception.AppException(
                                            com.tpanh.backend.exception.ErrorCode
                                                    .BUILDING_NOT_FOUND));
        }

        return tenantService.getTenants(buildingId, roomId, active, pageable);
    }

    @Operation(
            summary = "Thông tin khách",
            description = "Lấy thông tin chi tiết khách thuê (chỉ Manager)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy thông tin khách thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy khách thuê")
            })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ApiResponse<TenantResponse> getTenantById(@PathVariable("id") final Integer id) {
        final var response = tenantService.getTenantById(id);
        return ApiResponse.<TenantResponse>builder()
                .result(response)
                .message("Lấy thông tin khách thành công")
                .build();
    }

    @Operation(
            summary = "Cập nhật thông tin khách thuê",
            description =
                    "Cập nhật thông tin khách thuê (tên, số điện thoại, email, người đại diện) - chỉ Manager")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Cập nhật thông tin khách thuê thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Dữ liệu không hợp lệ"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy khách thuê")
            })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<TenantResponse> updateTenant(
            @PathVariable("id") final Integer id,
            @RequestBody @Valid final TenantUpdateRequest request) {
        final var response = tenantService.updateTenant(id, request);
        return ApiResponse.<TenantResponse>builder()
                .result(response)
                .message("Cập nhật thông tin khách thuê thành công")
                .build();
    }

    @Operation(
            summary = "Kết thúc hợp đồng",
            description = "Kết thúc hợp đồng thuê của khách (chỉ Manager)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Kết thúc hợp đồng thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Hợp đồng đã được kết thúc trước đó"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy khách thuê")
            })
    @PutMapping("/{id}/end")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<TenantResponse> endTenantContract(@PathVariable("id") final Integer id) {
        final var response = tenantService.endTenantContract(id);
        return ApiResponse.<TenantResponse>builder()
                .result(response)
                .message("Kết thúc hợp đồng thành công")
                .build();
    }
}
