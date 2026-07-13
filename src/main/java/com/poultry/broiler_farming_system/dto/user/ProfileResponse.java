package com.poultry.broiler_farming_system.dto.user;

import com.poultry.broiler_farming_system.entity.enums.UserRole;

public record ProfileResponse(
        Long id,
        String fullName,
        String username,
        String email,
        String phoneNumber,
        String profileImageUrl,
        UserRole role
) {
}
