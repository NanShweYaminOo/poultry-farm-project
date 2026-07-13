package com.poultry.broiler_farming_system.service.marketplace;

import com.poultry.broiler_farming_system.dto.marketplace.CreateSalesPostRequest;
import com.poultry.broiler_farming_system.dto.marketplace.SalesPostResponse;

import java.util.List;

public interface SalesPostService {

    // Title + description are screened by ContentModerationService before
    // the post is created; a violation blocks the post and flags the
    // author's profile (isFlaggedForReview) rather than silently rejecting.
    SalesPostResponse createPost(Long creatorId, CreateSalesPostRequest request);

    List<SalesPostResponse> listPosts();
}
