package com.rentalapp.module.auth.dto;

import com.rentalapp.module.auth.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthUserResponse {
    private final String id;
    private final String email;
    private final String fullName;
    private final Role role;
}
