package com.tpanh.backend.security;

import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public UserPrincipal get() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return principal;
    }

    public String getUserId() {
        return get().getUserId();
    }

    public boolean hasRole(final String role) {
        return get().hasRole(role);
    }
}
