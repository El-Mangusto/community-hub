package com.elmangusto.communityhub.controller.admin.v1;

import com.elmangusto.communityhub.config.SecurityConfig;
import com.elmangusto.communityhub.dto.request.UserRoleRequest;
import com.elmangusto.communityhub.dto.request.UserStatusRequest;
import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.entity.User;
import com.elmangusto.communityhub.entity.enums.UserRole;
import com.elmangusto.communityhub.entity.enums.UserStatus;
import com.elmangusto.communityhub.security.CustomUserDetails;
import com.elmangusto.communityhub.security.CustomUserDetailsService;
import com.elmangusto.communityhub.security.JwtAuthFilter;
import com.elmangusto.communityhub.service.UserService;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import(SecurityConfig.class)
class AdminUserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private CustomUserDetails adminPrincipal;
    private CustomUserDetails superAdminPrincipal;
    private CustomUserDetails userPrincipal;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());

        adminPrincipal = new CustomUserDetails(User.builder()
                .id(1L).username("admin").role(UserRole.ADMIN).status(UserStatus.ACTIVE).build());

        superAdminPrincipal = new CustomUserDetails(User.builder()
                .id(2L).username("superadmin").role(UserRole.SUPER_ADMIN).status(UserStatus.ACTIVE).build());

        userPrincipal = new CustomUserDetails(User.builder()
                .id(3L).username("plainUser").role(UserRole.USER).status(UserStatus.ACTIVE).build());
    }

    @Test
    void getById_returns200_whenAdmin() throws Exception {
        UserResponse response = new UserResponse(5L, "targetUser", UserRole.USER, UserStatus.ACTIVE);
        when(userService.getById(eq(5L), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/users/5")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getById_returns403_whenPlainUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/5")
                        .with(user(userPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_returns200_whenAdmin() throws Exception {
        UserResponse response = new UserResponse(5L, "targetUser", UserRole.USER, UserStatus.ACTIVE);
        Page<UserResponse> page = new PageImpl<>(List.of(response));

        when(userService.getAll(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5));
    }

    @Test
    void getAll_returns403_whenPlainUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user(userPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void setStatus_returns200_whenAdmin() throws Exception {
        UserStatusRequest request = new UserStatusRequest(UserStatus.BANNED);
        UserResponse response = new UserResponse(5L, "targetUser", UserRole.USER, UserStatus.BANNED);

        when(userService.setStatus(eq(5L), any(UserStatusRequest.class), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/users/5/status")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BANNED"));
    }

    @Test
    void setStatus_returns403_whenPlainUser() throws Exception {
        UserStatusRequest request = new UserStatusRequest(UserStatus.BANNED);

        mockMvc.perform(patch("/api/v1/admin/users/5/status")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void setRole_returns200_whenSuperAdmin() throws Exception {
        UserRoleRequest request = new UserRoleRequest(UserRole.ADMIN);
        UserResponse response = new UserResponse(5L, "targetUser", UserRole.ADMIN, UserStatus.ACTIVE);

        when(userService.setRole(eq(5L), any(UserRoleRequest.class), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/users/5/role")
                        .with(user(superAdminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void setRole_returns403_whenOnlyAdmin() throws Exception {
        UserRoleRequest request = new UserRoleRequest(UserRole.ADMIN);

        mockMvc.perform(patch("/api/v1/admin/users/5/role")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void setRole_returns401_whenNotAuthenticated() throws Exception {
        UserRoleRequest request = new UserRoleRequest(UserRole.ADMIN);

        mockMvc.perform(patch("/api/v1/admin/users/5/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}