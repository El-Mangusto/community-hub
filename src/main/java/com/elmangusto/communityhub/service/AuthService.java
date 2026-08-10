package com.elmangusto.communityhub.service;

import com.elmangusto.communityhub.dto.request.LoginRequest;
import com.elmangusto.communityhub.dto.request.UserRegisterRequest;
import com.elmangusto.communityhub.dto.response.AuthResponse;
import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.entity.User;
import com.elmangusto.communityhub.exception.ResourceAlreadyExistsException;
import com.elmangusto.communityhub.mapper.UserMapper;
import com.elmangusto.communityhub.repository.UserRepository;
import com.elmangusto.communityhub.security.CustomUserDetails;
import com.elmangusto.communityhub.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserResponse register(UserRegisterRequest request) {

        log.info("Registering user with username={}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceAlreadyExistsException(
                    "User with username '%s' already exists".formatted(request.username()));
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);

        log.info("User registered successfully. id={}, username={}", saved.getId(), saved.getUsername());

        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for username={}", request.username());

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        log.info("Login successful. userId={}, username={}", userDetails.getId(), userDetails.getUsername());

        return new AuthResponse(jwtService.generateToken(userDetails));
    }
}
