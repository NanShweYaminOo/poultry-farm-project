package com.poultry.broiler_farming_system.dto.groupchat;

import java.time.LocalDateTime;
import java.util.List;

public record AdminGroupChatDetailResponse(
        Long id,
        String groupName,
        LocalDateTime createdDate,
        List<AdminGroupChatMemberResponse> members,
        List<GroupChatMessageResponse> messages
) {
}
