package com.poultry.broiler_farming_system.service.inventory;

import com.poultry.broiler_farming_system.entity.InventoryItem;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {

    // Adds stock, creating the (batch, itemName) row on first use.
    InventoryItem restock(Long batchId, String itemName, String unit, BigDecimal quantityToAdd);

    /**
     * Subtracts usage from the batch's running stock for that item. If no
     * row exists yet the item is created starting at zero, so usage is
     * always recorded even when nothing was formally stocked in first -- the
     * quantity is then allowed to go negative as a visible "needs restock"
     * signal rather than blocking the (already-happened) real-world action
     * that triggered the deduction.
     */
    InventoryItem deduct(Long batchId, String itemName, BigDecimal quantityToDeduct);

    List<InventoryItem> listForBatch(Long batchId);
}
