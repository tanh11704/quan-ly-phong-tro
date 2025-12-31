package com.tpanh.backend.dto;

import com.tpanh.backend.enums.Role;
import jakarta.validation.constraints.NotNull;

public record GrantRoleRequest(@NotNull Role role) {}
