package com.elmangusto.communityhub.controller.api.v1;

import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.security.CustomUserDetails;
import com.elmangusto.communityhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getById(principal.getId(), principal);
    }

}