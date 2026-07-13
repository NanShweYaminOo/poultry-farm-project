package com.poultry.broiler_farming_system.dto.groupchat;

public record AdminGroupChatMemberResponse(
        Long userId,
        String username,
        String fullName
) {
}
