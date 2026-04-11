package com.rentalapp.module.auth.repository;

import com.rentalapp.module.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenId(String tokenId);
    List<RefreshToken> findAllByUserIdAndRevokedAtIsNullAndExpiresAtAfter(String userId, Instant now);
}
