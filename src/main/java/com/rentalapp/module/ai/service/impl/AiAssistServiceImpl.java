package com.rentalapp.module.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionRequest;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionResponse;
import com.rentalapp.module.ai.dto.InterpretListingSearchRequest;
import com.rentalapp.module.ai.dto.InterpretListingSearchResponse;
import com.rentalapp.module.ai.entity.AiRequestLog;
import com.rentalapp.module.ai.repository.AiRequestLogRepository;
import com.rentalapp.module.ai.service.AiAssistService;
import com.rentalapp.module.ai.service.AiAssistProvider;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.listings.repository.AmenityRepository;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiAssistServiceImpl implements AiAssistService {
    private static final String LISTING_DESCRIPTION_USE_CASE = "LISTING_DESCRIPTION_ENHANCE";
    private static final String SEARCH_INTERPRET_USE_CASE = "LISTING_SEARCH_INTERPRET";

    private final AiRequestLogRepository aiRequestLogRepository;
    private final UserRepository userRepository;
    private final AmenityRepository amenityRepository;
    private final AiAssistProvider aiAssistProvider;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public EnhanceListingDescriptionResponse enhanceListingDescription(EnhanceListingDescriptionRequest request) {
        String userId = SecurityUtils.requireCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        AiRequestLog log = new AiRequestLog();
        log.setUser(user);
        log.setUseCase(LISTING_DESCRIPTION_USE_CASE);
        log.setRequestPayload(toJson(request));

        try {
            EnhanceListingDescriptionResponse response = aiAssistProvider.enhanceListingDescription(request);
            log.setStatus("SUCCESS");
            log.setResponsePayload(toJson(response));
            aiRequestLogRepository.save(log);
            return response;
        } catch (RuntimeException exception) {
            log.setStatus("FAILED");
            log.setErrorMessage(exception.getMessage());
            aiRequestLogRepository.save(log);
            throw exception;
        }
    }

    @Override
    @Transactional
    public InterpretListingSearchResponse interpretListingSearch(InterpretListingSearchRequest request) {
        User user = loadCurrentUser();
        AiRequestLog log = user == null ? null : buildLog(user, SEARCH_INTERPRET_USE_CASE, request);

        try {
            InterpretListingSearchResponse response = aiAssistProvider.interpretListingSearch(request, amenityRepository.findAll());
            saveSuccess(log, response);
            return response;
        } catch (RuntimeException exception) {
            saveFailure(log, exception);
            throw exception;
        }
    }

    private User loadCurrentUser() {
        String userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId).orElse(null);
    }

    private AiRequestLog buildLog(User user, String useCase, Object request) {
        AiRequestLog log = new AiRequestLog();
        log.setUser(user);
        log.setUseCase(useCase);
        log.setRequestPayload(toJson(request));
        return log;
    }

    private void saveSuccess(AiRequestLog log, Object response) {
        if (log == null) {
            return;
        }

        log.setStatus("SUCCESS");
        log.setResponsePayload(toJson(response));
        aiRequestLogRepository.save(log);
    }

    private void saveFailure(AiRequestLog log, RuntimeException exception) {
        if (log == null) {
            return;
        }

        log.setStatus("FAILED");
        log.setErrorMessage(exception.getMessage());
        aiRequestLogRepository.save(log);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize AI request log payload.", exception);
        }
    }
}
