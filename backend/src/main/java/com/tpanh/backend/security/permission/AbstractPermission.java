package com.tpanh.backend.security.permission;

import com.tpanh.backend.security.UserPrincipal;
import org.springframework.security.core.Authentication;

public abstract class AbstractPermission {

    protected UserPrincipal extractPrincipal(final Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal;
    }
}
