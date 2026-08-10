package com.elmangusto.communityhub.config;

import com.elmangusto.communityhub.entity.User;
import com.elmangusto.communityhub.entity.enums.UserRole;
import com.elmangusto.communityhub.entity.enums.UserStatus;
import com.elmangusto.communityhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.username}")
    private String username;

    @Value("${admin.bootstrap.password}")
    private String rawPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(UserRole.SUPER_ADMIN)) {
            return;
        }

        User admin = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);
        log.warn("Bootstrap admin created with username={}", username);
    }
}
