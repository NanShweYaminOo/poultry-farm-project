package com.poultry.broiler_farming_system.dto.websocket;

// Sent back to the offending sender only, over their own private
// /user/queue/errors -- STOMP has no HTTP status code to fall back on, and a
// raw STOMP ERROR frame would terminate the whole session, which is far too
// heavy-handed for a recoverable, expected rejection like profanity or a
// business-rule violation. See ChatWebSocketController and
// GroupChatWebSocketController's @MessageExceptionHandler methods.
public record StompErrorResponse(String message) {
}
