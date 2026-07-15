package com.poultry.broiler_farming_system.service.marketplace;

import com.poultry.broiler_farming_system.dto.marketplace.AdminBuyRequestResponse;
import com.poultry.broiler_farming_system.dto.marketplace.BuyRequestResponse;
import com.poultry.broiler_farming_system.dto.marketplace.CreateBuyRequestRequest;

import java.util.List;

public interface BuyRequestService {

    // Free for any authenticated user, per spec. Title + description are
    // screened by ContentModerationService before creation.
    BuyRequestResponse createRequest(Long creatorId, CreateBuyRequestRequest request);

    List<BuyRequestResponse> listRequests();

    // Admin-only moderation view, newest first, with the author's
    // flagged/banned status visible.
    List<AdminBuyRequestResponse> listAllForAdmin();

    // Admin override delete -- no ownership check.
    void deleteByAdmin(Long adminId, Long requestId, String reason);
}
