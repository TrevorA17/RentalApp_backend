package com.rentalapp.module.auth.service;

public interface RateLimitService {
    void checkRateLimit(String bucketName, String clientKey, int maxRequests);
}
