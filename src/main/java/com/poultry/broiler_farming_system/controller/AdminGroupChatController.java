package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatDetailResponse;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.groupchat.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Admin-only moderation view over the single, system-wide group chat (see
// GroupChatService's Javadoc). Access is restricted to ROLE_ADMIN by the
// existing "/api/v1/admin/**" rule in SecurityConfig.
@RestController
@RequestMapping("/api/v1/admin/group-chats")
@RequiredArgsConstructor
public class AdminGroupChatController {

    private final GroupChatService groupChatService;

    @GetMapping
    public AdminGroupChatDetailResponse getSharedGroup() {
        return groupChatService.getSharedGroupDetail();
    }

    @DeleteMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long messageId,
            @RequestParam(required = false) String reason) {
        groupChatService.deleteMessage(principal.getId(), messageId, reason);
    }
}
