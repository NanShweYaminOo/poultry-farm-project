package com.poultry.broiler_farming_system.security;

import com.poultry.broiler_farming_system.entity.GroupChatMemberId;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.repository.GroupChatMemberRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Authenticates and authorizes STOMP frames on the client-inbound channel.
 * Registered via {@code configureClientInboundChannel} in
 * {@link com.poultry.broiler_farming_system.config.WebSocketConfig}.
 *
 * <p><b>Why {@link com.poultry.broiler_farming_system.security.JwtAuthenticationFilter}
 * (the servlet filter) cannot do this job:</b>
 * <ol>
 *   <li>It is an {@code OncePerRequestFilter} -- it runs once per <i>servlet HTTP
 *       request</i>. The WebSocket handshake (the initial {@code GET /ws} with an
 *       {@code Upgrade} header) is the only HTTP request in the whole exchange; the
 *       moment it succeeds, the TCP connection is upgraded to the WebSocket protocol
 *       and every STOMP frame after that (CONNECT, SUBSCRIBE, SEND, DISCONNECT, ...)
 *       travels as a WebSocket <i>message</i>, not an HTTP request. There is no
 *       servlet request for the filter to run against ever again for the lifetime
 *       of that connection, which can be hours.</li>
 *   <li>Even for that one handshake request, a browser's native {@code WebSocket}
 *       constructor cannot set an {@code Authorization} header -- the JS WebSocket
 *       API only accepts a URL and a sub-protocol list, nothing else. So a Bearer
 *       token can never reach the filter via the header it looks for during a
 *       browser-originated handshake in the first place.</li>
 *   <li>The one thing that <i>does</i> survive the handshake is the
 *       {@code auth_token} cookie (browsers attach cookies to the handshake request
 *       automatically), and {@code JwtAuthenticationFilter} would happily populate
 *       {@code SecurityContextHolder} from it for that single request. But that
 *       {@code SecurityContext} is thread-local and dies with the handshake request
 *       thread -- Spring's STOMP support does not automatically carry it forward
 *       as the {@code Principal} for every later frame on the now-upgraded
 *       connection unless something explicitly wires that association. That
 *       "something" is this interceptor.</li>
 * </ol>
 * The fix: authenticate explicitly on the STOMP {@code CONNECT} frame instead,
 * where the JWT travels as a normal STOMP header (fully controllable by any STOMP
 * JS client such as {@code @stomp/stompjs} via {@code connectHeaders}, sidestepping
 * the browser's WebSocket-header limitation entirely), and bind the resulting
 * {@link Authentication} onto the STOMP session via
 * {@link StompHeaderAccessor#setUser(java.security.Principal)}. Spring's
 * {@code StompSubProtocolHandler} then re-attaches that same {@code Principal} to
 * every subsequent frame on the same session automatically, so authenticating once
 * at CONNECT is sufficient for {@code Principal} propagation -- but authorization
 * (who is allowed to SUBSCRIBE/SEND where) is re-checked per-frame below, against
 * a freshly-loaded {@link User}, not the CONNECT-time snapshot -- a STOMP session
 * can outlive a role change (e.g. PAID -&gt; FREE when a batch is stopped) or a ban,
 * mirroring the same "re-resolve from the DB every time" rule
 * {@code JwtAuthenticationFilter} already applies to ordinary REST requests.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String GROUP_TOPIC_PREFIX = "/topic/group-chat/";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final GroupChatMemberRepository groupChatMemberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticateConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscribe(accessor);
        }
        // SEND frames are intentionally NOT authorized here. A rejection thrown
        // from an interceptor becomes a raw STOMP ERROR frame, which per the STOMP
        // spec terminates the whole session -- fine for "you tried to eavesdrop on
        // a group you're not in" (a boundary violation), way too heavy for "your
        // message contained profanity" (an expected, recoverable, per-message
        // rejection). SEND-time business rules (membership, role, moderation) are
        // therefore enforced inside ChatWebSocketController /
        // GroupChatWebSocketController instead, whose @MessageExceptionHandler
        // methods route a soft rejection back to the sender via
        // @SendToUser("/queue/errors") without dropping the connection.

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);
        if (token == null || !jwtService.isValid(token)) {
            throw new MessagingException("Missing or invalid JWT on STOMP CONNECT.");
        }

        String username = jwtService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!userDetails.isEnabled()) {
            throw new MessagingException("This account has been banned.");
        }

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        // This is what makes every later frame on this same session report
        // accessor.getUser() / the @MessageMapping method's Principal argument as
        // this user -- see the class-level Javadoc.
        accessor.setUser(authentication);
    }

    private void authorizeSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        Authentication authentication = (Authentication) accessor.getUser();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new MessagingException("Not authenticated.");
        }

        // /user/** destinations (e.g. /user/queue/messages, /user/queue/errors) are
        // already scoped to the subscribing session by Spring's own
        // UserDestinationMessageHandler -- it rewrites the destination using the
        // Principal bound at CONNECT, so a client can only ever receive messages
        // convertAndSendToUser(...) actually addressed to *them*. There's nothing
        // further to check; this is what keeps P2P mail private without any manual
        // per-user-id topic bookkeeping.
        if (destination.startsWith("/user/")) {
            return;
        }

        if (destination.startsWith(GROUP_TOPIC_PREFIX)) {
            Long groupChatId = parseGroupChatId(destination);
            if (groupChatId == null) {
                throw new MessagingException("Malformed group chat destination.");
            }

            // Re-loaded from the DB, not read off the CONNECT-time Authentication --
            // see the class Javadoc on why a long-lived session can't trust a stale
            // snapshot for authorization decisions.
            User currentUser = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new MessagingException("User no longer exists."));
            if (Boolean.TRUE.equals(currentUser.getIsBanned())) {
                throw new MessagingException("This account has been banned.");
            }
            if (currentUser.getRole() != UserRole.PAID && currentUser.getRole() != UserRole.ADMIN) {
                throw new MessagingException("Group chat is for active farmers and admins only.");
            }
            boolean isMember = groupChatMemberRepository.existsById(new GroupChatMemberId(groupChatId, principal.getId()));
            if (!isMember) {
                throw new MessagingException("You are not a member of this group chat.");
            }
        }
    }

    private Long parseGroupChatId(String destination) {
        try {
            return Long.valueOf(destination.substring(GROUP_TOPIC_PREFIX.length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // The JWT rides as a normal STOMP header on the CONNECT frame itself (set via
    // e.g. stompClient.connectHeaders = { Authorization: 'Bearer <token>' } on the
    // client), not the WebSocket handshake's HTTP Authorization header -- see the
    // class Javadoc for why the handshake header isn't a usable option from a
    // browser.
    private String extractToken(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            return null;
        }
        String header = authHeaders.get(0);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring("Bearer ".length());
        }
        return null;
    }
}
