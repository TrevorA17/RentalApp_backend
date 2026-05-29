package com.rentalapp.module.auth.repository;

import com.rentalapp.module.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String>, IPasswordResetTokenRepository {
}
