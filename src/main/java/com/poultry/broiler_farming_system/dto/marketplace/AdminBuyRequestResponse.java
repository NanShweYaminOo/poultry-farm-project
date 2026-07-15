package com.poultry.broiler_farming_system.dto.marketplace;

import java.time.LocalDateTime;

public record AdminBuyRequestResponse(
        Long id,
        Long creatorId,
        String creatorUsername,
        Boolean creatorIsBanned,
        Boolean creatorIsFlaggedForReview,
        String title,
        String description,
        Integer quantity,
        LocalDateTime createdDate
) {
}
