package com.elmangusto.communityhub.dto.request;

import com.elmangusto.communityhub.entity.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleRequest(
        @NotNull
        UserRole role
) {}
