package com.rentalapp.module.auth.service.impl;

import com.rentalapp.config.AppSecurityProperties;
import com.rentalapp.exception.AuthenticationException;
import com.rentalapp.module.auth.service.ILoginAttemptService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryLoginAttemptService implements ILoginAttemptService {
    private final AppSecurityProperties appSecurityProperties;
    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public InMemoryLoginAttemptService(AppSecurityProperties appSecurityProperties) {
        this.appSecurityProperties = appSecurityProperties;
    }

    @Override
    public void ensureLoginAllowed(String email, String clientKey) {
        Instant now = Instant.now();
        if (isLocked("email:" + normalize(email), now) || isLocked("client:" + normalize(clientKey), now)) {
            throw new AuthenticationException("AUTH_LOGIN_TEMPORARILY_BLOCKED", "Too many failed login attempts. Try again later.");
        }
    }

    @Override
    public void recordFailedLogin(String email, String clientKey) {
        Instant now = Instant.now();
        registerFailure("email:" + normalize(email), now);
        registerFailure("client:" + normalize(clientKey), now);
    }

    @Override
    public void recordSuccessfulLogin(String email, String clientKey) {
        attempts.remove("email:" + normalize(email));
        attempts.remove("client:" + normalize(clientKey));
    }

    private void registerFailure(String key, Instant now) {
        attempts.compute(key, (ignored, current) -> {
            AttemptState state = current == null || stateExpired(current, now) ? new AttemptState() : current;
            state.failures++;
            if (state.failures >= appSecurityProperties.loginAbuseProtection().maxFailures()) {
                state.lockedUntil = now.plus(appSecurityProperties.loginAbuseProtection().lockoutDuration());
            }
            return state;
        });
    }

    private boolean isLocked(String key, Instant now) {
        AttemptState state = attempts.get(key);
        if (state == null) {
            return false;
        }

        if (stateExpired(state, now)) {
            attempts.remove(key);
            return false;
        }

        return state.lockedUntil != null && state.lockedUntil.isAfter(now);
    }

    private boolean stateExpired(AttemptState state, Instant now) {
        return state.lockedUntil != null && state.lockedUntil.isBefore(now);
    }

    private String normalize(String value) {
        return value == null ? "unknown" : value.trim().toLowerCase();
    }

    private static class AttemptState {
        private int failures;
        private Instant lockedUntil;
    }
}
