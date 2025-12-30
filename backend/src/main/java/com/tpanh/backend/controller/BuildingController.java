package com.tpanh.backend.controller;

import com.tpanh.backend.config.PaginationConfig;
import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.BuildingResponse;
import com.tpanh.backend.dto.BuildingUpdateRequest;
import com.tpanh.backend.dto.PageResponse;
import com.tpanh.backend.dto.RoomResponse;
import com.tpanh.backend.service.BuildingService;
import com.tpanh.backend.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("${app.api-prefix}/buildings")
@RequiredArgsConstructor
@Tag(name = "Buildings", description = "API quản lý tòa nhà (dành cho Manager)")
public class BuildingController {
    private final BuildingService buildingService;
    private final RoomService roomService;

    @Operation(summary = "Tạo tòa nhà mới", description = "Tạo tòa nhà mới (chỉ Manager)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        description = "Tạo tòa nhà thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Dữ liệu không hợp lệ"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập")
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<BuildingResponse> createBuilding(
            @RequestBody @Valid final BuildingCreationRequest request) {
        final var response = buildingService.createBuilding(request);
        return ApiResponse.<BuildingResponse>builder()
                .result(response)
                .message("Tạo tòa nhà thành công")
                .build();
    }

    @Operation(
            summary = "Danh sách tòa nhà của Manager",
            description =
                    "Lấy danh sách tất cả tòa nhà do Manager hiện tại quản lý (có phân trang). "
                            + "Sử dụng query parameters: page (số trang, bắt đầu từ 0), size (số phần tử mỗi trang), sort (sắp xếp)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy danh sách thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập")
            })
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public PageResponse<BuildingResponse> getBuildings(
            @Parameter(description = "Thông tin phân trang (page, size, sort)")
                    @PageableDefault(size = PaginationConfig.DEFAULT_PAGE_SIZE, sort = "id")
                    final Pageable pageable) {
        return buildingService.getBuildingsByCurrentManager(pageable);
    }

    @Operation(
            summary = "Chi tiết tòa nhà",
            description = "Lấy thông tin chi tiết tòa nhà (chỉ Manager sở hữu hoặc Admin)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy thông tin thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy tòa nhà"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập")
            })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ApiResponse<BuildingResponse> getBuildingById(@PathVariable("id") final Integer id) {
        final var response = buildingService.getBuildingById(id);
        return ApiResponse.<BuildingResponse>builder()
                .result(response)
                .message("Lấy thông tin tòa nhà thành công")
                .build();
    }

    @Operation(
            summary = "Cập nhật tòa nhà",
            description = "Cập nhật thông tin tòa nhà (chỉ Manager sở hữu)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Cập nhật thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy tòa nhà"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập")
            })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<BuildingResponse> updateBuilding(
            @PathVariable("id") final Integer id,
            @RequestBody @Valid final BuildingUpdateRequest request) {
        final var response = buildingService.updateBuilding(id, request);
        return ApiResponse.<BuildingResponse>builder()
                .result(response)
                .message("Cập nhật tòa nhà thành công")
                .build();
    }

    @Operation(summary = "Xóa tòa nhà", description = "Xóa tòa nhà (chỉ Manager sở hữu)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Xóa thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy tòa nhà"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập")
            })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<Void> deleteBuilding(@PathVariable("id") final Integer id) {
        buildingService.deleteBuilding(id);
        return ApiResponse.<Void>builder().message("Xóa tòa nhà thành công").build();
    }

    @Operation(
            summary = "Danh sách phòng trong tòa nhà",
            description =
                    "Lấy danh sách tất cả phòng trong một tòa nhà (chỉ Manager, có phân trang). "
                            + "Sử dụng query parameters: page, size, sort, status (lọc theo trạng thái: VACANT, OCCUPIED, MAINTENANCE)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy danh sách phòng thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy tòa nhà"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập")
            })
    @GetMapping("/{id}/rooms")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public PageResponse<RoomResponse> getRoomsByBuildingId(
            @PathVariable("id") final Integer id,
            @Parameter(description = "Lọc theo trạng thái phòng", example = "VACANT")
                    @RequestParam(value = "status", required = false)
                    final com.tpanh.backend.enums.RoomStatus status,
            @Parameter(description = "Thông tin phân trang (page, size, sort)")
                    @PageableDefault(size = PaginationConfig.DEFAULT_PAGE_SIZE, sort = "roomNo")
                    final Pageable pageable) {
        return roomService.getRoomsByBuildingId(id, status, pageable);
    }
}
