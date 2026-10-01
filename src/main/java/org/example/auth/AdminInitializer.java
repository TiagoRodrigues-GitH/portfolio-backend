package org.example.auth;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Creates or updates the single admin account from the environment:
 * ADMIN_EMAIL and ADMIN_PASSWORD_HASH (a BCrypt hash, never the plain password).
 * Generate the hash locally, e.g. with `htpasswd -bnBC 12 "" 'your-password' | tr -d ':\n'`.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository users;
    private final String email;
    private final String passwordHash;

    public AdminInitializer(UserRepository users,
                            @Value("${app.admin.email:}") String email,
                            @Value("${app.admin.password-hash:}") String passwordHash) {
        this.users = users;
        this.email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash == null ? "" : passwordHash.trim();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isEmpty() || passwordHash.isEmpty()) {
            LOG.warn("ADMIN_EMAIL / ADMIN_PASSWORD_HASH not set: no admin account, login is disabled.");
            return;
        }
        if (!passwordHash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$")) {
            throw new IllegalStateException("ADMIN_PASSWORD_HASH must be a BCrypt hash, not a plain password.");
        }
        User admin = users.findByEmail(email).orElseGet(() -> User.builder()
                .email(email).fullName("Administrator").createdAt(LocalDateTime.now()).build());
        admin.setPassword(passwordHash);
        admin.setRole(User.Role.ADMIN);
        users.save(admin);
        LOG.info("Admin account ready.");
    }
}
