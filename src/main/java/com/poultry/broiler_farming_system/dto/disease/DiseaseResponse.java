package com.poultry.broiler_farming_system.dto.disease;

import com.poultry.broiler_farming_system.entity.enums.DiseaseSeverity;

import java.time.LocalDateTime;

public record DiseaseResponse(
        Long id,
        String name,
        String keySymptoms,
        DiseaseSeverity severity,
        String notes,
        String imageUrl,
        LocalDateTime createdDate
) {
}
