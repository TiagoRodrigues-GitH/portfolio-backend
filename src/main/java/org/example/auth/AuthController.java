package org.example.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

/** POST /api/auth/login: e-mail + password (BCrypt) -> short-lived JWT with scope ADMIN. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(@NotBlank @Email @Size(max = 200) String email,
                               @NotBlank @Size(max = 200) String password) { }

    public record LoginResponse(String token, long expiresIn) { }

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final LoginAttempts attempts;
    private final Duration ttl;
    /** Compared against when the e-mail is unknown, so both paths take the same time. */
    private final String dummyHash;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
                          LoginAttempts attempts, @Value("${app.jwt.ttl-minutes:120}") long ttlMinutes) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.attempts = attempts;
        this.ttl = Duration.ofMinutes(ttlMinutes);
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String client = http.getRemoteAddr();
        if (attempts.isBlocked(client)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts");
        }
        User user = users.findByEmail(request.email().trim().toLowerCase(Locale.ROOT)).orElse(null);
        boolean ok = passwordEncoder.matches(request.password(), user != null ? user.getPassword() : dummyHash)
                && user != null && user.getRole() == User.Role.ADMIN;
        if (!ok) {
            attempts.failed(client);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        attempts.succeeded(client);
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("portfolio-backend")
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim("scope", "ADMIN")
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, ttl.toSeconds());
    }
}
