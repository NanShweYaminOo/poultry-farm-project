package com.poultry.broiler_farming_system.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryItemResponse(
        Long id,
        Long batchId,
        String itemName,
        String unit,
        BigDecimal quantityInStock,
        LocalDateTime updatedAt
) {
}
