package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.accountupgrade.AccountUpgradeRequestResponse;
import com.poultry.broiler_farming_system.dto.accountupgrade.ReviewAccountUpgradeRequestRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.accountupgrade.AccountUpgradeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Admin-only review of Guest -> Farmer upgrade requests. Access is
// restricted to ROLE_ADMIN by the existing "/api/v1/admin/**" rule in
// SecurityConfig.
@RestController
@RequestMapping("/api/v1/admin/account-upgrade-requests")
@RequiredArgsConstructor
public class AdminAccountUpgradeRequestController {

    private final AccountUpgradeRequestService accountUpgradeRequestService;

    @GetMapping
    public List<AccountUpgradeRequestResponse> listAll() {
        return accountUpgradeRequestService.listForAdmin();
    }

    // e.g. { "decision": "APPROVED" } or { "decision": "REJECTED", "adminNote": "..." }
    @PostMapping("/{requestId}/review")
    public AccountUpgradeRequestResponse review(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId,
            @RequestBody ReviewAccountUpgradeRequestRequest request) {
        return accountUpgradeRequestService.review(requestId, principal.getId(), request);
    }
}
