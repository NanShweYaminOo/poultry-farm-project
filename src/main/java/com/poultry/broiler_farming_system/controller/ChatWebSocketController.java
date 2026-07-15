package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.chat.ChatMessageResponse;
import com.poultry.broiler_farming_system.dto.chat.SendChatMessageRequest;
import com.poultry.broiler_farming_system.dto.websocket.StompErrorResponse;
import com.poultry.broiler_farming_system.exception.ProfaneContentException;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.chat.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

// STOMP side of P2P chat. The Principal argument below is exactly the
// Authentication StompAuthChannelInterceptor bound to this session at
// CONNECT -- Spring re-attaches it to every frame on the session
// automatically, so there's no re-authentication needed per message here,
// only the authorization/business-rule checks that ChatMessageService itself
// performs (moderation, ban re-check, self-message rejection).
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // Client sends to /app/chat.send. No @SendTo here -- delivery is targeted
    // (convertAndSendToUser), not broadcast, since a P2P message must reach
    // only the two participants.
    @MessageMapping("/chat.send")
    public void send(@Payload SendChatMessageRequest request, Principal principal) {
        UserPrincipal sender = resolvePrincipal(principal);
        ChatMessageResponse response = chatMessageService.sendMessage(sender.getId(), request);

        messagingTemplate.convertAndSendToUser(response.receiverUsername(), "/queue/messages", response);
        // Echoed back to the sender's own private queue -- confirms delivery and
        // keeps any other open tab/device of theirs in sync.
        messagingTemplate.convertAndSendToUser(response.senderUsername(), "/queue/messages", response);
    }

    // Recoverable, per-message rejections -- routed back to ONLY the sender via
    // their own private error queue, not a STOMP ERROR frame (which would
    // terminate the whole session over what is often just "don't swear").
    @MessageExceptionHandler(ProfaneContentException.class)
    @SendToUser("/queue/errors")
    public StompErrorResponse handleProfanity(ProfaneContentException ex) {
        return new StompErrorResponse(ex.getMessage());
    }

    @MessageExceptionHandler({IllegalArgumentException.class, UnauthorizedActionException.class, ResourceNotFoundException.class})
    @SendToUser("/queue/errors")
    public StompErrorResponse handleRejection(RuntimeException ex) {
        return new StompErrorResponse(ex.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public StompErrorResponse handleUnexpected(Exception ex) {
        return new StompErrorResponse("Could not send message.");
    }

    private UserPrincipal resolvePrincipal(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        throw new UnauthorizedActionException("Not authenticated.");
    }
}
