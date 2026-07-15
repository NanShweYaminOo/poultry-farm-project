package com.poultry.broiler_farming_system.dto.systemlog;

import com.poultry.broiler_farming_system.entity.enums.SystemLogAction;

import java.time.LocalDateTime;

public record SystemLogResponse(
        Long id,
        Long adminId,
        String adminUsername,
        SystemLogAction action,
        String targetType,
        Long targetId,
        String description,
        LocalDateTime createdAt
) {
}
