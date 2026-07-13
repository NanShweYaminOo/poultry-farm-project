package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.groupchat.AddGroupChatMemberRequest;
import com.poultry.broiler_farming_system.dto.groupchat.CreateGroupChatRequest;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatMessageResponse;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatResponse;
import com.poultry.broiler_farming_system.dto.groupchat.SendGroupChatMessageRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.groupchat.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Access restricted to ROLE_PAID/ROLE_ADMIN at the SecurityConfig level --
// "Group Chat accessible only by active Farmers and Admins" per spec.
@RestController
@RequestMapping("/api/v1/group-chats")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupChatResponse create(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateGroupChatRequest request) {
        return groupChatService.createGroup(principal.getId(), request);
    }

    @PostMapping("/{groupChatId}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addMember(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long groupChatId,
            @RequestBody AddGroupChatMemberRequest request) {
        groupChatService.addMember(groupChatId, principal.getId(), request);
    }

    @PostMapping("/{groupChatId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupChatMessageResponse sendMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long groupChatId,
            @RequestBody SendGroupChatMessageRequest request) {
        return groupChatService.sendMessage(groupChatId, principal.getId(), request);
    }

    @GetMapping("/{groupChatId}/messages")
    public List<GroupChatMessageResponse> listMessages(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long groupChatId) {
        return groupChatService.listMessages(groupChatId, principal.getId());
    }
}
