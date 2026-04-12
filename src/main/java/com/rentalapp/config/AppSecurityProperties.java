package com.rentalapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        RateLimitProperties rateLimit,
        LoginAbuseProtectionProperties loginAbuseProtection,
        PasswordResetProperties passwordReset
) {
    public record RateLimitProperties(
            int authRequestsPerWindow,
            int sensitiveActionRequestsPerWindow,
            Duration window
    ) {
    }

    public record LoginAbuseProtectionProperties(
            int maxFailures,
            Duration lockoutDuration
    ) {
    }

    public record PasswordResetProperties(
            Duration tokenTtl,
            List<String> devExposeProfiles
    ) {
    }
}
