package com.poultry.broiler_farming_system.dto.medicine;

import java.math.BigDecimal;

/**
 * Ad-hoc calculator request: estimate how much of a medicine is needed for a
 * batch right now, based on its latest daily log, optionally pricing it with
 * a farmer-supplied unit price instead of the Admin's default.
 */
public record MedicineEstimateRequest(
        Long batchId,
        String medicineName,
        BigDecimal userPricePerUnit
) {
}
