package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.UtilityReadingCreationRequest;
import com.tpanh.backend.dto.UtilityReadingResponse;
import com.tpanh.backend.dto.UtilityReadingUpdateRequest;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.service.UtilityReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("${app.api-prefix}/utility-readings")
@RequiredArgsConstructor
@Tag(name = "Utility Readings", description = "API ghi chỉ số điện nước (dành cho Manager)")
public class UtilityReadingController {

    private final UtilityReadingService utilityReadingService;
    private final BuildingRepository buildingRepository;

    @Operation(
            summary = "Ghi chỉ số điện nước",
            description =
                    "Ghi chỉ số điện nước cho một phòng trong tháng. "
                            + "Manager có thể chụp ảnh đồng hồ và lưu URL ảnh. "
                            + "Mỗi phòng chỉ có thể ghi một lần cho mỗi tháng.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        description = "Ghi chỉ số thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Dữ liệu không hợp lệ hoặc đã tồn tại"),
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
    public ApiResponse<UtilityReadingResponse> createUtilityReading(
            @RequestBody @Valid final UtilityReadingCreationRequest request) {
        final var response = utilityReadingService.createUtilityReading(request);
        return ApiResponse.<UtilityReadingResponse>builder()
                .result(response)
                .message("Ghi chỉ số điện nước thành công")
                .build();
    }

    @Operation(
            summary = "Cập nhật chỉ số điện nước",
            description = "Cập nhật chỉ số điện nước đã ghi (chỉ Manager)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Cập nhật thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy bản ghi")
            })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<UtilityReadingResponse> updateUtilityReading(
            @PathVariable("id") final Integer id,
            @RequestBody @Valid final UtilityReadingUpdateRequest request) {
        final var response = utilityReadingService.updateUtilityReading(id, request);
        return ApiResponse.<UtilityReadingResponse>builder()
                .result(response)
                .message("Cập nhật chỉ số điện nước thành công")
                .build();
    }

    @Operation(
            summary = "Chi tiết chỉ số điện nước",
            description = "Lấy thông tin chi tiết một bản ghi chỉ số điện nước")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy thông tin thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy bản ghi")
            })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ApiResponse<UtilityReadingResponse> getUtilityReadingById(
            @PathVariable("id") final Integer id) {
        final var response = utilityReadingService.getUtilityReadingById(id);
        return ApiResponse.<UtilityReadingResponse>builder()
                .result(response)
                .message("Lấy thông tin chỉ số điện nước thành công")
                .build();
    }

    @Operation(
            summary = "Lịch sử chỉ số điện nước của phòng",
            description = "Lấy danh sách tất cả chỉ số điện nước đã ghi của một phòng")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy danh sách thành công")
            })
    @GetMapping("/rooms/{roomId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ApiResponse<List<UtilityReadingResponse>> getUtilityReadingsByRoomId(
            @PathVariable("roomId") final Integer roomId) {
        final var response = utilityReadingService.getUtilityReadingsByRoomId(roomId);
        return ApiResponse.<List<UtilityReadingResponse>>builder()
                .result(response)
                .message("Lấy lịch sử chỉ số điện nước thành công")
                .build();
    }

    @Operation(
            summary = "Chỉ số điện nước theo tòa nhà và tháng",
            description =
                    "Lấy danh sách chỉ số điện nước của tất cả phòng trong tòa nhà theo tháng. "
                            + "Sử dụng để kiểm tra trước khi tạo hóa đơn.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy danh sách thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy tòa nhà")
            })
    @GetMapping("/buildings/{buildingId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<UtilityReadingResponse>> getUtilityReadingsByBuildingAndMonth(
            @PathVariable("buildingId") final Integer buildingId,
            @Parameter(description = "Tháng (VD: 2025-01)", example = "2025-01")
                    @RequestParam("month")
                    final String month) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var currentUserId = authentication.getName();

        // Verify building belongs to current manager
        buildingRepository
                .findByIdAndManagerId(buildingId, currentUserId)
                .orElseThrow(
                        () ->
                                new com.tpanh.backend.exception.AppException(
                                        com.tpanh.backend.exception.ErrorCode.BUILDING_NOT_FOUND));

        final var response =
                utilityReadingService.getUtilityReadingsByBuildingAndMonth(buildingId, month);
        return ApiResponse.<List<UtilityReadingResponse>>builder()
                .result(response)
                .message("Lấy danh sách chỉ số điện nước thành công")
                .build();
    }
}
