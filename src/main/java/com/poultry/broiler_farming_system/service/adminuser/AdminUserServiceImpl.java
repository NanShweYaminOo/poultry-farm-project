package com.poultry.broiler_farming_system.service.adminuser;

import com.poultry.broiler_farming_system.dto.user.AdminUserDetailResponse;
import com.poultry.broiler_farming_system.dto.user.AdminUserFilter;
import com.poultry.broiler_farming_system.dto.user.AdminUserSummaryResponse;
import com.poultry.broiler_farming_system.dto.user.IssueWarningRequest;
import com.poultry.broiler_farming_system.dto.user.UserWarningResponse;
import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.GroupChat;
import com.poultry.broiler_farming_system.entity.PaymentTransaction;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.UserWarning;
import com.poultry.broiler_farming_system.entity.enums.PaymentStatus;
import com.poultry.broiler_farming_system.entity.enums.SystemLogAction;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.repository.BuyRequestRepository;
import com.poultry.broiler_farming_system.repository.ChatMessageRepository;
import com.poultry.broiler_farming_system.repository.FeedbackTicketRepository;
import com.poultry.broiler_farming_system.repository.GroupChatMemberRepository;
import com.poultry.broiler_farming_system.repository.GroupChatMessageRepository;
import com.poultry.broiler_farming_system.repository.GroupChatRepository;
import com.poultry.broiler_farming_system.repository.PaymentTransactionRepository;
import com.poultry.broiler_farming_system.repository.SalesPostRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.repository.UserWarningRepository;
import com.poultry.broiler_farming_system.service.notification.NotificationService;
import com.poultry.broiler_farming_system.service.scheduling.MedicineAlarmSchedulerService;
import com.poultry.broiler_farming_system.service.systemlog.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final SalesPostRepository salesPostRepository;
    private final BuyRequestRepository buyRequestRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserWarningRepository userWarningRepository;
    private final FeedbackTicketRepository feedbackTicketRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GroupChatRepository groupChatRepository;
    private final GroupChatMemberRepository groupChatMemberRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final MedicineAlarmSchedulerService schedulerService;
    private final SystemLogService systemLogService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserSummaryResponse> listUsers(AdminUserFilter filter, Pageable pageable) {
        return userRepository.findAll(buildSpecification(filter), pageable).map(this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(Long userId) {
        User user = getUser(userId);

        long batchCount = batchRepository.countByFarmerId(userId);
        long salesPostCount = salesPostRepository.countByCreatorId(userId);
        long buyRequestCount = buyRequestRepository.countByCreatorId(userId);

        List<PaymentTransaction> payments = paymentTransactionRepository.findByUserIdOrderByTransactionTimestampDesc(userId);
        long pendingCount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();
        long approvedCount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.APPROVED).count();
        long rejectedCount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.REJECTED).count();
        AdminUserDetailResponse.PaymentHistorySummary paymentHistory = new AdminUserDetailResponse.PaymentHistorySummary(
                payments.size(), pendingCount, approvedCount, rejectedCount);

        List<UserWarningResponse> warnings = userWarningRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toWarningResponse)
                .toList();

        return new AdminUserDetailResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLocation(),
                user.getPreferredLanguage(),
                user.getRole(),
                user.getAccountType(),
                user.getIsBanned(),
                user.getIsFlaggedForReview(),
                user.getPostingExtensionExpiry(),
                user.getCreatedDate(),
                batchCount,
                salesPostCount,
                buyRequestCount,
                paymentHistory,
                warnings);
    }

    @Override
    public AdminUserSummaryResponse banUser(Long adminId, Long userId, String reason) {
        requireNotSelf(adminId, userId, "ban");
        User target = getUser(userId);
        requireNonAdminTarget(target, "banned");

        target.setIsBanned(true);
        User saved = userRepository.save(target);

        systemLogService.record(adminId, SystemLogAction.BAN_USER, "USER", userId,
                "Banned user '" + target.getUsername() + "' (id " + userId + ")"
                        + (StringUtils.hasText(reason) ? "; reason: " + reason.trim() : "."));

        return toSummaryResponse(saved);
    }

    @Override
    public AdminUserSummaryResponse unbanUser(Long adminId, Long userId) {
        User target = getUser(userId);

        target.setIsBanned(false);
        User saved = userRepository.save(target);

        systemLogService.record(adminId, SystemLogAction.UNBAN_USER, "USER", userId,
                "Unbanned user '" + target.getUsername() + "' (id " + userId + ").");

        return toSummaryResponse(saved);
    }

    @Override
    public AdminUserSummaryResponse dismissFlag(Long adminId, Long userId) {
        User target = getUser(userId);

        target.setIsFlaggedForReview(false);
        User saved = userRepository.save(target);

        systemLogService.record(adminId, SystemLogAction.DISMISS_FLAG, "USER", userId,
                "Dismissed the review flag on user '" + target.getUsername() + "' (id " + userId + ").");

        return toSummaryResponse(saved);
    }

    @Override
    public UserWarningResponse issueWarning(Long adminId, Long userId, IssueWarningRequest request) {
        if (!StringUtils.hasText(request.reason())) {
            throw new IllegalArgumentException("reason is required.");
        }
        requireNotSelf(adminId, userId, "warn");

        User admin = getUser(adminId);
        User target = getUser(userId);
        requireNonAdminTarget(target, "warned");

        UserWarning warning = new UserWarning();
        warning.setAdmin(admin);
        warning.setRecipient(target);
        warning.setReason(request.reason().trim());
        UserWarning saved = userWarningRepository.save(warning);

        systemLogService.record(adminId, SystemLogAction.ISSUE_WARNING, "USER", userId,
                "Issued a warning to user '" + target.getUsername() + "' (id " + userId
                        + "); reason: " + request.reason().trim());
        notificationService.notifyAdminWarningIssued(saved);

        return toWarningResponse(saved);
    }

    @Override
    public void deleteUser(Long adminId, Long userId, String reason) {
        requireNotSelf(adminId, userId, "delete");
        User target = getUser(userId);
        requireNonAdminTarget(target, "deleted");

        // Evict every pending Quartz medicine alarm across this farmer's
        // batches before anything cascades away, mirroring
        // BatchServiceImpl.stopBatch().
        for (Batch batch : batchRepository.findByFarmerIdOrderByCreatedDateDesc(userId)) {
            schedulerService.cancelAllForBatch(batch.getId());
        }

        // Every FK to User that ISN'T cascaded from the User side must be
        // cleared manually before the row itself can be deleted. Batches
        // (and everything that cascades from Batch -- daily logs, alarms,
        // expenses, inventory, payment transactions) ARE cascaded via
        // User.batches, so they're left to the final delete() below.
        groupChatMessageRepository.deleteBySenderId(userId);
        groupChatMemberRepository.deleteByIdUserId(userId);
        List<GroupChat> ownedGroupChats = groupChatRepository.findByCreatedById(userId);
        // deleteAll (not a bulk deleteBy query) so GroupChat's own
        // cascade=ALL/orphanRemoval on members+messages fires for every
        // other member's data in a group this user created.
        groupChatRepository.deleteAll(ownedGroupChats);

        chatMessageRepository.deleteBySenderIdOrReceiverId(userId, userId);
        feedbackTicketRepository.deleteBySubmittedById(userId);
        salesPostRepository.deleteByCreatorId(userId);
        buyRequestRepository.deleteByCreatorId(userId);
        userWarningRepository.deleteByRecipientId(userId);

        String username = target.getUsername();
        userRepository.delete(target);

        systemLogService.record(adminId, SystemLogAction.DELETE_USER, "USER", userId,
                "Deleted user '" + username + "' (id " + userId + ")"
                        + (StringUtils.hasText(reason) ? "; reason: " + reason.trim() : "."));
    }

    private Specification<User> buildSpecification(AdminUserFilter filter) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (filter.role() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), filter.role()));
        }
        if (filter.accountType() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("accountType"), filter.accountType()));
        }
        if (filter.isFlaggedForReview() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isFlaggedForReview"), filter.isFlaggedForReview()));
        }
        if (filter.isBanned() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isBanned"), filter.isBanned()));
        }
        if (StringUtils.hasText(filter.location())) {
            String pattern = "%" + filter.location().trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("location")), pattern));
        }
        return spec;
    }

    private void requireNotSelf(Long adminId, Long targetUserId, String action) {
        if (adminId.equals(targetUserId)) {
            throw new UnauthorizedActionException("You cannot " + action + " your own account.");
        }
    }

    private void requireNonAdminTarget(User target, String pastTenseAction) {
        if (target.getRole() == UserRole.ADMIN) {
            throw new UnauthorizedActionException(
                    "User " + target.getId() + " is an Admin and cannot be " + pastTenseAction + " through this endpoint.");
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " was not found."));
    }

    private AdminUserSummaryResponse toSummaryResponse(User user) {
        return new AdminUserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLocation(),
                user.getRole(),
                user.getAccountType(),
                user.getIsBanned(),
                user.getIsFlaggedForReview(),
                user.getCreatedDate());
    }

    private UserWarningResponse toWarningResponse(UserWarning warning) {
        return new UserWarningResponse(
                warning.getId(),
                warning.getAdmin().getId(),
                warning.getAdmin().getUsername(),
                warning.getRecipient().getId(),
                warning.getRecipient().getUsername(),
                warning.getReason(),
                warning.getCreatedAt());
    }
}
