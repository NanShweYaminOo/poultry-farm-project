package com.poultry.broiler_farming_system.service.groupchat;

import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatDetailResponse;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatMessageResponse;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatSummaryResponse;
import com.poultry.broiler_farming_system.dto.groupchat.SendGroupChatMessageRequest;

import java.util.List;

// There is exactly one group chat in the whole system, shared by every
// PAID Farmer and every Admin -- not a create-your-own-group/invite-members
// feature. SecurityConfig restricts every endpoint here to
// ROLE_PAID/ROLE_ADMIN, so eligibility is already enforced before any of
// these methods run; there is no further per-group membership to manage
// beyond auto-joining the caller into that one group.
public interface GroupChatService {

    // Auto-provisions the shared group on first-ever call (owned by
    // whichever ADMIN account happens to exist first) and auto-joins the
    // caller into it. Idempotent -- safe to call on every page load.
    GroupChatSummaryResponse getOrJoinSharedGroup(Long userId);

    // content is screened by ContentModerationService before the message is stored.
    GroupChatMessageResponse sendMessage(Long groupChatId, Long senderId, SendGroupChatMessageRequest request);

    List<GroupChatMessageResponse> listMessages(Long groupChatId, Long requesterId);

    // Admin-only moderation view of the shared group (auto-provisions it
    // too, in case an admin opens this page before any farmer ever has).
    AdminGroupChatDetailResponse getSharedGroupDetail();

    void deleteMessage(Long adminId, Long messageId, String reason);
}
