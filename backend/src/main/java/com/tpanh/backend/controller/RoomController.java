package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.RoomCreationRequest;
import com.tpanh.backend.dto.RoomResponse;
import com.tpanh.backend.dto.RoomUpdateRequest;
import com.tpanh.backend.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api-prefix}/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "API quản lý phòng trọ (dành cho Manager)")
public class RoomController {
    private final RoomService roomService;

    @Operation(summary = "Thêm phòng mới", description = "Thêm phòng mới vào tòa nhà (chỉ Manager)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Thêm phòng thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Dữ liệu không hợp lệ"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy tòa nhà")
            })
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<RoomResponse> createRoom(
            @RequestBody @Valid final RoomCreationRequest request) {
        final var response = roomService.createRoom(request);
        return ApiResponse.<RoomResponse>builder()
                .result(response)
                .message("Thêm phòng thành công")
                .build();
    }

    @Operation(summary = "Cập nhật phòng", description = "Cập nhật thông tin phòng (chỉ Manager)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Cập nhật phòng thành công"),
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
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<RoomResponse> updateRoom(
            @PathVariable final Integer id, @RequestBody @Valid final RoomUpdateRequest request) {
        final var response = roomService.updateRoom(id, request);
        return ApiResponse.<RoomResponse>builder()
                .result(response)
                .message("Cập nhật phòng thành công")
                .build();
    }

    @Operation(summary = "Xóa phòng", description = "Xóa phòng khỏi hệ thống (chỉ Manager)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "204",
                        description = "Xóa phòng thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy phòng")
            })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteRoom(@PathVariable final Integer id) {
        roomService.deleteRoom(id);
    }
}
