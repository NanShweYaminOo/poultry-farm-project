package com.poultry.broiler_farming_system.service.marketplace;

import com.poultry.broiler_farming_system.dto.marketplace.BuyRequestResponse;
import com.poultry.broiler_farming_system.dto.marketplace.CreateBuyRequestRequest;

import java.util.List;

public interface BuyRequestService {

    // Free for any authenticated user, per spec. Title + description are
    // screened by ContentModerationService before creation.
    BuyRequestResponse createRequest(Long creatorId, CreateBuyRequestRequest request);

    List<BuyRequestResponse> listRequests();
}
