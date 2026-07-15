package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.accountupgrade.AccountUpgradeRequestResponse;
import com.poultry.broiler_farming_system.dto.accountupgrade.CreateAccountUpgradeRequestRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.accountupgrade.AccountUpgradeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Any authenticated Guest can file/view their own upgrade requests here.
// Admin-wide listing + review stays on AdminAccountUpgradeRequestController.
@RestController
@RequestMapping("/api/v1/account-upgrade-requests")
@RequiredArgsConstructor
public class AccountUpgradeRequestController {

    private final AccountUpgradeRequestService accountUpgradeRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountUpgradeRequestResponse create(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) CreateAccountUpgradeRequestRequest request) {
        return accountUpgradeRequestService.create(principal.getId(), request);
    }

    @GetMapping("/me")
    public List<AccountUpgradeRequestResponse> listMine(@AuthenticationPrincipal UserPrincipal principal) {
        return accountUpgradeRequestService.listMine(principal.getId());
    }
}
