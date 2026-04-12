package com.rentalapp.module.profiles.service;

import com.rentalapp.module.profiles.dto.ProfileResponse;
import com.rentalapp.module.profiles.dto.ProfileUpsertRequest;

public interface ProfileService {
    ProfileResponse getMyProfile();
    ProfileResponse upsertMyProfile(ProfileUpsertRequest request);
    ProfileResponse getPublicProfile(String userId);
}
