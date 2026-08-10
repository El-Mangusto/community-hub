package com.elmangusto.communityhub.controller.admin.v1;

import com.elmangusto.communityhub.dto.request.UserRoleRequest;
import com.elmangusto.communityhub.dto.request.UserStatusRequest;
import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.security.CustomUserDetails;
import com.elmangusto.communityhub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getById(id, principal);
    }

    @GetMapping
    public Page<UserResponse> getAll(
            @ParameterObject
            @PageableDefault(size = 10, sort = "login")
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getAll(pageable, principal);
    }

    @PatchMapping("/{id}/ban")
    public UserResponse setStatus(@PathVariable Long id,
                                     @RequestBody @Valid UserStatusRequest request,
                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.setStatus(id, request, principal);
    }

    @PatchMapping("/{id}/role")
    public UserResponse setRole(@PathVariable Long id,
                                @RequestBody @Valid UserRoleRequest request,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.setRole(id, request, principal);
    }
}
