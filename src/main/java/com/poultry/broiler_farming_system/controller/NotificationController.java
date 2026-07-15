package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.notification.NotificationResponse;
import com.poultry.broiler_farming_system.dto.notification.UnreadCountResponse;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Any authenticated user can list/read their own notifications here. No
// SecurityConfig rule needed -- falls through to the default
// anyRequest().authenticated(), and every method is scoped to the caller's
// own id, never a path parameter, so there's nothing to authorize per-role.
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Page<NotificationResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return notificationService.listForUser(principal.getId(), pageable);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return new UnreadCountResponse(notificationService.unreadCount(principal.getId()));
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return notificationService.markAsRead(principal.getId(), id);
    }
}
