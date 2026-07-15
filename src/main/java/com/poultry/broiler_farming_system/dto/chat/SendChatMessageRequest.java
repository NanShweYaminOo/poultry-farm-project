package com.poultry.broiler_farming_system.dto.chat;

// senderId is NOT a field here -- it's the authenticated caller, taken from
// the security context (REST) or the STOMP session's bound Principal
// (WebSocket), never client input. Any two users may chat, per spec -- no
// role/eligibility check like group chat's PAID/ADMIN restriction.
public record SendChatMessageRequest(Long receiverId, String content) {
}
