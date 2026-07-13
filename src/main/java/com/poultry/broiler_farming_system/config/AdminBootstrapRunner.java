package com.poultry.broiler_farming_system.config;

import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Registration always creates FREE users (see RegisterRequest), so without
// this there would be no way to reach an ADMIN account at all short of a
// manual DB write. Runs once at startup; no-ops once any ADMIN exists.
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap-enabled}")
    private boolean bootstrapEnabled;

    @Value("${app.admin.default-username}")
    private String defaultUsername;

    @Value("${app.admin.default-email}")
    private String defaultEmail;

    @Value("${app.admin.default-password}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        if (!bootstrapEnabled || userRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }

        User admin = new User();
        admin.setFullName("System Administrator");
        admin.setUsername(defaultUsername);
        admin.setPhoneNumber("0000000000");
        admin.setEmail(defaultEmail);
        admin.setPassword(passwordEncoder.encode(defaultPassword));
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);

        log.warn("Bootstrapped a default ADMIN account (username='{}'). Change its password immediately.",
                defaultUsername);
    }
}
