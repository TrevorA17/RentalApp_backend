package com.rentalapp.module.profiles.service.impl;

import com.rentalapp.exception.ResourceNotFoundException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.profiles.dto.ProfileResponse;
import com.rentalapp.module.profiles.dto.ProfileUpsertRequest;
import com.rentalapp.module.profiles.entity.Profile;
import com.rentalapp.module.profiles.repository.ProfileRepository;
import com.rentalapp.module.profiles.service.ProfileService;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile() {
        String userId = SecurityUtils.requireCurrentUserId();
        User user = findUser(userId);
        return profileRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> toEmptyResponse(user));
    }

    @Override
    @Transactional
    public ProfileResponse upsertMyProfile(ProfileUpsertRequest request) {
        String userId = SecurityUtils.requireCurrentUserId();
        User user = findUser(userId);

        Profile profile = profileRepository.findByUserId(userId).orElseGet(Profile::new);
        profile.setUser(user);
        profile.setFullName(request.getFullName().trim());
        profile.setPhoneNumber(normalize(request.getPhoneNumber()));
        profile.setBio(normalize(request.getBio()));
        profile.setProfilePhotoUrl(normalize(request.getProfilePhotoUrl()));
        profile.setCity(normalize(request.getCity()));
        profile.setServiceAreas(stringifyList(request.getServiceAreas()));

        if (user.getRole() == Role.AGENT) {
            profile.setCompanyName(normalize(request.getCompanyName()));
            profile.setFeeStructure(normalize(request.getFeeStructure()));
        } else {
            profile.setCompanyName(null);
            profile.setFeeStructure(null);
        }

        return toResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getPublicProfile(String userId) {
        User user = findUser(userId);
        return profileRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> toEmptyResponse(user));
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String stringifyList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        return values.stream()
                .map(this::normalize)
                .filter(value -> value != null)
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElse(null);
    }

    private List<String> toList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private ProfileResponse toResponse(Profile profile) {
        User user = profile.getUser();

        return ProfileResponse.builder()
                .userId(user.getId())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .bio(profile.getBio())
                .profilePhotoUrl(profile.getProfilePhotoUrl())
                .city(profile.getCity())
                .serviceAreas(toList(profile.getServiceAreas()))
                .companyName(profile.getCompanyName())
                .feeStructure(profile.getFeeStructure())
                .verificationStatus(profile.getVerificationStatus())
                .role(user.getRole())
                .email(user.getEmail())
                .build();
    }

    private ProfileResponse toEmptyResponse(User user) {
        return ProfileResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .serviceAreas(List.of())
                .verificationStatus(com.rentalapp.module.profiles.entity.VerificationStatus.UNVERIFIED)
                .role(user.getRole())
                .email(user.getEmail())
                .build();
    }
}
