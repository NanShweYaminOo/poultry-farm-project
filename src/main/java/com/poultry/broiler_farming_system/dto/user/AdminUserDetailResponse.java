package com.poultry.broiler_farming_system.dto.user;

import com.poultry.broiler_farming_system.entity.enums.AccountType;
import com.poultry.broiler_farming_system.entity.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserDetailResponse(
        Long id,
        String fullName,
        String username,
        String email,
        String phoneNumber,
        String location,
        String preferredLanguage,
        UserRole role,
        AccountType accountType,
        Boolean isBanned,
        Boolean isFlaggedForReview,
        LocalDateTime postingExtensionExpiry,
        LocalDateTime createdDate,
        long batchCount,
        long salesPostCount,
        long buyRequestCount,
        PaymentHistorySummary paymentHistory,
        List<UserWarningResponse> warnings
) {
    // No monetary amount field exists on PaymentTransaction -- this is a
    // count-by-status summary, not a sum of money.
    public record PaymentHistorySummary(
            long totalCount,
            long pendingCount,
            long approvedCount,
            long rejectedCount
    ) {
    }
}
