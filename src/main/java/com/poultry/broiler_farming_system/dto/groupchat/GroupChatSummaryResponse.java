package com.poultry.broiler_farming_system.dto.groupchat;

import java.time.LocalDateTime;

// Farmer/Admin-facing "my groups" list -- trimmed down from
// AdminGroupChatSummaryResponse (no createdBy fields; a member doesn't need
// audit info, just enough to render a group list/sidebar).
public record GroupChatSummaryResponse(
        Long id,
        String groupName,
        long memberCount,
        String lastMessagePreview,
        LocalDateTime lastMessageAt
) {
}
