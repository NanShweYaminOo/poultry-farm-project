package com.poultry.broiler_farming_system.dto.accountupgrade;

import com.poultry.broiler_farming_system.entity.enums.RequestStatus;

import java.time.LocalDateTime;

public record AccountUpgradeRequestResponse(
        Long id,
        Long userId,
        String username,
        String reason,
        RequestStatus status,
        LocalDateTime requestedAt,
        Long reviewedByAdminId,
        LocalDateTime reviewedAt,
        String adminNote
) {
}
