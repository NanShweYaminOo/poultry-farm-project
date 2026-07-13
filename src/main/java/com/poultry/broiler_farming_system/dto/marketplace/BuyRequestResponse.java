package com.poultry.broiler_farming_system.dto.marketplace;

import java.time.LocalDateTime;

public record BuyRequestResponse(
        Long id,
        Long creatorId,
        String creatorUsername,
        String creatorProfileImageUrl,
        String title,
        String description,
        Integer quantity,
        LocalDateTime createdDate
) {
}
