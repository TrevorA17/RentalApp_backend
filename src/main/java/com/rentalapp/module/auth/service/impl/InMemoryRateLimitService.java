package com.rentalapp.module.auth.service.impl;

import com.rentalapp.config.AppSecurityProperties;
import com.rentalapp.exception.RateLimitExceededException;
import com.rentalapp.module.auth.service.RateLimitService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRateLimitService implements RateLimitService {
    private final AppSecurityProperties appSecurityProperties;
    private final Map<String, CounterWindow> windows = new ConcurrentHashMap<>();

    public InMemoryRateLimitService(AppSecurityProperties appSecurityProperties) {
        this.appSecurityProperties = appSecurityProperties;
    }

    @Override
    public void checkRateLimit(String bucketName, String clientKey, int maxRequests) {
        Instant now = Instant.now();
        String key = bucketName + ":" + (clientKey == null || clientKey.isBlank() ? "unknown" : clientKey);
        CounterWindow window = windows.compute(key, (ignored, current) -> {
            if (current == null || current.expiresAt.isBefore(now)) {
                CounterWindow reset = new CounterWindow();
                reset.count = 1;
                reset.expiresAt = now.plus(appSecurityProperties.rateLimit().window());
                return reset;
            }

            current.count++;
            return current;
        });

        if (window.count > maxRequests) {
            throw new RateLimitExceededException("Too many requests. Please slow down and try again shortly.");
        }
    }

    private static class CounterWindow {
        private int count;
        private Instant expiresAt;
    }
}
