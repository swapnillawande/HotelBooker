package com.swappy.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.swappy.dto.AuthResponse;
import com.swappy.dto.LoginRequest;
import com.swappy.dto.RegisterRequest;
import com.swappy.dto.UserProfileDto;
import com.swappy.entities.AuthSession;
import com.swappy.entities.User;
import com.swappy.entities.enums.Role;
import com.swappy.exception.InvalidCredentialsException;
import com.swappy.repository.AuthSessionRepository;
import com.swappy.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final long sessionHours;

    public AuthService(
            UserRepository userRepository,
            AuthSessionRepository authSessionRepository,
            PasswordEncoder passwordEncoder,
            @Value("${auth.session-hours:168}") long sessionHours) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionHours = sessionHours;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("An account already exists for this email");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.GUEST));
        return createSession(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);
        try {
            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                throw new InvalidCredentialsException();
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidCredentialsException();
        }
        return createSession(user);
    }

    @Transactional
    public Optional<User> authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return authSessionRepository.findActiveSession(
                        hash(rawToken), LocalDateTime.now())
                .map(AuthSession::getUser);
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        authSessionRepository.findActiveSession(
                        hash(rawToken), LocalDateTime.now())
                .ifPresent(session -> session.setRevokedAt(LocalDateTime.now()));
    }

    private AuthResponse createSession(User user) {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(sessionHours);

        AuthSession session = new AuthSession();
        session.setTokenHash(hash(rawToken));
        session.setUser(user);
        session.setExpiresAt(expiresAt);
        authSessionRepository.save(session);
        return new AuthResponse(rawToken, expiresAt, UserProfileDto.from(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
