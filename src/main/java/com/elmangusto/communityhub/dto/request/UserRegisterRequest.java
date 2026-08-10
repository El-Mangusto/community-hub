package com.elmangusto.communityhub.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserRegisterRequest(

        @NotBlank(message = "Username number is required")
        String username,

        @NotBlank(message = "Password number is required")
        String password
) {}
