package com.poultry.broiler_farming_system.dto.disease;

import com.poultry.broiler_farming_system.entity.enums.DiseaseSeverity;

public record UpsertDiseaseRequest(
        String name,
        String keySymptoms,
        DiseaseSeverity severity,
        String notes
) {
}
