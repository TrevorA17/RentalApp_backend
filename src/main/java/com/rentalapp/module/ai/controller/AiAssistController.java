package com.rentalapp.module.ai.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionRequest;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionResponse;
import com.rentalapp.module.ai.service.AiAssistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AiAssistController {
    private final AiAssistService aiAssistService;

    @PostMapping("/api/v1/ai/listings/description-enhance")
    public ResponseEntity<ApiResponse<EnhanceListingDescriptionResponse>> enhanceListingDescription(
            @Valid @RequestBody EnhanceListingDescriptionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("AI suggestion generated.", aiAssistService.enhanceListingDescription(request)));
    }
}
