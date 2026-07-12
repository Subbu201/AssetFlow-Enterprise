package com.assetflow.auth.service;

import com.assetflow.auth.entity.UserAccount;
import com.assetflow.auth.repository.UserRepository;
import com.assetflow.common.RecordStatus;
import com.assetflow.common.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean bootstrapAdminEnabled;

    @Value("${app.bootstrap.admin.email:admin@assetflow.com}")
    private String bootstrapAdminEmail;

    @Value("${app.bootstrap.admin.password:Admin@123}")
    private String bootstrapAdminPassword;

    @Value("${app.bootstrap.admin.name:AssetFlow Admin}")
    private String bootstrapAdminName;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrapAdmin() {
        if (!bootstrapAdminEnabled) {
            log.info("Admin bootstrap is disabled.");
            return;
        }

        boolean adminExists = userRepository.existsByRole(Role.ADMIN);
        if (adminExists) {
            log.info("An ADMIN account already exists. Skipping bootstrap.");
            return;
        }

        UserAccount admin = UserAccount.builder()
                .fullName(bootstrapAdminName)
                .email(bootstrapAdminEmail.toLowerCase())
                .password(passwordEncoder.encode(bootstrapAdminPassword))
                .role(Role.ADMIN)
                .status(RecordStatus.ACTIVE)
                .build();

        userRepository.save(admin);
        log.info("Successfully bootstrapped initial ADMIN account: {}", bootstrapAdminEmail);
    }
}
