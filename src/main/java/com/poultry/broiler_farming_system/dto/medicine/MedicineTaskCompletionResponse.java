package com.poultry.broiler_farming_system.dto.medicine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MedicineTaskCompletionResponse(
        Long batchAlarmId,
        Long batchExpenseId,
        String medicineName,
        BigDecimal calculatedRequirement,
        BigDecimal overriddenQuantity,
        BigDecimal finalCostIncurred,
        LocalDateTime completedAt
) {
}
