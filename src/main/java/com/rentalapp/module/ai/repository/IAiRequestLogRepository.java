package com.rentalapp.module.ai.repository;

import com.rentalapp.module.ai.entity.AiRequestLog;

public interface IAiRequestLogRepository {
    AiRequestLog save(AiRequestLog log);
}
