package com.poultry.broiler_farming_system.service.accountupgrade;

import com.poultry.broiler_farming_system.dto.accountupgrade.AccountUpgradeRequestResponse;
import com.poultry.broiler_farming_system.dto.accountupgrade.CreateAccountUpgradeRequestRequest;
import com.poultry.broiler_farming_system.dto.accountupgrade.ReviewAccountUpgradeRequestRequest;

import java.util.List;

public interface AccountUpgradeRequestService {

    // Guest submits a request to become a Farmer. Rejects if the caller is
    // already a Farmer, or already has a PENDING request. Always lands as
    // PENDING; review is separate.
    AccountUpgradeRequestResponse create(Long userId, CreateAccountUpgradeRequestRequest request);

    // The caller's own upgrade requests, most recent first.
    List<AccountUpgradeRequestResponse> listMine(Long userId);

    // Every upgrade request in the system, most recent first -- for the
    // admin review screen.
    List<AccountUpgradeRequestResponse> listForAdmin();

    // Admin approves or rejects a pending request. On approval, flips
    // requestedBy.accountType to FARMER. Rejection just records the review
    // with no further side effects.
    AccountUpgradeRequestResponse review(Long requestId, Long adminId, ReviewAccountUpgradeRequestRequest request);
}
