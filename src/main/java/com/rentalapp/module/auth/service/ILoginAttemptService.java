package com.rentalapp.module.auth.service;

public interface ILoginAttemptService {
    void ensureLoginAllowed(String email, String clientKey);
    void recordFailedLogin(String email, String clientKey);
    void recordSuccessfulLogin(String email, String clientKey);
}
