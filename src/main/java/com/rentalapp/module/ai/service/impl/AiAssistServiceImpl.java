package com.rentalapp.module.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionRequest;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionResponse;
import com.rentalapp.module.ai.entity.AiRequestLog;
import com.rentalapp.module.ai.repository.AiRequestLogRepository;
import com.rentalapp.module.ai.service.AiAssistService;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AiAssistServiceImpl implements AiAssistService {
    private static final String USE_CASE = "LISTING_DESCRIPTION_ENHANCE";

    private final AiRequestLogRepository aiRequestLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public EnhanceListingDescriptionResponse enhanceListingDescription(EnhanceListingDescriptionRequest request) {
        String userId = SecurityUtils.requireCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        AiRequestLog log = new AiRequestLog();
        log.setUser(user);
        log.setUseCase(USE_CASE);
        log.setRequestPayload(toJson(request));

        try {
            EnhanceListingDescriptionResponse response = buildHeuristicResponse(request);
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

    private EnhanceListingDescriptionResponse buildHeuristicResponse(EnhanceListingDescriptionRequest request) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Confirm exact viewing availability and move-in timeline.");
        suggestions.add("Highlight nearby landmarks, transit access, or shopping options.");

        if (request.getAmenities() == null || request.getAmenities().isEmpty()) {
            suggestions.add("Add amenities to improve listing credibility and search relevance.");
        }

        if (request.getDescription().trim().length() < 120) {
            suggestions.add("Add more detail about space, light, security, and overall condition.");
        }

        String amenitySummary = summarizeAmenities(request.getAmenities());
        String availabilityPhrase = toAvailabilityPhrase(request.getAvailabilityStatus().name());
        String rentPhrase = formatCurrency(request.getRentAmount());

        String enhancedDescription = String.format(
                "%s %d-bedroom %s in %s, %s, %s at %s. %s %s %s",
                startsWithArticle(request.getHouseType().name()) ? "An" : "A",
                request.getBedrooms(),
                request.getHouseType().name().toLowerCase(Locale.ROOT).replace('_', ' '),
                request.getArea().trim(),
                request.getCity().trim(),
                availabilityPhrase,
                rentPhrase,
                request.getFurnished() ? "This furnished option is positioned for renters who want a faster move-in." : "This unfurnished home gives renters room to make the space their own.",
                amenitySummary,
                normalizeSentence(request.getDescription())
        ).trim();

        return EnhanceListingDescriptionResponse.builder()
                .enhancedDescription(enhancedDescription)
                .suggestions(suggestions.stream().distinct().toList())
                .provider("heuristic-fallback")
                .build();
    }

    private String summarizeAmenities(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return "Amenities can be added to strengthen the listing.";
        }

        String joined = amenities.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .limit(4)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");

        return joined.isBlank() ? "Amenities can be added to strengthen the listing." : "Highlights include " + joined + ".";
    }

    private String normalizeSentence(String value) {
        String trimmed = value.trim();
        if (trimmed.endsWith(".")) {
            return trimmed;
        }

        return trimmed + ".";
    }

    private String formatCurrency(BigDecimal value) {
        return "KES " + value.stripTrailingZeros().toPlainString();
    }

    private String toAvailabilityPhrase(String availabilityStatus) {
        return switch (availabilityStatus) {
            case "AVAILABLE_NOW" -> "available now";
            case "AVAILABLE_SOON" -> "available soon";
            case "OCCUPIED" -> "currently occupied";
            default -> availabilityStatus.replace('_', ' ').toLowerCase(Locale.ROOT);
        };
    }

    private boolean startsWithArticle(String houseType) {
        return houseType.startsWith("A");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize AI request log payload.", exception);
        }
    }
}
