package com.poultry.broiler_farming_system.dto.chat;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long senderId,
        String senderUsername,
        Long receiverId,
        String receiverUsername,
        String content,
        LocalDateTime sentAt,
        Boolean isRead
) {
}
