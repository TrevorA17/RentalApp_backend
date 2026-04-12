package com.rentalapp.module.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PasswordResetRequestResponse {
    private final String message;
    private final String resetToken;
    private final Instant expiresAt;
    private final boolean devModeTokenExposed;
}
