package com.tpanh.backend.dto;

import com.tpanh.backend.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin người dùng")
public class UserDTO {
    @Schema(description = "ID của người dùng", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Tên đăng nhập (có thể null nếu là user Zalo)", example = "admin")
    private String username;

    @Schema(description = "Tên đầy đủ", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Role của người dùng", example = "ADMIN")
    private Role role;

    @Schema(description = "Trạng thái hoạt động của tài khoản", example = "true")
    private Boolean active;
}
