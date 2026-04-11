package com.rentalapp.module.profiles.dto;

import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.profiles.entity.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProfileResponse {
    private final String userId;
    private final String fullName;
    private final String phoneNumber;
    private final String bio;
    private final String profilePhotoUrl;
    private final String city;
    private final List<String> serviceAreas;
    private final String companyName;
    private final String feeStructure;
    private final VerificationStatus verificationStatus;
    private final Role role;
    private final String email;
}
