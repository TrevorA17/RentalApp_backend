package com.rentalapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.media")
public record AppMediaProperties(
        String storagePath,
        String publicBaseUrl,
        long maxFileSizeBytes,
        List<String> allowedContentTypes
) {
}
