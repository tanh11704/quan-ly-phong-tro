package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.UserDTO;
import com.tpanh.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api-prefix}/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "API quản lý hệ thống (chỉ dành cho Admin)")
public class AdminController {
    private final UserService userService;

    @Operation(
            summary = "Danh sách người dùng",
            description = "Admin xem danh sách tất cả người dùng (đặc biệt là Manager) để hỗ trợ")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy danh sách người dùng thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Chỉ Admin mới có quyền truy cập")
            })
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserDTO>> getAllUsers() {
        final var users = userService.getAllUsers();
        return ApiResponse.<List<UserDTO>>builder()
                .result(users)
                .message("Lấy danh sách người dùng thành công")
                .build();
    }

    @Operation(
            summary = "Khóa/Mở khóa tài khoản Manager",
            description =
                    "Admin có quyền khóa (deactivate) hoặc mở khóa (activate) tài khoản Manager khi vi phạm pháp luật")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Cập nhật trạng thái tài khoản thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Chỉ Admin mới có quyền thực hiện"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy người dùng")
            })
    @PutMapping("/users/{userId}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDTO> toggleUserActive(@PathVariable final String userId) {
        final var userDTO = userService.toggleUserActive(userId);
        return ApiResponse.<UserDTO>builder()
                .result(userDTO)
                .message(
                        userDTO.getActive()
                                ? "Đã mở khóa tài khoản thành công"
                                : "Đã khóa tài khoản thành công")
                .build();
    }
}
