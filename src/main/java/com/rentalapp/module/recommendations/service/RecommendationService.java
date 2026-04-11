package com.rentalapp.module.recommendations.service;

import com.rentalapp.module.recommendations.dto.RecommendationResponse;

import java.util.List;

public interface RecommendationService {
    List<RecommendationResponse> getRecommendations(int limit);
}
