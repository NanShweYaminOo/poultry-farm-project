package com.poultry.broiler_farming_system.dto.groupchat;

import java.time.LocalDateTime;

public record AdminGroupChatSummaryResponse(
        Long id,
        String groupName,
        Long createdById,
        String createdByUsername,
        LocalDateTime createdDate,
        long memberCount,
        long messageCount,
        String lastMessagePreview,
        LocalDateTime lastMessageAt
) {
}
