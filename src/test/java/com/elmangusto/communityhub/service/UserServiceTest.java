package com.elmangusto.communityhub.service;

import com.elmangusto.communityhub.dto.request.UserRoleRequest;
import com.elmangusto.communityhub.dto.request.UserStatusRequest;
import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.entity.User;
import com.elmangusto.communityhub.entity.enums.UserRole;
import com.elmangusto.communityhub.entity.enums.UserStatus;
import com.elmangusto.communityhub.exception.ResourceNotFoundException;
import com.elmangusto.communityhub.mapper.UserMapper;
import com.elmangusto.communityhub.repository.UserRepository;
import com.elmangusto.communityhub.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getById_shouldReturnUser_whenRequestedByAdmin() {

        User user = getUser();
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, UserRole.ADMIN);

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        UserResponse userResponse = getUserResponse();

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        UserResponse result = userService.getById(OWNER_ID, principal);

        assertThat(result).isEqualTo(userResponse);
    }

    @Test
    void getById_shouldThrowAccessDeniedException_whenRequestedByAnotherUser() {

        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, UserRole.USER);

        assertThatThrownBy(() -> userService.getById(OWNER_ID, principal))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRepository, userMapper);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, UserRole.ADMIN);

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(OWNER_ID, principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(userRepository).findById(OWNER_ID);
    }

    @Test
    void getAll_shouldReturnPageOfUser_whenUsersExist() {

        User user = getUser();
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, UserRole.ADMIN);
        UserResponse userResponse = getUserResponse();

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable))
                .thenReturn(userPage);

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        Page<UserResponse> result = userService.getAll(pageable, principal);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(userResponse);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(userRepository).findAll(pageable);
        verify(userMapper).toResponse(user);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoUsersExist() {

        Pageable pageable = PageRequest.of(0, 10);
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, UserRole.ADMIN);
        Page<User> emptyPage = Page.empty(pageable);

        when(userRepository.findAll(pageable))
                .thenReturn(emptyPage);

        Page<UserResponse> result = userService.getAll(pageable, principal);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(userRepository).findAll(pageable);
        verifyNoInteractions(userMapper);
    }

    @Test
    void setStatus_shouldUpdateStatus_whenUserExists() {
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, UserRole.ADMIN);

        User user = getUser();
        User saved = getUser();
        saved.setStatus(UserStatus.BANNED);

        UserResponse response = new UserResponse(
                1L,
                "testUser",
                UserRole.USER,
                UserStatus.BANNED
        );

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(saved);

        when(userMapper.toResponse(saved))
                .thenReturn(response);

        UserStatusRequest request = new UserStatusRequest(UserStatus.BANNED);

        UserResponse result = userService.setStatus(OWNER_ID, request, principal);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(UserStatus.BANNED);

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void setStatus_shouldReturnUnchanged_whenStatusIsSame() {
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, UserRole.ADMIN);

        User user = getUser();
        UserResponse userResponse = getUserResponse();

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        UserStatusRequest request = new UserStatusRequest(UserStatus.ACTIVE);

        UserResponse result = userService.setStatus(OWNER_ID, request, principal);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper).toResponse(user);
    }

    @Test
    void setStatus_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, UserRole.ADMIN);

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.empty());

        UserStatusRequest request = new UserStatusRequest(UserStatus.BANNED);

        assertThatThrownBy(() -> userService.setStatus(OWNER_ID, request, principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void setStatus_shouldThrowAccessDeniedException_whenActingOnSelf() {

        CustomUserDetails principal = getPrincipal(OWNER_ID, UserRole.ADMIN);

        UserStatusRequest request = new UserStatusRequest(UserStatus.BANNED);

        assertThatThrownBy(() -> userService.setStatus(OWNER_ID, request, principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("yourself");

        verifyNoInteractions(userRepository, userMapper);
    }

    @Test
    void setRole_shouldUpdateRole_whenUserExists() {

        User user = getUser();
        User saved = getUser();
        saved.setRole(UserRole.ADMIN);
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, UserRole.SUPER_ADMIN);

        UserResponse response = new UserResponse(
                1L,
                "testUser",
                UserRole.ADMIN,
                UserStatus.ACTIVE
        );

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(saved);

        when(userMapper.toResponse(saved))
                .thenReturn(response);

        UserRoleRequest request = new UserRoleRequest(UserRole.ADMIN);

        UserResponse result = userService.setRole(OWNER_ID, request, principal);

        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(UserRole.ADMIN);

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void setRole_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, UserRole.SUPER_ADMIN);

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.empty());

        UserRoleRequest request = new UserRoleRequest(UserRole.ADMIN);

        assertThatThrownBy(() -> userService.setRole(OWNER_ID, request, principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void setRole_shouldThrowAccessDeniedException_whenActingOnSelf() {

        CustomUserDetails principal = getPrincipal(OWNER_ID, UserRole.SUPER_ADMIN);

        UserRoleRequest request = new UserRoleRequest(UserRole.ADMIN);

        assertThatThrownBy(() -> userService.setRole(OWNER_ID, request, principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own role");

        verifyNoInteractions(userRepository, userMapper);
    }



    private static User getUser() {
        return User.builder()
                .id(OWNER_ID)
                .username("testUser")
                .password("12345678")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private static CustomUserDetails getPrincipal(Long userId, UserRole role) {
        User user = User.builder()
                .id(userId)
                .role(role)
                .build();
        return new CustomUserDetails(user);
    }

    private static UserResponse getUserResponse() {
        return new UserResponse(
                1L,
                "testUser",
                UserRole.USER,
                UserStatus.ACTIVE
        );
    }

}
