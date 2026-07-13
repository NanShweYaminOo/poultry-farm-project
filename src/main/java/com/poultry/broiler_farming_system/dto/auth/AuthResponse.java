package com.poultry.broiler_farming_system.dto.auth;

import com.poultry.broiler_farming_system.entity.enums.AccountType;
import com.poultry.broiler_farming_system.entity.enums.UserRole;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        Long userId,
        String username,
        UserRole role,
        AccountType accountType
) {
}
