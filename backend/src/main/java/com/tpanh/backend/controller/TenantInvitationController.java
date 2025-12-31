package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.TenantInvitationRequest;
import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.security.CurrentUser;
import com.tpanh.backend.security.UserPrincipal;
import com.tpanh.backend.service.TenantInvitationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant-invitations")
@RequiredArgsConstructor
public class TenantInvitationController {

    private final TenantInvitationService invitationService;
    private final CurrentUser currentUser;

    @PostMapping
    public ApiResponse<String> inviteTenant(
            @Valid @RequestBody final TenantInvitationRequest request) {
        invitationService.inviteTenant(request, currentUser.getUserId());
        return ApiResponse.success("Gửi lời mời thành công");
    }

    @PostMapping("/{token}/accept")
    public ApiResponse<TenantResponse> acceptInvitation(
            @PathVariable final UUID token,
            @AuthenticationPrincipal final UserPrincipal userPrincipal) {
        final var tenantResponse = invitationService.acceptInvitation(token, userPrincipal);
        return ApiResponse.success(tenantResponse);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> cancelInvitation(@PathVariable final UUID id) {
        invitationService.cancelInvitation(id);
        return ApiResponse.success("Hủy lời mời thành công");
    }
}
