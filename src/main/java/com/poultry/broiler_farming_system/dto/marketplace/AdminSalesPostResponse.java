package com.poultry.broiler_farming_system.dto.marketplace;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminSalesPostResponse(
        Long id,
        Long creatorId,
        String creatorUsername,
        Boolean creatorIsBanned,
        Boolean creatorIsFlaggedForReview,
        String title,
        String description,
        BigDecimal price,
        LocalDateTime createdDate
) {
}
