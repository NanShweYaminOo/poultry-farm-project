package com.poultry.broiler_farming_system.service.auth;

import com.poultry.broiler_farming_system.dto.auth.AuthResponse;
import com.poultry.broiler_farming_system.dto.auth.LoginRequest;
import com.poultry.broiler_farming_system.dto.auth.RegisterRequest;

public interface AuthService {

    // Always creates a FREE-role user; returns a token immediately (no
    // separate login step needed after registering).
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
