package com.poultry.broiler_farming_system.dto.medicine;

import java.math.BigDecimal;

/**
 * "Mark as Done" payload. overriddenQuantity/finalCostIncurred are always
 * required here — this is the farmer's explicit, manually-confirmed figure
 * (whether it matches the system estimate or corrects it), which is what
 * ultimately gets written to batch_expenses.
 */
public record MedicineTaskCompletionRequest(
        Long batchAlarmId,
        BigDecimal overriddenQuantity,
        BigDecimal finalCostIncurred,
        String notes
) {
}
