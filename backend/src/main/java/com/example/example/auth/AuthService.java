package com.example.example.auth;

import java.time.Instant;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.example.auth.AuthDtos.AuthRequest;
import com.example.example.auth.AuthDtos.AuthResponse;
import com.example.example.auth.AuthDtos.RegisterRequest;
import com.example.example.auth.AuthDtos.UserResponse;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long expiresInSeconds;

    public AuthService(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.security.jwt.expires-in-seconds:3600}") long expiresInSeconds) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.expiresInSeconds = expiresInSeconds;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = users.save(new User(
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().trim(),
                request.role() == null ? Role.USER : request.role()));
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteAccount(Long id, String currentUserEmail) {
        User user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getEmail().equals(currentUserEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admins cannot delete their own account");
        }

        if (user.getRole() == Role.ADMIN && users.countByRole(Role.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete the last admin account");
        }

        users.delete(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = users.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> invalidCredentials());

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        return createResponse(user);
    }

    private AuthResponse createResponse(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expiresInSeconds);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("example")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("displayName", user.getDisplayName())
                .claim("role", user.getRole().name())
                .build();
        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
        return new AuthResponse(token, "Bearer", expiresInSeconds, UserResponse.from(user));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
