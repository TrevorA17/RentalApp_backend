package com.rentalapp.module.admin.controller;

import com.rentalapp.common.api.ApiResponse;
import com.rentalapp.module.admin.dto.AdminListingResponse;
import com.rentalapp.module.admin.dto.AdminModerationActionResponse;
import com.rentalapp.module.admin.dto.AdminUserResponse;
import com.rentalapp.module.admin.dto.UpdateListingApprovalRequest;
import com.rentalapp.module.admin.dto.UpdateUserStatusRequest;
import com.rentalapp.module.admin.service.IAdminModerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminModerationController {
    private final IAdminModerationService adminModerationService;

    @GetMapping("/api/v1/admin/listings")
    @PreAuthorize("hasAuthority('LISTING_MODERATE')")
    public ResponseEntity<ApiResponse<List<AdminListingResponse>>> getListingsForModeration() {
        return ResponseEntity.ok(ApiResponse.ok(adminModerationService.getListingsForModeration()));
    }

    @PatchMapping("/api/v1/admin/listings/{listingId}/approval")
    @PreAuthorize("hasAuthority('LISTING_MODERATE')")
    public ResponseEntity<ApiResponse<AdminListingResponse>> updateListingApproval(
            @PathVariable String listingId,
            @Valid @RequestBody UpdateListingApprovalRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Listing moderation updated.", adminModerationService.updateListingApproval(listingId, request)));
    }

    @GetMapping("/api/v1/admin/users")
    @PreAuthorize("hasAuthority('USER_MODERATE')")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminModerationService.getUsers()));
    }

    @GetMapping("/api/v1/admin/moderation-actions")
    @PreAuthorize("hasAuthority('MODERATION_AUDIT_VIEW')")
    public ResponseEntity<ApiResponse<List<AdminModerationActionResponse>>> getRecentModerationActions(
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminModerationService.getRecentModerationActions(limit)));
    }

    @PatchMapping("/api/v1/admin/users/{userId}/status")
    @PreAuthorize("hasAuthority('USER_MODERATE')")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("User status updated.", adminModerationService.updateUserStatus(userId, request)));
    }
}
