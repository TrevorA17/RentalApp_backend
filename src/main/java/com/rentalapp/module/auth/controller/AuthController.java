package com.rentalapp.module.auth.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.auth.dto.AuthResponse;
import com.rentalapp.module.auth.dto.AuthUserResponse;
import com.rentalapp.module.auth.dto.LoginRequest;
import com.rentalapp.module.auth.dto.LogoutRequest;
import com.rentalapp.module.auth.dto.PasswordResetConfirmRequest;
import com.rentalapp.module.auth.dto.PasswordResetRequestRequest;
import com.rentalapp.module.auth.dto.PasswordResetRequestResponse;
import com.rentalapp.module.auth.dto.RefreshTokenRequest;
import com.rentalapp.module.auth.dto.RegisterRequest;
import com.rentalapp.module.auth.service.impl.AuthRateLimitService;
import com.rentalapp.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthRateLimitService authRateLimitService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        authRateLimitService.enforceAuthWindow("auth:register", httpRequest);
        AuthUserResponse user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Account created", Map.of("userId", user.getId(), "role", user.getRole())));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        authRateLimitService.enforceAuthWindow("auth:login", httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(request, authRateLimitService.resolveClientKey(httpRequest))));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        authRateLimitService.enforceAuthWindow("auth:refresh", httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Session refreshed", authService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.ok("Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.ok(authService.me()));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<PasswordResetRequestResponse>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestRequest request,
            HttpServletRequest httpRequest
    ) {
        authRateLimitService.enforceAuthWindow("auth:password-reset-request", httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(authService.requestPasswordReset(request)));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request,
            HttpServletRequest httpRequest
    ) {
        authRateLimitService.enforceSensitiveActionWindow("auth:password-reset-confirm", httpRequest);
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful", null));
    }
}
