package com.rentalapp.module.auth.service.impl;

import com.rentalapp.exception.AuthenticationException;
import com.rentalapp.exception.ValidationException;
import com.rentalapp.module.auth.dto.AuthResponse;
import com.rentalapp.module.auth.dto.AuthUserResponse;
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
import com.rentalapp.module.auth.repository.PasswordResetTokenRepository;
import com.rentalapp.module.auth.repository.RefreshTokenRepository;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.auth.service.AuthService;
import com.rentalapp.module.auth.service.LoginAttemptService;
import com.rentalapp.config.AppSecurityProperties;
import com.rentalapp.security.JwtTokenProvider;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final AppSecurityProperties appSecurityProperties;
    private final Environment environment;

    @Override
    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new ValidationException("Admin accounts cannot be registered publicly.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        validatePasswordStrength(request.getPassword());
        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            throw new ValidationException("An account with this email already exists.");
        });

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setFullName(request.getFullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        return toAuthUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, String clientKey) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        loginAttemptService.ensureLoginAllowed(normalizedEmail, clientKey);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> invalidCredentials(normalizedEmail, clientKey));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AuthenticationException("AUTH_ACCOUNT_SUSPENDED", "This account has been suspended.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw invalidCredentials(normalizedEmail, clientKey);
        }

        loginAttemptService.recordSuccessfulLogin(normalizedEmail, clientKey);
        revokeActiveRefreshTokens(user.getId());
        return issueSession(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        var principal = jwtTokenProvider.parseToken(request.getRefreshToken());

        if (!"REFRESH".equals(principal.getTokenType()) || principal.getTokenId() == null) {
            throw AuthenticationException.invalidToken();
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenId(principal.getTokenId())
                .orElseThrow(AuthenticationException::invalidToken);

        Instant now = Instant.now();
        if (!refreshToken.isActiveAt(now)) {
            throw AuthenticationException.invalidToken();
        }

        User user = refreshToken.getUser();
        if (user.getStatus() == UserStatus.SUSPENDED) {
            refreshToken.setRevokedAt(now);
            refreshTokenRepository.save(refreshToken);
            throw new AuthenticationException("AUTH_ACCOUNT_SUSPENDED", "This account has been suspended.");
        }

        refreshToken.setRevokedAt(now);
        refreshTokenRepository.save(refreshToken);

        return issueSession(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        var principal = jwtTokenProvider.parseToken(request.getRefreshToken());

        if (!"REFRESH".equals(principal.getTokenType()) || principal.getTokenId() == null) {
            throw AuthenticationException.invalidToken();
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenId(principal.getTokenId())
                .orElseThrow(AuthenticationException::invalidToken);

        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUserResponse me() {
        String userId = SecurityUtils.requireCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(AuthenticationException::invalidToken);

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AuthenticationException("AUTH_ACCOUNT_SUSPENDED", "This account has been suspended.");
        }

        return toAuthUserResponse(user);
    }

    @Override
    @Transactional
    public PasswordResetRequestResponse requestPasswordReset(PasswordResetRequestRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String tokenValue = null;
        Instant expiresAt = null;

        var userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            revokeActivePasswordResetTokens(user.getId());

            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setToken(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
            token.setExpiresAt(Instant.now().plus(appSecurityProperties.passwordReset().tokenTtl()));
            PasswordResetToken savedToken = passwordResetTokenRepository.save(token);

            if (shouldExposeResetToken()) {
                tokenValue = savedToken.getToken();
                expiresAt = savedToken.getExpiresAt();
            }

            log.info("Password reset requested for user {}", user.getEmail());
            if (tokenValue != null) {
                log.info("Dev-mode password reset token for {}: {}", user.getEmail(), tokenValue);
            }
        }

        return PasswordResetRequestResponse.builder()
                .message("If an account exists for that email, a password reset token has been issued.")
                .resetToken(tokenValue)
                .expiresAt(expiresAt)
                .devModeTokenExposed(tokenValue != null)
                .build();
    }

    @Override
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        validatePasswordStrength(request.getNewPassword());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ValidationException("Password reset token is invalid or expired."));

        Instant now = Instant.now();
        if (!resetToken.isActiveAt(now)) {
            throw new ValidationException("Password reset token is invalid or expired.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setConsumedAt(now);
        passwordResetTokenRepository.save(resetToken);
        revokeActiveRefreshTokens(user.getId());
        revokeActivePasswordResetTokens(user.getId());
    }

    private AuthResponse issueSession(User user) {
        String refreshTokenId = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenId(refreshTokenId);
        refreshToken.setExpiresAt(Instant.now().plusMillis(604800000));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user, refreshTokenId))
                .user(toAuthUserResponse(user))
                .build();
    }

    private void revokeActiveRefreshTokens(String userId) {
        Instant now = Instant.now();
        refreshTokenRepository.findAllByUserIdAndRevokedAtIsNullAndExpiresAtAfter(userId, now)
                .forEach(token -> token.setRevokedAt(now));
    }

    private void revokeActivePasswordResetTokens(String userId) {
        Instant now = Instant.now();
        passwordResetTokenRepository.findAllByUserIdAndConsumedAtIsNullAndExpiresAtAfter(userId, now)
                .forEach(token -> token.setConsumedAt(now));
    }

    private void validatePasswordStrength(String password) {
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUppercase || !hasLowercase || !hasDigit) {
            throw new ValidationException("Password must include at least one uppercase letter, one lowercase letter, and one number.");
        }
    }

    private AuthUserResponse toAuthUserResponse(User user) {
        return AuthUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    private AuthenticationException invalidCredentials(String email, String clientKey) {
        loginAttemptService.recordFailedLogin(email, clientKey);
        return AuthenticationException.invalidCredentials();
    }

    private boolean shouldExposeResetToken() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> appSecurityProperties.passwordReset().devExposeProfiles().contains(profile));
    }
}
