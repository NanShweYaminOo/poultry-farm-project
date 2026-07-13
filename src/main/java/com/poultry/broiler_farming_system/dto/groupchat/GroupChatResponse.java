package com.poultry.broiler_farming_system.dto.groupchat;

import java.time.LocalDateTime;

public record GroupChatResponse(Long id, String groupName, Long createdById, LocalDateTime createdDate) {
}
