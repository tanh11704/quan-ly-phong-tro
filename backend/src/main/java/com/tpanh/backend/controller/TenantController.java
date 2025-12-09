package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.TenantCreationRequest;
import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api-prefix}/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "API quản lý khách thuê (dành cho Manager)")
public class TenantController {
    private final TenantService tenantService;

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
    public ApiResponse<TenantResponse> getTenantById(@PathVariable final Integer id) {
        final var response = tenantService.getTenantById(id);
        return ApiResponse.<TenantResponse>builder()
                .result(response)
                .message("Lấy thông tin khách thành công")
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
    public ApiResponse<TenantResponse> endTenantContract(@PathVariable final Integer id) {
        final var response = tenantService.endTenantContract(id);
        return ApiResponse.<TenantResponse>builder()
                .result(response)
                .message("Kết thúc hợp đồng thành công")
                .build();
    }
}
