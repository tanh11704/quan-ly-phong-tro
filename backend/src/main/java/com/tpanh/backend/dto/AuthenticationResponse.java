package com.tpanh.backend.dto;

import com.tpanh.backend.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Phản hồi chứa JWT token và roles sau khi xác thực thành công")
public class AuthenticationResponse {
    @Schema(
            description = "JWT token để sử dụng cho các API tiếp theo",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Roles của người dùng để Frontend điều hướng", example = "[\"ADMIN\"]")
    private Set<Role> roles;
}
