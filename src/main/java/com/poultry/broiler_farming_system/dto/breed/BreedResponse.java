package com.poultry.broiler_farming_system.dto.breed;

import com.poultry.broiler_farming_system.entity.enums.BreedStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BreedResponse(
        Long id,
        String name,
        String origin,
        BigDecimal avgMarketWeightKg,
        Integer growthPeriodDays,
        BigDecimal fcr,
        String description,
        String imageUrl,
        BreedStatus status,
        LocalDateTime createdDate
) {
}
