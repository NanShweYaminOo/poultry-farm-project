package com.poultry.broiler_farming_system.service.groupchat;

import com.poultry.broiler_farming_system.dto.groupchat.AddGroupChatMemberRequest;
import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatDetailResponse;
import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatMemberResponse;
import com.poultry.broiler_farming_system.dto.groupchat.AdminGroupChatSummaryResponse;
import com.poultry.broiler_farming_system.dto.groupchat.CreateGroupChatRequest;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatMessageResponse;
import com.poultry.broiler_farming_system.dto.groupchat.GroupChatResponse;
import com.poultry.broiler_farming_system.dto.groupchat.SendGroupChatMessageRequest;
import com.poultry.broiler_farming_system.entity.GroupChat;
import com.poultry.broiler_farming_system.entity.GroupChatMember;
import com.poultry.broiler_farming_system.entity.GroupChatMemberId;
import com.poultry.broiler_farming_system.entity.GroupChatMessage;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.GroupChatMemberRepository;
import com.poultry.broiler_farming_system.repository.GroupChatMessageRepository;
import com.poultry.broiler_farming_system.repository.GroupChatRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.moderation.ContentModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupChatServiceImpl implements GroupChatService {

    private static final Set<UserRole> ELIGIBLE_ROLES = Set.of(UserRole.PAID, UserRole.ADMIN);

    private final GroupChatRepository groupChatRepository;
    private final GroupChatMemberRepository groupChatMemberRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final UserRepository userRepository;
    private final ContentModerationService contentModerationService;

    @Override
    public GroupChatResponse createGroup(Long creatorId, CreateGroupChatRequest request) {
        if (!StringUtils.hasText(request.groupName())) {
            throw new IllegalArgumentException("groupName is required.");
        }

        User creator = getUser(creatorId);

        GroupChat group = new GroupChat();
        group.setGroupName(request.groupName().trim());
        group.setCreatedBy(creator);
        GroupChat savedGroup = groupChatRepository.save(group);

        // Creator joins their own group automatically -- otherwise they
        // couldn't send a message to it without a separate join call.
        addMemberInternal(savedGroup, creator);

        return toGroupResponse(savedGroup);
    }

    @Override
    public void addMember(Long groupChatId, Long requesterId, AddGroupChatMemberRequest request) {
        GroupChat group = getGroup(groupChatId);
        requireMember(groupChatId, requesterId);

        User target = getUser(request.userId());
        if (!ELIGIBLE_ROLES.contains(target.getRole())) {
            throw new IllegalArgumentException(
                    "User " + target.getId() + " is not an active Farmer or Admin and cannot join the group chat.");
        }

        addMemberInternal(group, target);
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
    @Transactional(readOnly = true)
    public List<AdminGroupChatSummaryResponse> listAllGroups() {
        return groupChatRepository.findAll().stream()
                .map(this::toAdminSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminGroupChatDetailResponse getGroupDetail(Long groupChatId) {
        GroupChat group = getGroup(groupChatId);

        List<AdminGroupChatMemberResponse> members = groupChatMemberRepository.findByIdGroupChatId(groupChatId).stream()
                .map(member -> new AdminGroupChatMemberResponse(
                        member.getUser().getId(), member.getUser().getUsername(), member.getUser().getFullName()))
                .toList();

        List<GroupChatMessageResponse> messages = groupChatMessageRepository.findByGroupChatIdOrderBySentAtAsc(groupChatId).stream()
                .map(this::toMessageResponse)
                .toList();

        return new AdminGroupChatDetailResponse(group.getId(), group.getGroupName(), group.getCreatedDate(), members, messages);
    }

    @Override
    public void deleteMessage(Long groupChatId, Long messageId) {
        GroupChatMessage message = groupChatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message " + messageId + " was not found."));
        if (!message.getGroupChat().getId().equals(groupChatId)) {
            throw new ResourceNotFoundException("Message " + messageId + " does not belong to group chat " + groupChatId + ".");
        }
        groupChatMessageRepository.delete(message);
    }

    private AdminGroupChatSummaryResponse toAdminSummaryResponse(GroupChat group) {
        GroupChatMessage lastMessage = groupChatMessageRepository
                .findTopByGroupChatIdOrderBySentAtDesc(group.getId())
                .orElse(null);
        return new AdminGroupChatSummaryResponse(
                group.getId(),
                group.getGroupName(),
                group.getCreatedBy().getId(),
                group.getCreatedBy().getUsername(),
                group.getCreatedDate(),
                groupChatMemberRepository.countByIdGroupChatId(group.getId()),
                groupChatMessageRepository.countByGroupChatId(group.getId()),
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

    private GroupChatResponse toGroupResponse(GroupChat group) {
        return new GroupChatResponse(
                group.getId(), group.getGroupName(), group.getCreatedBy().getId(), group.getCreatedDate());
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
