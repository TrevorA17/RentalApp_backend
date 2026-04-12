package com.rentalapp.module.ai.repository;

import com.rentalapp.module.ai.entity.AiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, String> {
}
