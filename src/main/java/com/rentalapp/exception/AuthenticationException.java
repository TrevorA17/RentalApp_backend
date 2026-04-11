package com.rentalapp.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApiException {
    public AuthenticationException(String code, String message) {
        super(HttpStatus.UNAUTHORIZED, code, message);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("AUTH_INVALID_CREDENTIALS", "Invalid email or password.");
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("AUTH_INVALID_TOKEN", "Authentication token is invalid or expired.");
    }
}
