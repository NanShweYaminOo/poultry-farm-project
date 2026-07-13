package com.poultry.broiler_farming_system.dto.inventory;

import java.math.BigDecimal;

// unit is only applied when the item is created for the first time; an
// existing item keeps whatever unit it was first stocked with.
public record RestockRequest(
        Long batchId,
        String itemName,
        String unit,
        BigDecimal quantity
) {
}
