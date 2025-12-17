package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.AuthenticationRequest;
import com.tpanh.backend.dto.AuthenticationResponse;
import com.tpanh.backend.dto.ExchangeTokenRequest;
import com.tpanh.backend.dto.IntrospectRequest;
import com.tpanh.backend.dto.IntrospectResponse;
import com.tpanh.backend.dto.RegistrationRequest;
import com.tpanh.backend.dto.RegistrationResponse;
import com.tpanh.backend.service.AuthenticationService;
import com.tpanh.backend.service.JwtService;
import com.tpanh.backend.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api-prefix}")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API xác thực người dùng")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final RegistrationService registrationService;

    @Operation(
            summary = "Đăng nhập truyền thống",
            description = "Xác thực bằng tên đăng nhập và mật khẩu (dành cho Admin)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Đăng nhập thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Thông tin đăng nhập không đúng")
            })
    @PostMapping("/auth/token")
    public ApiResponse<AuthenticationResponse> authenticate(
            @RequestBody @Valid final AuthenticationRequest request) {
        final var response = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(response)
                .message("Đăng nhập thành công")
                .build();
    }

    @Operation(
            summary = "Đăng nhập truyền thống (Alias)",
            description = "Alias endpoint cho /auth/token để tương thích với client")
    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticateAlias(
            @RequestBody @Valid final AuthenticationRequest request) {
        return authenticate(request);
    }

    @Operation(
            summary = "Đăng nhập qua Zalo",
            description = "Trao đổi token Zalo lấy token hệ thống (dành cho Khách thuê)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Xác thực thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Token Zalo không hợp lệ")
            })
    @PostMapping("/auth/outbound/authentication")
    public ApiResponse<AuthenticationResponse> outboundAuthenticate(
            @RequestBody @Valid final ExchangeTokenRequest request) {
        final var response = authenticationService.outboundAuthenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(response)
                .message("Xác thực Zalo thành công")
                .build();
    }

    @Operation(
            summary = "Kiểm tra token",
            description = "Kiểm tra tính hợp lệ của JWT token mà không cần giải mã thủ công")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Kết quả kiểm tra token")
            })
    @PostMapping("/auth/introspect")
    public ApiResponse<IntrospectResponse> introspect(
            @RequestBody @Valid final IntrospectRequest request) {
        final var isValid = jwtService.verifyToken(request.getToken());
        final var response = new IntrospectResponse(isValid);
        return ApiResponse.<IntrospectResponse>builder()
                .result(response)
                .message(isValid ? "Token hợp lệ" : "Token không hợp lệ hoặc đã hết hạn")
                .build();
    }

    @Operation(
            summary = "Đăng ký tài khoản Manager",
            description =
                    "Đăng ký tài khoản Manager mới. Tài khoản sẽ ở trạng thái PENDING cho đến khi được kích hoạt qua email.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Đăng ký thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Thông tin đăng ký không hợp lệ hoặc đã tồn tại")
            })
    @PostMapping("/auth/register")
    public ApiResponse<RegistrationResponse> register(
            @RequestBody @Valid final RegistrationRequest request) {
        final var response = registrationService.register(request);
        return ApiResponse.<RegistrationResponse>builder()
                .result(response)
                .message(response.getMessage())
                .build();
    }

    @Operation(
            summary = "Kích hoạt tài khoản",
            description = "Kích hoạt tài khoản Manager bằng token nhận được từ email")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Kích hoạt thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Token không hợp lệ hoặc đã hết hạn")
            })
    @GetMapping("/auth/activate")
    public ApiResponse<String> activate(@RequestParam("token") final String token) {
        registrationService.activateAccount(token);
        return ApiResponse.<String>builder()
                .result("Tài khoản đã được kích hoạt thành công")
                .message("Bạn có thể đăng nhập ngay bây giờ")
                .build();
    }
}
