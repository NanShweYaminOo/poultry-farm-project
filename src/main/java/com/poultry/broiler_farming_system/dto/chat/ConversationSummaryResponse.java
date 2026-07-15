package com.poultry.broiler_farming_system.dto.chat;

import java.time.LocalDateTime;

public record ConversationSummaryResponse(
        Long otherUserId,
        String otherUsername,
        String otherFullName,
        String lastMessagePreview,
        LocalDateTime lastMessageAt,
        long unreadCount
) {
}
