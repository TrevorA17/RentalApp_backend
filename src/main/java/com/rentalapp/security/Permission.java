package com.rentalapp.security;

import com.rentalapp.module.auth.entity.Role;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum Permission {
    LISTING_VIEW,
    LISTING_CREATE,
    LISTING_UPDATE_OWN,
    LISTING_PUBLISH_OWN,
    LISTING_VIEW_OWN,
    LISTING_UPLOAD_MEDIA,
    LISTING_MODERATE,

    SAVED_LISTING_MANAGE,

    INQUIRY_SEND,
    INQUIRY_VIEW_OWN,
    INQUIRY_UPDATE_STATUS,

    SUGGESTION_VIEW,

    PROFILE_VIEW_OWN,
    PROFILE_UPDATE_OWN,

    RECOMMENDATION_SUBMIT,
    RECOMMENDATION_MODERATE,

    REPORT_SUBMIT,
    REPORT_MODERATE,

    USER_MODERATE,
    MODERATION_AUDIT_VIEW,

    AI_ASSIST_USE;

    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = Map.of(
            Role.RENTER, EnumSet.of(
                    LISTING_VIEW,
                    SAVED_LISTING_MANAGE,
                    INQUIRY_SEND, INQUIRY_VIEW_OWN,
                    SUGGESTION_VIEW,
                    PROFILE_VIEW_OWN, PROFILE_UPDATE_OWN,
                    RECOMMENDATION_SUBMIT,
                    REPORT_SUBMIT,
                    AI_ASSIST_USE
            ),
            Role.AGENT, EnumSet.of(
                    LISTING_VIEW, LISTING_CREATE, LISTING_UPDATE_OWN, LISTING_PUBLISH_OWN,
                    LISTING_VIEW_OWN, LISTING_UPLOAD_MEDIA,
                    SAVED_LISTING_MANAGE,
                    INQUIRY_SEND, INQUIRY_VIEW_OWN, INQUIRY_UPDATE_STATUS,
                    SUGGESTION_VIEW,
                    PROFILE_VIEW_OWN, PROFILE_UPDATE_OWN,
                    RECOMMENDATION_SUBMIT,
                    REPORT_SUBMIT,
                    AI_ASSIST_USE
            ),
            Role.LANDLORD, EnumSet.of(
                    LISTING_VIEW, LISTING_CREATE, LISTING_UPDATE_OWN, LISTING_PUBLISH_OWN,
                    LISTING_VIEW_OWN, LISTING_UPLOAD_MEDIA,
                    SAVED_LISTING_MANAGE,
                    INQUIRY_SEND, INQUIRY_VIEW_OWN, INQUIRY_UPDATE_STATUS,
                    SUGGESTION_VIEW,
                    PROFILE_VIEW_OWN, PROFILE_UPDATE_OWN,
                    RECOMMENDATION_SUBMIT,
                    REPORT_SUBMIT,
                    AI_ASSIST_USE
            ),
            Role.ADMIN, EnumSet.of(
                    LISTING_VIEW, LISTING_MODERATE,
                    PROFILE_VIEW_OWN, PROFILE_UPDATE_OWN,
                    RECOMMENDATION_MODERATE,
                    REPORT_SUBMIT, REPORT_MODERATE,
                    USER_MODERATE, MODERATION_AUDIT_VIEW,
                    AI_ASSIST_USE
            )
    );

    public static Set<Permission> forRole(Role role) {
        Set<Permission> permissions = ROLE_PERMISSIONS.get(role);
        return permissions == null ? Set.of() : permissions;
    }
}
