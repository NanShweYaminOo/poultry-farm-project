package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatDetailResponse;
import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatSummaryResponse;
import com.poultry.broiler_farming_system.service.groupchat.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Admin-only moderation view over all group chats. Access is restricted to
// ROLE_ADMIN by the existing "/api/v1/admin/**" rule in SecurityConfig, so
// no per-group membership check is needed here (unlike GroupChatController).
@RestController
@RequestMapping("/api/v1/admin/group-chats")
@RequiredArgsConstructor
public class AdminGroupChatController {

    private final GroupChatService groupChatService;

    @GetMapping
    public List<AdminGroupChatSummaryResponse> listAll() {
        return groupChatService.listAllGroups();
    }

    @GetMapping("/{groupChatId}")
    public AdminGroupChatDetailResponse getDetail(@PathVariable Long groupChatId) {
        return groupChatService.getGroupDetail(groupChatId);
    }

    @DeleteMapping("/{groupChatId}/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable Long groupChatId, @PathVariable Long messageId) {
        groupChatService.deleteMessage(groupChatId, messageId);
    }
}
