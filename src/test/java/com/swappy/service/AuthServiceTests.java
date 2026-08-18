package com.swappy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.swappy.dto.AuthResponse;
import com.swappy.dto.LoginRequest;
import com.swappy.dto.RegisterRequest;
import com.swappy.entities.AuthSession;
import com.swappy.entities.User;
import com.swappy.exception.InvalidCredentialsException;
import com.swappy.repository.AuthSessionRepository;
import com.swappy.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock private UserRepository userRepository;
    @Mock private AuthSessionRepository authSessionRepository;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        authService = new AuthService(userRepository, authSessionRepository, passwordEncoder, 24);
    }

    @Test
    void registersUserWithNormalizedEmailHashedPasswordAndHashedSessionToken() {
        when(userRepository.existsByEmailIgnoreCase("alex@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });

        AuthResponse response = authService.register(new RegisterRequest(
                " Alex Doe ", " Alex@Example.COM ", "strong-password"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("alex@example.com", savedUser.getEmail());
        assertEquals("Alex Doe", savedUser.getName());
        assertTrue(passwordEncoder.matches("strong-password", savedUser.getPassword()));

        ArgumentCaptor<AuthSession> sessionCaptor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionRepository).save(sessionCaptor.capture());
        AuthSession savedSession = sessionCaptor.getValue();
        assertEquals(64, savedSession.getTokenHash().length());
        assertNotEquals(response.accessToken(), savedSession.getTokenHash());
        assertEquals(43, response.accessToken().length());
        assertNotNull(response.expiresAt());
    }

    @Test
    void rejectsInvalidPasswordWithoutCreatingSession() {
        User user = new User();
        user.setEmail("alex@example.com");
        user.setPassword(passwordEncoder.encode("correct-password"));
        when(userRepository.findByEmailIgnoreCase("alex@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("alex@example.com", "wrong-password")));
    }

    @Test
    void authenticatesUsingOnlyTheStoredTokenHash() {
        when(userRepository.existsByEmailIgnoreCase("alex@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });
        AuthResponse response = authService.register(new RegisterRequest(
                "Alex", "alex@example.com", "strong-password"));

        ArgumentCaptor<AuthSession> sessionCaptor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionRepository).save(sessionCaptor.capture());
        AuthSession session = sessionCaptor.getValue();
        when(authSessionRepository.findActiveSession(
                org.mockito.ArgumentMatchers.eq(session.getTokenHash()), any()))
                .thenReturn(Optional.of(session));

        assertEquals(7L, authService.authenticate(response.accessToken()).orElseThrow().getId());
    }
}
