package com.elmangusto.communityhub.dto.request;

import com.elmangusto.communityhub.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull
        UserStatus status
) {}
