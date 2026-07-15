package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.user.AdminUserDetailResponse;
import com.poultry.broiler_farming_system.dto.user.AdminUserFilter;
import com.poultry.broiler_farming_system.dto.user.AdminUserSummaryResponse;
import com.poultry.broiler_farming_system.dto.user.BanUserRequest;
import com.poultry.broiler_farming_system.dto.user.IssueWarningRequest;
import com.poultry.broiler_farming_system.dto.user.UserWarningResponse;
import com.poultry.broiler_farming_system.entity.enums.AccountType;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.adminuser.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Admin-only user management + moderation. Access is restricted to
// ROLE_ADMIN by the existing "/api/v1/admin/**" rule in SecurityConfig.
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // All filters optional, e.g. ?role=PAID&isFlaggedForReview=true&page=0&size=20
    @GetMapping
    public Page<AdminUserSummaryResponse> listUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) AccountType accountType,
            @RequestParam(required = false) Boolean isFlaggedForReview,
            @RequestParam(required = false) Boolean isBanned,
            @RequestParam(required = false) String location,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        AdminUserFilter filter = new AdminUserFilter(role, accountType, isFlaggedForReview, isBanned, location);
        return adminUserService.listUsers(filter, pageable);
    }

    @GetMapping("/{userId}")
    public AdminUserDetailResponse getUserDetail(@PathVariable Long userId) {
        return adminUserService.getUserDetail(userId);
    }

    // Body may be omitted entirely; e.g. { "reason": "Repeated profanity in sales posts" }
    @PostMapping("/{userId}/ban")
    public AdminUserSummaryResponse banUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @RequestBody(required = false) BanUserRequest request) {
        return adminUserService.banUser(principal.getId(), userId, request != null ? request.reason() : null);
    }

    @PostMapping("/{userId}/unban")
    public AdminUserSummaryResponse unbanUser(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long userId) {
        return adminUserService.unbanUser(principal.getId(), userId);
    }

    @PostMapping("/{userId}/dismiss-flag")
    public AdminUserSummaryResponse dismissFlag(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long userId) {
        return adminUserService.dismissFlag(principal.getId(), userId);
    }

    // e.g. { "reason": "Inappropriate language in group chat" }
    @PostMapping("/{userId}/warnings")
    @ResponseStatus(HttpStatus.CREATED)
    public UserWarningResponse issueWarning(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @RequestBody IssueWarningRequest request) {
        return adminUserService.issueWarning(principal.getId(), userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        adminUserService.deleteUser(principal.getId(), userId, reason);
    }
}
