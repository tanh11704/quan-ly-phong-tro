package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.GrantRoleRequest;
import com.tpanh.backend.dto.UserDTO;
import com.tpanh.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api-prefix}")
@RequiredArgsConstructor
@Tag(name = "User", description = "API quản lý người dùng")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Lấy thông tin người dùng hiện tại",
            description = "Lấy thông tin của người dùng đang đăng nhập từ JWT token")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy thông tin thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Người dùng chưa đăng nhập hoặc token không hợp lệ")
            })
    @GetMapping("/users/my-info")
    public ApiResponse<UserDTO> getMyInfo() {
        final var userDTO = userService.getCurrentUser();
        return ApiResponse.<UserDTO>builder()
                .result(userDTO)
                .message("Lấy thông tin người dùng thành công")
                .build();
    }

    @Operation(
            summary = "Cấp quyền cho người dùng",
            description = "Cấp quyền cho người dùng (chỉ Admin). Role: MANAGER, ADMIN")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Cấp quyền thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy người dùng")
            })
    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDTO> grantRole(
            @PathVariable("id") final String id,
            @RequestBody @Valid final GrantRoleRequest request) {
        final var userDTO = userService.grantRole(id, request.role());
        return ApiResponse.<UserDTO>builder()
                .result(userDTO)
                .message("Cấp quyền thành công")
                .build();
    }
}
