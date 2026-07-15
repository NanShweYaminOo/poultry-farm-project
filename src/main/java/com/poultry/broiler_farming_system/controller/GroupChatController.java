package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.groupchat.GroupChatMessageResponse;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatSummaryResponse;
import com.poultry.broiler_farming_system.dto.groupchat.SendGroupChatMessageRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.groupchat.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Access restricted to ROLE_PAID/ROLE_ADMIN at the SecurityConfig level --
// "Group Chat accessible only by active Farmers and Admins" per spec. There
// is exactly one group chat in the system (see GroupChatService's Javadoc),
// so there's no create/invite flow -- getSharedGroup() below auto-provisions
// it and auto-joins the caller.
//
// Contract unchanged for sendMessage/listMessages -- sendMessage() ALSO
// pushes the saved message to /topic/group-chat/{id} afterward, so a client
// connected over STOMP (GroupChatWebSocketController's live-send path)
// still sees a REST-originated message in real time.
@RestController
@RequestMapping("/api/v1/group-chats")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public GroupChatSummaryResponse getSharedGroup(@AuthenticationPrincipal UserPrincipal principal) {
        return groupChatService.getOrJoinSharedGroup(principal.getId());
    }

    @PostMapping("/{groupChatId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupChatMessageResponse sendMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long groupChatId,
            @RequestBody SendGroupChatMessageRequest request) {
        GroupChatMessageResponse response = groupChatService.sendMessage(groupChatId, principal.getId(), request);
        messagingTemplate.convertAndSend("/topic/group-chat/" + groupChatId, response);
        return response;
    }

    @GetMapping("/{groupChatId}/messages")
    public List<GroupChatMessageResponse> listMessages(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long groupChatId) {
        return groupChatService.listMessages(groupChatId, principal.getId());
    }
}
