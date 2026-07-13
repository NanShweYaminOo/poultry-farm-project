package com.poultry.broiler_farming_system.dto.medicine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BatchAlarmResponse(
        Long id,
        Long batchId,
        String medicineName,
        String cronExpression,
        LocalDateTime scheduledTime,
        BigDecimal calculatedRequirement,
        BigDecimal overriddenQuantity,
        BigDecimal finalCostIncurred,
        boolean isCompleted,
        LocalDateTime completedAt
) {
}
