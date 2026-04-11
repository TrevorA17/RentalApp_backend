package com.rentalapp.module.recommendations.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.recommendations.dto.RecommendationResponse;
import com.rentalapp.module.recommendations.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/api/v1/recommendations")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getRecommendations(
            @RequestParam(defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.getRecommendations(limit)));
    }
}
