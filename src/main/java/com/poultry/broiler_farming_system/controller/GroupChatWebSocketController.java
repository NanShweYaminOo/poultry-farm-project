package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.groupchat.GroupChatMessageResponse;
import com.poultry.broiler_farming_system.dto.groupchat.SendGroupChatMessageRequest;
import com.poultry.broiler_farming_system.dto.websocket.StompErrorResponse;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.ProfaneContentException;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.groupchat.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

// Real-time half of group chat. GroupChatController's existing REST
// endpoints (create/addMember/sendMessage/listMessages) are completely
// unchanged -- this only ADDS a live-send path on top, delegating to the
// exact same GroupChatService.sendMessage(...) so persistence, membership
// enforcement, and content moderation stay defined in exactly one place.
// GroupChatController.send() now also broadcasts to /topic/group-chat/{id}
// after a REST-originated send, so STOMP-connected subscribers see messages
// regardless of which path a client used to send them.
@Controller
@RequiredArgsConstructor
public class GroupChatWebSocketController {

    private final GroupChatService groupChatService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Client sends to /app/group-chat.send/{groupChatId}; broadcast target is
    // /topic/group-chat/{groupChatId}. Subscribing to that topic is itself
    // gated by StompAuthChannelInterceptor (role + membership), so only
    // eligible members ever receive this.
    @MessageMapping("/group-chat.send/{groupChatId}")
    public void send(
            @DestinationVariable Long groupChatId,
            @Payload SendGroupChatMessageRequest request,
            Principal principal) {
        UserPrincipal sender = resolvePrincipal(principal);

        // SecurityConfig's hasAnyRole("PAID","ADMIN") on /api/v1/group-chats/**
        // only gates the REST path -- it has no effect on STOMP destinations, so
        // the same role rule has to be re-enforced here explicitly. Re-loaded
        // fresh rather than trusting the CONNECT-time snapshot, since a
        // long-lived STOMP session can outlive a role change (see
        // StompAuthChannelInterceptor's Javadoc for the same reasoning).
        User currentUser = userRepository.findById(sender.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User " + sender.getId() + " was not found."));
        if (Boolean.TRUE.equals(currentUser.getIsBanned())) {
            throw new UnauthorizedActionException("This account has been banned.");
        }
        if (currentUser.getRole() != UserRole.PAID && currentUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException("Group chat is for active farmers and admins only.");
        }

        // Membership check + content moderation both happen inside this existing
        // call -- see GroupChatServiceImpl.sendMessage.
        GroupChatMessageResponse response = groupChatService.sendMessage(groupChatId, sender.getId(), request);
        messagingTemplate.convertAndSend("/topic/group-chat/" + groupChatId, response);
    }

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
