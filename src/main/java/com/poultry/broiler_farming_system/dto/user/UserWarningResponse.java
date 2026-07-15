package com.poultry.broiler_farming_system.dto.user;

import java.time.LocalDateTime;

public record UserWarningResponse(
        Long id,
        Long adminId,
        String adminUsername,
        Long recipientId,
        String recipientUsername,
        String reason,
        LocalDateTime createdAt
) {
}
