package com.rentalapp.module.admin.dto;

import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserResponse {
    private final String id;
    private final String fullName;
    private final String email;
    private final Role role;
    private final UserStatus status;
    private final boolean emailVerified;
}
