package com.rentalapp.module.auth.service;

import com.rentalapp.module.auth.dto.AuthResponse;
import com.rentalapp.module.auth.dto.AuthUserResponse;
import com.rentalapp.module.auth.dto.LoginRequest;
import com.rentalapp.module.auth.dto.RegisterRequest;

public interface AuthService {
    AuthUserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthUserResponse me();
}
