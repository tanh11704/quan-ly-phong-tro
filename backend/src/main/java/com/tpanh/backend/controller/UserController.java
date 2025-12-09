package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.UserDTO;
import com.tpanh.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
