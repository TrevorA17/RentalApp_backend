package com.rentalapp.module.admin.service;

import com.rentalapp.module.admin.dto.AdminListingResponse;
import com.rentalapp.module.admin.dto.AdminUserResponse;
import com.rentalapp.module.admin.dto.UpdateListingApprovalRequest;
import com.rentalapp.module.admin.dto.UpdateUserStatusRequest;

import java.util.List;

public interface AdminModerationService {
    List<AdminListingResponse> getListingsForModeration();
    AdminListingResponse updateListingApproval(String listingId, UpdateListingApprovalRequest request);
    List<AdminUserResponse> getUsers();
    AdminUserResponse updateUserStatus(String userId, UpdateUserStatusRequest request);
}
