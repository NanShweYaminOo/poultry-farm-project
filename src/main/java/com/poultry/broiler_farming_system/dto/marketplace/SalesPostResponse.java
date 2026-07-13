package com.poultry.broiler_farming_system.dto.marketplace;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalesPostResponse(
        Long id,
        Long creatorId,
        String creatorUsername,
        String creatorProfileImageUrl,
        String title,
        String description,
        BigDecimal price,
        LocalDateTime createdDate
) {
}
