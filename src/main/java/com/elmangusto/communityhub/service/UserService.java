package com.elmangusto.communityhub.service;

import com.elmangusto.communityhub.dto.request.UserRoleRequest;
import com.elmangusto.communityhub.dto.request.UserStatusRequest;
import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.entity.User;
import com.elmangusto.communityhub.entity.enums.UserRole;
import com.elmangusto.communityhub.exception.ResourceNotFoundException;
import com.elmangusto.communityhub.mapper.UserMapper;
import com.elmangusto.communityhub.repository.UserRepository;
import com.elmangusto.communityhub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable, CustomUserDetails principal) {
        boolean isAdmin = principal.user().getRole() == UserRole.ADMIN;

        if (!isAdmin) {
            throw new AccessDeniedException("Access denied");
        }

        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id, CustomUserDetails principal) {
        boolean isSelf = principal.getId().equals(id);
        boolean isAdmin = principal.user().getRole() == UserRole.ADMIN;

        if (!isSelf && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to view this user");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse setStatus(Long id, UserStatusRequest request, CustomUserDetails principal) {

        log.info("Changing status for userId={} to newStatus={}", id, request.status());

        boolean isSelf = principal.getId().equals(id);

        if (isSelf) {
            throw new AccessDeniedException("You cannot ban/unban yourself");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        boolean actorIsSuperAdmin = principal.user().getRole() == UserRole.SUPER_ADMIN;
        boolean targetIsPrivileged = user.getRole() != UserRole.USER;

        if (targetIsPrivileged && !actorIsSuperAdmin) {
            throw new AccessDeniedException("Only a super admin can change status of an admin account");
        }

        if (user.getStatus() == request.status()) {
            log.info("user id={} already has status={}, no changes applied", id, request.status());
            return userMapper.toResponse(user);
        }

        user.setStatus(request.status());

        User saved = userRepository.save(user);

        log.info("User id={} status changed successfully to {}", saved.getId(), saved.getStatus());

        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse setRole(Long id, UserRoleRequest request, CustomUserDetails principal) {

        log.info("Changing role for userId={} to newRole={}", id, request.role());

        if (principal.getId().equals(id)) {
            throw new AccessDeniedException("You cannot change your own role");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setRole(request.role());

        User saved = userRepository.save(user);

        log.info("User id={} role changed successfully to {}", saved.getId(), saved.getRole());

        return userMapper.toResponse(saved);
    }
}
