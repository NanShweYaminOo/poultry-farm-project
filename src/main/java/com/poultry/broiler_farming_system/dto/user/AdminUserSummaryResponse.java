package com.poultry.broiler_farming_system.dto.user;

import com.poultry.broiler_farming_system.entity.enums.AccountType;
import com.poultry.broiler_farming_system.entity.enums.UserRole;

import java.time.LocalDateTime;

public record AdminUserSummaryResponse(
        Long id,
        String fullName,
        String username,
        String email,
        String phoneNumber,
        String location,
        UserRole role,
        AccountType accountType,
        Boolean isBanned,
        Boolean isFlaggedForReview,
        LocalDateTime createdDate
) {
}
