package com.rentalapp.module.auth.repository;

import com.rentalapp.module.auth.entity.PasswordResetToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IPasswordResetTokenRepository {
    PasswordResetToken save(PasswordResetToken token);
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findAllByUserIdAndConsumedAtIsNullAndExpiresAtAfter(String userId, Instant now);
}
