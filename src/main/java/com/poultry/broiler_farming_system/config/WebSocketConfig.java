package com.poultry.broiler_farming_system.config;

import com.poultry.broiler_farming_system.security.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Enables STOMP-over-WebSocket messaging for both P2P chat
 * ({@link com.poultry.broiler_farming_system.controller.ChatWebSocketController})
 * and real-time group chat
 * ({@link com.poultry.broiler_farming_system.controller.GroupChatWebSocketController}).
 *
 * <p><b>Simple broker vs. an external broker (RabbitMQ/ActiveMQ):</b> this app
 * runs as a single embedded-Tomcat instance against one MySQL database, with no
 * load balancer or multi-instance deployment anywhere in its configuration --
 * see {@code application.properties}. Message volume is a farm's chat traffic
 * (P2P messages, group chat, a handful of concurrent users per group), nowhere
 * near what would saturate an in-memory broker. {@link MessageBrokerRegistry#enableSimpleBroker}
 * keeps everything in-process: no extra service to install, run, secure, or
 * monitor, and one less moving part for a project this size. The tradeoff to
 * know about: the simple broker's subscription registry lives entirely in this
 * JVM's heap, so it cannot relay a message published on one instance to a
 * subscriber connected to a *different* instance. The moment this app is
 * horizontally scaled behind a load balancer, that tradeoff turns into silently
 * dropped messages for whichever fraction of users landed on a different node
 * than the sender -- at that point, swap this for
 * {@link MessageBrokerRegistry#enableStompBrokerRelay} pointing at RabbitMQ
 * (with its STOMP plugin) or ActiveMQ, which every connected instance publishes
 * through and subscribes from, restoring cross-instance delivery. Nothing else
 * in this file (the endpoint, the destination prefixes, the interceptor) needs
 * to change when that day comes -- only this one method.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    // Same-origin by default (the JSP dashboard/admin shells and the WebSocket
    // endpoint are served by this same app) -- override via
    // app.websocket.allowed-origins for a split frontend/backend deployment.
    @Value("${app.websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS fallback for browsers/networks that block native WebSocket
        // (some corporate proxies do); transparent to STOMP clients that speak
        // native WebSocket, they just connect straight through.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic -- broadcast destinations (group chat rooms).
        // /queue -- combined with setUserDestinationPrefix below, backs each
        // user's private P2P message/error queue.
        registry.enableSimpleBroker("/topic", "/queue");
        // Client SEND frames must target /app/** to reach an @MessageMapping
        // controller method, e.g. /app/chat.send.
        registry.setApplicationDestinationPrefixes("/app");
        // Lets server code address a specific user via
        // SimpMessagingTemplate.convertAndSendToUser(username, "/queue/messages", ...)
        // and lets a client subscribe to its own mail at /user/queue/messages --
        // Spring rewrites both ends using the Principal bound at CONNECT (see
        // StompAuthChannelInterceptor), so no user can subscribe to another
        // user's mail even if they guess the destination string.
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
