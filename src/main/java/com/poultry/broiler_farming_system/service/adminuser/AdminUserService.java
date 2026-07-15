package com.poultry.broiler_farming_system.service.adminuser;

import com.poultry.broiler_farming_system.dto.user.AdminUserDetailResponse;
import com.poultry.broiler_farming_system.dto.user.AdminUserFilter;
import com.poultry.broiler_farming_system.dto.user.AdminUserSummaryResponse;
import com.poultry.broiler_farming_system.dto.user.IssueWarningRequest;
import com.poultry.broiler_farming_system.dto.user.UserWarningResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    // Any combination of filter fields may be null/absent.
    Page<AdminUserSummaryResponse> listUsers(AdminUserFilter filter, Pageable pageable);

    // Includes batch/post counts and a payment-status-count summary --
    // PaymentTransaction has no monetary amount field to sum.
    AdminUserDetailResponse getUserDetail(Long userId);

    // Idempotent: banning an already-banned user just re-saves the same
    // state. Refuses to act on the caller's own account or an ADMIN target.
    AdminUserSummaryResponse banUser(Long adminId, Long userId, String reason);

    // Idempotent, no ADMIN/self guard needed -- unbanning is never punitive.
    AdminUserSummaryResponse unbanUser(Long adminId, Long userId);

    // Clears isFlaggedForReview after an admin has reviewed the automated
    // profanity flag. Idempotent; no ADMIN/self guard since this isn't punitive.
    AdminUserSummaryResponse dismissFlag(Long adminId, Long userId);

    // Records a formal warning (new user_warnings row) without banning.
    // Refuses to act on the caller's own account or an ADMIN target.
    UserWarningResponse issueWarning(Long adminId, Long userId, IssueWarningRequest request);

    // Full cascade hard-delete: cleans up every FK to this user that isn't
    // already JPA-cascaded (sales posts, buy requests, chat messages, group
    // chat messages/memberships, group chats they created, feedback
    // tickets, warnings they received) before deleting the row itself.
    // Batches (and everything that cascades from a batch -- daily logs,
    // alarms, expenses, inventory, payment transactions) are cascaded
    // automatically via User.batches. Refuses to act on the caller's own
    // account or an ADMIN target.
    void deleteUser(Long adminId, Long userId, String reason);
}
