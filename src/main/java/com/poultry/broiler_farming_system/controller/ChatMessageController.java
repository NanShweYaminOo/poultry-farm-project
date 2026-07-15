package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.chat.ChatMessageResponse;
import com.poultry.broiler_farming_system.dto.chat.ConversationSummaryResponse;
import com.poultry.broiler_farming_system.dto.chat.SendChatMessageRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.chat.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST side of P2P chat: history/list/mark-read, plus a POST send fallback
// for non-WebSocket clients. Real-time delivery for STOMP-connected clients
// goes through ChatWebSocketController's /app/chat.send instead, but a
// message sent via this REST endpoint is *also* pushed to the receiver's
// live /user/queue/messages below -- otherwise a client connected over STOMP
// would only see a REST-sent message on next poll/reload, defeating the
// point of "real-time" for mixed send-paths. No role restriction here (any
// authenticated user, any role) -- any two users may chat, per spec.
@RestController
@RequestMapping("/api/v1/chat-messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse send(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody SendChatMessageRequest request) {
        ChatMessageResponse response = chatMessageService.sendMessage(principal.getId(), request);
        broadcast(response);
        return response;
    }

    @GetMapping("/conversations")
    public List<ConversationSummaryResponse> listConversations(@AuthenticationPrincipal UserPrincipal principal) {
        return chatMessageService.listConversations(principal.getId());
    }

    @GetMapping("/with/{otherUserId}")
    public List<ChatMessageResponse> getConversation(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long otherUserId) {
        return chatMessageService.getConversation(principal.getId(), otherUserId);
    }

    @PostMapping("/with/{otherUserId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markConversationAsRead(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long otherUserId) {
        chatMessageService.markConversationAsRead(principal.getId(), otherUserId);
    }

    private void broadcast(ChatMessageResponse response) {
        messagingTemplate.convertAndSendToUser(response.receiverUsername(), "/queue/messages", response);
        // Echoed back to the sender's own private queue too, so any other tab/device
        // they have open updates immediately without waiting on this HTTP response.
        messagingTemplate.convertAndSendToUser(response.senderUsername(), "/queue/messages", response);
    }
}
