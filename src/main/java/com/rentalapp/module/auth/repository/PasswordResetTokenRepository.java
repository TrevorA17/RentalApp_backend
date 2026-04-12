package com.rentalapp.module.auth.repository;

import com.rentalapp.module.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findAllByUserIdAndConsumedAtIsNullAndExpiresAtAfter(String userId, Instant now);
}
