package com.rentalapp.module.auth.service;

public interface IRateLimitService {
    void checkRateLimit(String bucketName, String clientKey, int maxRequests);
}
