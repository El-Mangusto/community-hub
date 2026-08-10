package com.elmangusto.communityhub.dto.response;

import com.elmangusto.communityhub.entity.enums.UserRole;
import com.elmangusto.communityhub.entity.enums.UserStatus;

public record UserResponse(
        Long id,
        String username,
        UserRole role,
        UserStatus status
) {}
