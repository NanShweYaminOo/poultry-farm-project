package com.poultry.broiler_farming_system.service.chat;

import com.poultry.broiler_farming_system.dto.chat.ChatMessageResponse;
import com.poultry.broiler_farming_system.dto.chat.ConversationSummaryResponse;
import com.poultry.broiler_farming_system.dto.chat.SendChatMessageRequest;

import java.util.List;

// Any two users may chat, per spec -- unlike GroupChatService, there is no
// PAID/ADMIN eligibility gate here at the service layer (SecurityConfig has
// no role restriction on /api/v1/chat-messages/** either). Shared by both the
// REST controller (ChatMessageController) and the STOMP controller
// (ChatWebSocketController) so persistence, validation, and moderation only
// happen in one place regardless of which transport a message arrived on.
public interface ChatMessageService {

    ChatMessageResponse sendMessage(Long senderId, SendChatMessageRequest request);

    List<ChatMessageResponse> getConversation(Long callerId, Long otherUserId);

    // One row per distinct conversation partner, most recently active first.
    List<ConversationSummaryResponse> listConversations(Long callerId);

    void markConversationAsRead(Long callerId, Long otherUserId);
}
