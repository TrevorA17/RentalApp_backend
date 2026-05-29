package com.rentalapp.module.auth.repository;

import com.rentalapp.module.auth.entity.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IRefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByTokenId(String tokenId);
    List<RefreshToken> findAllByUserIdAndRevokedAtIsNullAndExpiresAtAfter(String userId, Instant now);
}
