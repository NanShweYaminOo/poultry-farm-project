package com.poultry.broiler_farming_system.service.groupchat;

import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatDetailResponse;
import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatMemberResponse;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatMessageResponse;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatSummaryResponse;
import com.poultry.broiler_farming_system.dto.groupchat.SendGroupChatMessageRequest;
import com.poultry.broiler_farming_system.entity.GroupChat;
import com.poultry.broiler_farming_system.entity.GroupChatMember;
import com.poultry.broiler_farming_system.entity.GroupChatMemberId;
import com.poultry.broiler_farming_system.entity.GroupChatMessage;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.SystemLogAction;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.GroupChatMemberRepository;
import com.poultry.broiler_farming_system.repository.GroupChatMessageRepository;
import com.poultry.broiler_farming_system.repository.GroupChatRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.moderation.ContentModerationService;
import com.poultry.broiler_farming_system.service.systemlog.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupChatServiceImpl implements GroupChatService {

    // Display name for the one system-wide group chat, created lazily the
    // first time anyone (Farmer or Admin) opens the feature.
    private static final String SHARED_GROUP_NAME = "Farmers' Lounge";

    private final GroupChatRepository groupChatRepository;
    private final GroupChatMemberRepository groupChatMemberRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final UserRepository userRepository;
    private final ContentModerationService contentModerationService;
    private final SystemLogService systemLogService;

    @Override
    public GroupChatSummaryResponse getOrJoinSharedGroup(Long userId) {
        User user = getUser(userId);
        GroupChat group = resolveSharedGroup();
        addMemberInternal(group, user);
        return toSummaryResponse(group);
    }

    @Override
    public GroupChatMessageResponse sendMessage(Long groupChatId, Long senderId, SendGroupChatMessageRequest request) {
        if (!StringUtils.hasText(request.content())) {
            throw new IllegalArgumentException("content is required.");
        }

        GroupChat group = getGroup(groupChatId);
        User sender = getUser(senderId);
        requireMember(groupChatId, senderId);

        contentModerationService.moderate(sender, request.content());

        GroupChatMessage message = new GroupChatMessage();
        message.setGroupChat(group);
        message.setSender(sender);
        message.setContent(request.content().trim());

        GroupChatMessage saved = groupChatMessageRepository.save(message);
        return toMessageResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupChatMessageResponse> listMessages(Long groupChatId, Long requesterId) {
        getGroup(groupChatId);
        requireMember(groupChatId, requesterId);
        return groupChatMessageRepository.findByGroupChatIdOrderBySentAtAsc(groupChatId).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Override
    public AdminGroupChatDetailResponse getSharedGroupDetail() {
        GroupChat group = resolveSharedGroup();

        List<AdminGroupChatMemberResponse> members = groupChatMemberRepository.findByIdGroupChatId(group.getId()).stream()
                .map(member -> new AdminGroupChatMemberResponse(
                        member.getUser().getId(), member.getUser().getUsername(), member.getUser().getFullName()))
                .toList();

        List<GroupChatMessageResponse> messages = groupChatMessageRepository.findByGroupChatIdOrderBySentAtAsc(group.getId()).stream()
                .map(this::toMessageResponse)
                .toList();

        return new AdminGroupChatDetailResponse(group.getId(), group.getGroupName(), group.getCreatedDate(), members, messages);
    }

    @Override
    public void deleteMessage(Long adminId, Long messageId, String reason) {
        GroupChatMessage message = groupChatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message " + messageId + " was not found."));

        String description = "Deleted message " + messageId + " in the shared group chat sent by '"
                + message.getSender().getUsername() + "'"
                + (StringUtils.hasText(reason) ? "; reason: " + reason.trim() : ".");

        groupChatMessageRepository.delete(message);

        systemLogService.record(adminId, SystemLogAction.DELETE_GROUP_CHAT_MESSAGE, "GROUP_CHAT_MESSAGE", messageId, description);
    }

    // Deterministically resolves to the same row every time (see
    // GroupChatRepository.findFirstByOrderByIdAsc's Javadoc), creating it on
    // the very first call anyone ever makes. Owned by whichever ADMIN
    // account happens to exist first -- ownership here is only a required
    // FK on the entity, not a meaningful permission (every Admin can
    // moderate the shared group regardless of who "created" it).
    private GroupChat resolveSharedGroup() {
        return groupChatRepository.findFirstByOrderByIdAsc()
                .orElseGet(this::createSharedGroup);
    }

    private GroupChat createSharedGroup() {
        User owner = userRepository.findFirstByRole(UserRole.ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "No ADMIN account exists yet to own the shared group chat."));
        GroupChat group = new GroupChat();
        group.setGroupName(SHARED_GROUP_NAME);
        group.setCreatedBy(owner);
        return groupChatRepository.save(group);
    }

    private GroupChatSummaryResponse toSummaryResponse(GroupChat group) {
        GroupChatMessage lastMessage = groupChatMessageRepository
                .findTopByGroupChatIdOrderBySentAtDesc(group.getId())
                .orElse(null);
        return new GroupChatSummaryResponse(
                group.getId(),
                group.getGroupName(),
                groupChatMemberRepository.countByIdGroupChatId(group.getId()),
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getSentAt() : null);
    }

    private void addMemberInternal(GroupChat group, User user) {
        GroupChatMemberId id = new GroupChatMemberId(group.getId(), user.getId());
        if (groupChatMemberRepository.existsById(id)) {
            return;
        }
        GroupChatMember member = new GroupChatMember();
        member.setId(id);
        member.setGroupChat(group);
        member.setUser(user);
        groupChatMemberRepository.save(member);
    }

    private void requireMember(Long groupChatId, Long userId) {
        if (!groupChatMemberRepository.existsById(new GroupChatMemberId(groupChatId, userId))) {
            throw new UnauthorizedActionException(
                    "You are not a member of group chat " + groupChatId + ".");
        }
    }

    private GroupChat getGroup(Long groupChatId) {
        return groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat " + groupChatId + " was not found."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " was not found."));
    }

    private GroupChatMessageResponse toMessageResponse(GroupChatMessage message) {
        return new GroupChatMessageResponse(
                message.getId(),
                message.getGroupChat().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.getSentAt()
        );
    }
}
