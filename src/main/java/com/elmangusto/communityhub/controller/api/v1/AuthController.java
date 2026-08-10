package com.elmangusto.communityhub.controller.api.v1;

import com.elmangusto.communityhub.dto.request.LoginRequest;
import com.elmangusto.communityhub.dto.request.UserRegisterRequest;
import com.elmangusto.communityhub.dto.response.AuthResponse;
import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody @Valid UserRegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }
}
