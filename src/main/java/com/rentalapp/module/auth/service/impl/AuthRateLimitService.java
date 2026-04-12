package com.rentalapp.module.auth.service.impl;

import com.rentalapp.config.AppSecurityProperties;
import com.rentalapp.module.auth.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthRateLimitService {
    private final RateLimitService rateLimitService;
    private final AppSecurityProperties appSecurityProperties;

    public AuthRateLimitService(RateLimitService rateLimitService, AppSecurityProperties appSecurityProperties) {
        this.rateLimitService = rateLimitService;
        this.appSecurityProperties = appSecurityProperties;
    }

    public void enforceAuthWindow(String bucket, HttpServletRequest request) {
        rateLimitService.checkRateLimit(bucket, resolveClientKey(request), appSecurityProperties.rateLimit().authRequestsPerWindow());
    }

    public void enforceSensitiveActionWindow(String bucket, HttpServletRequest request) {
        rateLimitService.checkRateLimit(bucket, resolveClientKey(request), appSecurityProperties.rateLimit().sensitiveActionRequestsPerWindow());
    }

    public String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
