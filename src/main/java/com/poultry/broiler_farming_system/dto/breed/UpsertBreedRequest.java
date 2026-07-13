package com.poultry.broiler_farming_system.dto.breed;

import com.poultry.broiler_farming_system.entity.enums.BreedStatus;

import java.math.BigDecimal;

// Bound via @ModelAttribute from multipart/form-data fields (sent alongside
// the image file), not a JSON @RequestBody -- a request can't mix a JSON body
// with a file part.
public record UpsertBreedRequest(
        String name,
        String origin,
        BigDecimal avgMarketWeightKg,
        Integer growthPeriodDays,
        BigDecimal fcr,
        String description,
        BreedStatus status
) {
}
