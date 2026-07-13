package com.poultry.broiler_farming_system.dto.medicine;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MedicineEstimateResponse(
        Long batchId,
        Long batchAlarmId,
        String medicineName,
        LocalDate logDate,
        Integer remainingChickenCount,
        BigDecimal dosagePerBird,
        String unit,
        BigDecimal calculatedQuantity,
        BigDecimal pricePerUnitUsed,
        boolean userPriceOverridden,
        BigDecimal estimatedCost
) {
}
