package com.poultry.broiler_farming_system.service.chat;

import com.poultry.broiler_farming_system.dto.chat.ChatMessageResponse;
import com.poultry.broiler_farming_system.dto.chat.ConversationSummaryResponse;
import com.poultry.broiler_farming_system.dto.chat.SendChatMessageRequest;
import com.poultry.broiler_farming_system.entity.ChatMessage;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.ChatMessageRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.moderation.ContentModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ContentModerationService contentModerationService;

    @Override
    public ChatMessageResponse sendMessage(Long senderId, SendChatMessageRequest request) {
        if (request.receiverId() == null) {
            throw new IllegalArgumentException("receiverId is required.");
        }
        if (!StringUtils.hasText(request.content())) {
            throw new IllegalArgumentException("content is required.");
        }
        if (request.receiverId().equals(senderId)) {
            throw new IllegalArgumentException("You cannot send a message to yourself.");
        }

        User sender = getUser(senderId);
        User receiver = getUser(request.receiverId());

        // Defense in depth for long-lived STOMP sessions: authentication-time ban
        // checks (JwtAuthenticationFilter per REST request, StompAuthChannelInterceptor
        // on STOMP CONNECT) can't catch a ban that happens mid-session, since a
        // WebSocket connection doesn't re-authenticate on its own. Re-checking here
        // means a banned user's send is rejected on their very next message either way.
        if (Boolean.TRUE.equals(sender.getIsBanned())) {
            throw new UnauthorizedActionException("This account has been banned.");
        }

        contentModerationService.moderate(sender, request.content());

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.content().trim())
                .build();

        return toResponse(chatMessageRepository.save(message));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversation(Long callerId, Long otherUserId) {
        getUser(callerId);
        getUser(otherUserId);
        // No membership/authorization check needed beyond both users existing --
        // the query is structurally scoped to messages between exactly these two
        // people, and callerId always comes from the authenticated caller, never
        // client input, so there's no way to read a conversation you're not part of.
        return chatMessageRepository.findConversation(callerId, otherUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> listConversations(Long callerId) {
        List<ChatMessage> all = chatMessageRepository.findBySenderIdOrReceiverIdOrderBySentAtDesc(callerId, callerId);

        // Input is already newest-first, so the first message we see for a given
        // "other party" is that conversation's last message -- a LinkedHashMap with
        // putIfAbsent both dedupes and preserves that most-recent-first order.
        Map<Long, ChatMessage> lastMessageByOtherUserId = new LinkedHashMap<>();
        for (ChatMessage message : all) {
            Long otherUserId = message.getSender().getId().equals(callerId)
                    ? message.getReceiver().getId()
                    : message.getSender().getId();
            lastMessageByOtherUserId.putIfAbsent(otherUserId, message);
        }

        return lastMessageByOtherUserId.entrySet().stream()
                .map(entry -> {
                    Long otherUserId = entry.getKey();
                    ChatMessage last = entry.getValue();
                    User other = last.getSender().getId().equals(otherUserId) ? last.getSender() : last.getReceiver();
                    long unreadCount = chatMessageRepository.countByReceiverIdAndSenderIdAndIsReadFalse(callerId, otherUserId);
                    return new ConversationSummaryResponse(
                            otherUserId, other.getUsername(), other.getFullName(),
                            last.getContent(), last.getSentAt(), unreadCount);
                })
                .toList();
    }

    @Override
    public void markConversationAsRead(Long callerId, Long otherUserId) {
        getUser(callerId);
        getUser(otherUserId);
        chatMessageRepository.markConversationAsRead(callerId, otherUserId);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " was not found."));
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getReceiver().getId(),
                message.getReceiver().getUsername(),
                message.getContent(),
                message.getSentAt(),
                message.getIsRead());
    }
}
