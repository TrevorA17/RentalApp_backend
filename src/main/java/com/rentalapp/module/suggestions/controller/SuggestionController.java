package com.rentalapp.module.suggestions.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.suggestions.dto.SuggestedListingResponse;
import com.rentalapp.module.suggestions.service.ISuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SuggestionController {
    private final ISuggestionService suggestionService;

    @GetMapping("/api/v1/suggestions/listings")
    public ResponseEntity<ApiResponse<List<SuggestedListingResponse>>> getSuggestedListings(
            @RequestParam(defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(suggestionService.getSuggestedListings(limit)));
    }
}
