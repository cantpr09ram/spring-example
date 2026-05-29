package com.example.example.auth;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String email;
    private final String password;
    private final String displayName;

    public AdminBootstrap(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.bootstrap-admin.enabled:true}") boolean enabled,
            @Value("${app.security.bootstrap-admin.email:admin@example.com}") String email,
            @Value("${app.security.bootstrap-admin.password:admin12345}") String password,
            @Value("${app.security.bootstrap-admin.display-name:Admin}") String displayName) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled || users.countByRole(Role.ADMIN) > 0) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        users.save(new User(
                normalizedEmail,
                passwordEncoder.encode(password),
                displayName.trim(),
                Role.ADMIN));
        log.warn("Created bootstrap admin account for {}", normalizedEmail);
    }
}
