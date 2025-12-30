package com.tpanh.backend.security;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

public class UserPrincipal implements Principal {
    private final String userId;
    private final List<String> roles;

    public UserPrincipal(final String userId, final List<String> roles) {
        this.userId = userId;
        this.roles = roles != null ? List.copyOf(roles) : List.of();
    }

    @Override
    public String getName() {
        return userId;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public boolean hasRole(final String role) {
        return roles.contains("ROLE_" + role) || roles.contains(role);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(userId, that.userId) && Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roles);
    }

    @Override
    public String toString() {
        return "UserPrincipal{userId='" + userId + "', roles=" + roles + "}";
    }
}
