package com.elmangusto.communityhub.service;

import com.elmangusto.communityhub.dto.request.LoginRequest;
import com.elmangusto.communityhub.dto.request.UserRegisterRequest;
import com.elmangusto.communityhub.dto.response.AuthResponse;
import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.entity.User;
import com.elmangusto.communityhub.entity.enums.UserRole;
import com.elmangusto.communityhub.entity.enums.UserStatus;
import com.elmangusto.communityhub.exception.ResourceAlreadyExistsException;
import com.elmangusto.communityhub.mapper.UserMapper;
import com.elmangusto.communityhub.repository.UserRepository;
import com.elmangusto.communityhub.security.CustomUserDetails;
import com.elmangusto.communityhub.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "12345678";
    private static final String TOKEN = "generated.jwt.token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void registration_shouldSaveUser_whenUsernameIsUnique() {

        UserRegisterRequest request = getUserRegisterRequest();

        User user = new User();
        User userSaved = new User();
        UserResponse userResponse = getUserResponse();

        when(userRepository.existsByUsername("testUser"))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encodedPassword");

        when(userRepository.save(user))
                .thenReturn(userSaved);

        when(userMapper.toResponse(userSaved))
                .thenReturn(userResponse);

        UserResponse result = authService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.username())
                .isEqualTo("testUser");

        verify(userRepository).existsByUsername("testUser");
        verify(userRepository).save(user);
        verify(userMapper).toEntity(request);
        verify(userMapper).toResponse(userSaved);
    }

    @Test
    void registration_shouldThrowResourceAlreadyExistsException_whenUsernameAlreadyExists() {

        UserRegisterRequest request = getUserRegisterRequest();

        when(userRepository.existsByUsername("testUser"))
                .thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("testUser");

        verify(userRepository).existsByUsername("testUser");

        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toEntity(any());
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
        CustomUserDetails userDetails = new CustomUserDetails(getActiveUser());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(TOKEN);

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo(TOKEN);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD));
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void login_shouldThrowBadCredentials_whenPasswordIsWrong() {

        LoginRequest request = new LoginRequest(USERNAME, "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowBadCredentials_whenUsernameDoesNotExist() {

        LoginRequest request = new LoginRequest("unknownLogin", PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowDisabled_whenUserIsBanned() {

        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User is disabled"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DisabledException.class);

        verify(jwtService, never()).generateToken(any());
    }



    private static UserRegisterRequest getUserRegisterRequest() {
        return new UserRegisterRequest("testUser", "12345678");
    }

    private static UserResponse getUserResponse() {
        return new UserResponse(
                1L,
                "testUser",
                UserRole.USER,
                UserStatus.ACTIVE
        );
    }

    private User getActiveUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername(USERNAME);
        user.setPassword("encodedPassword");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
