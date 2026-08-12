package com.elmangusto.communityhub.controller.api.v1;

import com.elmangusto.communityhub.config.SecurityConfig;
import com.elmangusto.communityhub.dto.request.LoginRequest;
import com.elmangusto.communityhub.dto.request.UserRegisterRequest;
import com.elmangusto.communityhub.dto.response.AuthResponse;
import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.entity.enums.UserRole;
import com.elmangusto.communityhub.entity.enums.UserStatus;
import com.elmangusto.communityhub.security.CustomUserDetailsService;
import com.elmangusto.communityhub.security.JwtAuthFilter;
import com.elmangusto.communityhub.service.AuthService;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void register_returns201_whenValidRequest() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("testUser", "password123");
        UserResponse response = new UserResponse(1L, "testUser", UserRole.USER, UserStatus.ACTIVE);

        when(authService.register(any(UserRegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_returns400_whenUsernameBlank() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns200_whenValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("testUser", "password123");
        AuthResponse response = new AuthResponse("jwt-token-value");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-value"));
    }

    @Test
    void login_returns400_whenPasswordBlank() throws Exception {
        LoginRequest request = new LoginRequest("testUser", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}