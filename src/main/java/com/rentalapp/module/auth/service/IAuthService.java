package com.rentalapp.module.auth.service;

import com.rentalapp.module.auth.dto.AuthResponse;
import com.rentalapp.module.auth.dto.AuthUserResponse;
import com.rentalapp.module.auth.dto.LoginRequest;
import com.rentalapp.module.auth.dto.LogoutRequest;
import com.rentalapp.module.auth.dto.PasswordResetConfirmRequest;
import com.rentalapp.module.auth.dto.PasswordResetRequestRequest;
import com.rentalapp.module.auth.dto.PasswordResetRequestResponse;
import com.rentalapp.module.auth.dto.RefreshTokenRequest;
import com.rentalapp.module.auth.dto.RegisterRequest;

public interface IAuthService {
    AuthUserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, String clientKey);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(LogoutRequest request);
    AuthUserResponse me();
    PasswordResetRequestResponse requestPasswordReset(PasswordResetRequestRequest request);
    void confirmPasswordReset(PasswordResetConfirmRequest request);
}
