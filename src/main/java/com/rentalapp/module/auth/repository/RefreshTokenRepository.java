package com.rentalapp.module.auth.repository;

import com.rentalapp.module.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String>, IRefreshTokenRepository {
}
