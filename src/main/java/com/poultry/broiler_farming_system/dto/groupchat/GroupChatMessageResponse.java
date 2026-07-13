package com.poultry.broiler_farming_system.dto.groupchat;

import java.time.LocalDateTime;

public record GroupChatMessageResponse(
        Long id,
        Long groupChatId,
        Long senderId,
        String senderUsername,
        String content,
        LocalDateTime sentAt
) {
}
