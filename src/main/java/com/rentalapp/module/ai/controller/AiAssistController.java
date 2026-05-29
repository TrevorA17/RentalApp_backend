package com.rentalapp.module.ai.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionRequest;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionResponse;
import com.rentalapp.module.ai.dto.InterpretListingSearchRequest;
import com.rentalapp.module.ai.dto.InterpretListingSearchResponse;
import com.rentalapp.module.ai.service.IAiAssistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AiAssistController {
    private final IAiAssistService aiAssistService;

    @PostMapping("/api/v1/ai/listings/description-enhance")
    public ResponseEntity<ApiResponse<EnhanceListingDescriptionResponse>> enhanceListingDescription(
            @Valid @RequestBody EnhanceListingDescriptionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("AI suggestion generated.", aiAssistService.enhanceListingDescription(request)));
    }

    @PostMapping("/api/v1/ai/search/interpret")
    public ResponseEntity<ApiResponse<InterpretListingSearchResponse>> interpretListingSearch(
            @Valid @RequestBody InterpretListingSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Search request interpreted.", aiAssistService.interpretListingSearch(request)));
    }
}
