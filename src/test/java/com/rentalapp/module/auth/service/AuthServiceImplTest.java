package com.rentalapp.module.auth.service;

import com.rentalapp.exception.AuthenticationException;
import com.rentalapp.exception.ValidationException;
import com.rentalapp.module.auth.dto.AuthResponse;
import com.rentalapp.module.auth.dto.LoginRequest;
import com.rentalapp.module.auth.dto.LogoutRequest;
import com.rentalapp.module.auth.dto.RefreshTokenRequest;
import com.rentalapp.module.auth.dto.RegisterRequest;
import com.rentalapp.module.auth.entity.RefreshToken;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.entity.UserStatus;
import com.rentalapp.module.auth.repository.RefreshTokenRepository;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.auth.service.impl.AuthServiceImpl;
import com.rentalapp.security.JwtProperties;
import com.rentalapp.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRLZXlGb3JSZW50YWxBcHAxMjM0NTY=",
                3600000,
                604800000
        );
        authService = new AuthServiceImpl(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                new JwtTokenProvider(properties)
        );
    }

    @Test
    void registerRejectsWeakPasswords() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setRole(Role.RENTER);

        assertThrows(ValidationException.class, () -> authService.register(request));
    }

    @Test
    void loginIssuesAccessAndRefreshTokens() {
        User user = buildUser();
        LoginRequest request = new LoginRequest();
        request.setEmail("agent@example.com");
        request.setPassword("Password123");

        when(userRepository.findByEmail("agent@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(refreshTokenRepository.findAllByUserIdAndRevokedAtIsNullAndExpiresAtAfter(anyString(), any()))
                .thenReturn(List.of());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.login(request);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshRotatesStoredRefreshToken() {
        User user = buildUser();
        JwtTokenProvider tokenProvider = new JwtTokenProvider(new JwtProperties(
                "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRLZXlGb3JSZW50YWxBcHAxMjM0NTY=",
                3600000,
                604800000
        ));

        String tokenId = "token-1";
        String refreshTokenValue = tokenProvider.generateRefreshToken(user, tokenId);

        RefreshToken stored = new RefreshToken();
        stored.setUser(user);
        stored.setTokenId(tokenId);
        stored.setExpiresAt(Instant.now().plusSeconds(3600));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshTokenValue);

        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.refresh(request);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(refreshTokenRepository, atLeastOnce()).save(any(RefreshToken.class));
    }

    @Test
    void logoutRevokesRefreshToken() {
        User user = buildUser();
        JwtTokenProvider tokenProvider = new JwtTokenProvider(new JwtProperties(
                "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRLZXlGb3JSZW50YWxBcHAxMjM0NTY=",
                3600000,
                604800000
        ));
        String tokenId = "token-logout";
        String refreshTokenValue = tokenProvider.generateRefreshToken(user, tokenId);

        RefreshToken stored = new RefreshToken();
        stored.setUser(user);
        stored.setTokenId(tokenId);
        stored.setExpiresAt(Instant.now().plusSeconds(3600));

        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken(refreshTokenValue);

        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.logout(request);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertNotNull(captor.getValue().getRevokedAt());
    }

    @Test
    void loginRejectsSuspendedAccounts() {
        User user = buildUser();
        user.setStatus(UserStatus.SUSPENDED);

        LoginRequest request = new LoginRequest();
        request.setEmail("agent@example.com");
        request.setPassword("Password123");

        when(userRepository.findByEmail("agent@example.com")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    private User buildUser() {
        User user = new User();
        user.setId("user-1");
        user.setEmail("agent@example.com");
        user.setFullName("Agent User");
        user.setPasswordHash("$2a$10$hash");
        user.setRole(Role.AGENT);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
