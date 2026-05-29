package com.rentalapp.module.profiles.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.profiles.dto.ProfileResponse;
import com.rentalapp.module.profiles.dto.ProfileUpsertRequest;
import com.rentalapp.module.profiles.service.IProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final IProfileService profileService;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PROFILE_VIEW_OWN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getMyProfile()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('PROFILE_UPDATE_OWN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> upsertMyProfile(@Valid @RequestBody ProfileUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile saved successfully", profileService.upsertMyProfile(request)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getPublicProfile(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getPublicProfile(userId)));
    }
}
