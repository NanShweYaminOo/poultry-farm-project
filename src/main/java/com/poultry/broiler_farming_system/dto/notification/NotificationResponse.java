package com.poultry.broiler_farming_system.dto.notification;

import com.poultry.broiler_farming_system.entity.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String titleEn,
        String titleMy,
        String messageEn,
        String messageMy,
        Boolean isRead,
        String relatedEntityType,
        Long relatedEntityId,
        LocalDateTime createdAt
) {
}
