package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.BuildingCreationRequest;
import com.tpanh.backend.dto.BuildingResponse;
import com.tpanh.backend.dto.RoomResponse;
import com.tpanh.backend.service.BuildingService;
import com.tpanh.backend.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
            summary = "Chi tiết tòa nhà",
            description = "Lấy thông tin chi tiết tòa nhà (chỉ Manager)")
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
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<BuildingResponse> getBuildingById(@PathVariable final Integer id) {
        final var response = buildingService.getBuildingById(id);
        return ApiResponse.<BuildingResponse>builder()
                .result(response)
                .message("Lấy thông tin tòa nhà thành công")
                .build();
    }

    @Operation(
            summary = "Danh sách phòng trong tòa nhà",
            description = "Lấy danh sách tất cả phòng trong một tòa nhà (chỉ Manager)")
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
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<RoomResponse>> getRoomsByBuildingId(@PathVariable final Integer id) {
        final var response = roomService.getRoomsByBuildingId(id);
        return ApiResponse.<List<RoomResponse>>builder()
                .result(response)
                .message("Lấy danh sách phòng thành công")
                .build();
    }
}
