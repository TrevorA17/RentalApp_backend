package com.rentalapp.module.auth.service;

import com.rentalapp.config.AppSecurityProperties;
import com.rentalapp.exception.AuthenticationException;
import com.rentalapp.exception.ValidationException;
import com.rentalapp.module.auth.dto.AuthResponse;
import com.rentalapp.module.auth.dto.LoginRequest;
import com.rentalapp.module.auth.dto.LogoutRequest;
import com.rentalapp.module.auth.dto.PasswordResetConfirmRequest;
import com.rentalapp.module.auth.dto.PasswordResetRequestRequest;
import com.rentalapp.module.auth.dto.PasswordResetRequestResponse;
import com.rentalapp.module.auth.dto.RefreshTokenRequest;
import com.rentalapp.module.auth.dto.RegisterRequest;
import com.rentalapp.module.auth.entity.PasswordResetToken;
import com.rentalapp.module.auth.entity.RefreshToken;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.entity.UserStatus;
import com.rentalapp.module.auth.repository.IPasswordResetTokenRepository;
import com.rentalapp.module.auth.repository.IRefreshTokenRepository;
import com.rentalapp.module.auth.repository.IUserRepository;
import com.rentalapp.module.auth.service.impl.AuthService;
import com.rentalapp.module.auth.service.impl.InMemoryLoginAttemptService;
import com.rentalapp.security.JwtProperties;
import com.rentalapp.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRefreshTokenRepository refreshTokenRepository;

    @Mock
    private IPasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment environment;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRLZXlGb3JSZW50YWxBcHAxMjM0NTY=",
                3600000,
                604800000
        );
        AppSecurityProperties securityProperties = new AppSecurityProperties(
                new AppSecurityProperties.RateLimitProperties(10, 5, Duration.ofMinutes(15)),
                new AppSecurityProperties.LoginAbuseProtectionProperties(5, Duration.ofMinutes(15)),
                new AppSecurityProperties.PasswordResetProperties(Duration.ofMinutes(30), List.of("local"))
        );

        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                new JwtTokenProvider(properties),
                new InMemoryLoginAttemptService(securityProperties),
                securityProperties,
                environment
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

        AuthResponse response = authService.login(request, "127.0.0.1");

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

        assertThrows(AuthenticationException.class, () -> authService.login(request, "127.0.0.1"));
    }

    @Test
    void requestPasswordResetReturnsDevTokenInLocalProfile() {
        User user = buildUser();
        PasswordResetRequestRequest request = new PasswordResetRequestRequest();
        request.setEmail("agent@example.com");

        when(userRepository.findByEmail("agent@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findAllByUserIdAndConsumedAtIsNullAndExpiresAtAfter(anyString(), any())).thenReturn(List.of());
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(environment.getActiveProfiles()).thenReturn(new String[] {"local"});

        PasswordResetRequestResponse response = authService.requestPasswordReset(request);

        assertEquals(true, response.isDevModeTokenExposed());
        assertNotNull(response.getResetToken());
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void confirmPasswordResetUpdatesPasswordAndConsumesToken() {
        User user = buildUser();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken("reset-token");
        token.setExpiresAt(Instant.now().plusSeconds(1800));

        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken("reset-token");
        request.setNewPassword("NewPassword123");

        when(passwordResetTokenRepository.findByToken("reset-token")).thenReturn(Optional.of(token));
        when(passwordResetTokenRepository.findAllByUserIdAndConsumedAtIsNullAndExpiresAtAfter(anyString(), any())).thenReturn(List.of());
        when(passwordEncoder.encode("NewPassword123")).thenReturn("encoded-password");

        authService.confirmPasswordReset(request);

        assertEquals("encoded-password", user.getPasswordHash());
        assertNotNull(token.getConsumedAt());
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository, atLeastOnce()).save(any(PasswordResetToken.class));
    }

    @Test
    void repeatedFailedLoginsTriggerTemporaryBlock() {
        User user = buildUser();
        LoginRequest request = new LoginRequest();
        request.setEmail("agent@example.com");
        request.setPassword("WrongPassword123");

        when(userRepository.findByEmail("agent@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        for (int index = 0; index < 5; index++) {
            assertThrows(AuthenticationException.class, () -> authService.login(request, "127.0.0.1"));
        }

        AuthenticationException blocked = assertThrows(AuthenticationException.class, () -> authService.login(request, "127.0.0.1"));
        assertEquals("AUTH_LOGIN_TEMPORARILY_BLOCKED", blocked.getCode());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
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
