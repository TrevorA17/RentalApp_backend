package com.rentalapp.module.auth.service;

public interface LoginAttemptService {
    void ensureLoginAllowed(String email, String clientKey);
    void recordFailedLogin(String email, String clientKey);
    void recordSuccessfulLogin(String email, String clientKey);
}
