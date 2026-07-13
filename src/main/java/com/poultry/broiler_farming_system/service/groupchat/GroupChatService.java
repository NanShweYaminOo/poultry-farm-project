package com.poultry.broiler_farming_system.service.groupchat;

import com.poultry.broiler_farming_system.dto.groupchat.AddGroupChatMemberRequest;
import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatDetailResponse;
import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatSummaryResponse;
import com.poultry.broiler_farming_system.dto.groupchat.CreateGroupChatRequest;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatMessageResponse;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatResponse;
import com.poultry.broiler_farming_system.dto.groupchat.SendGroupChatMessageRequest;

import java.util.List;

// SecurityConfig restricts every endpoint here to ROLE_PAID/ROLE_ADMIN
// (active Farmers and Admins only, per spec). Membership is a second,
// per-group check on top of that role gate: being PAID doesn't mean you're
// in every group, only the ones you created or were added to.
public interface GroupChatService {

    GroupChatResponse createGroup(Long creatorId, CreateGroupChatRequest request);

    void addMember(Long groupChatId, Long requesterId, AddGroupChatMemberRequest request);

    // content is screened by ContentModerationService before the message is stored.
    GroupChatMessageResponse sendMessage(Long groupChatId, Long senderId, SendGroupChatMessageRequest request);

    List<GroupChatMessageResponse> listMessages(Long groupChatId, Long requesterId);

    // Admin-only moderation views; no membership check, unlike the above.
    List<AdminGroupChatSummaryResponse> listAllGroups();

    AdminGroupChatDetailResponse getGroupDetail(Long groupChatId);

    void deleteMessage(Long groupChatId, Long messageId);
}
