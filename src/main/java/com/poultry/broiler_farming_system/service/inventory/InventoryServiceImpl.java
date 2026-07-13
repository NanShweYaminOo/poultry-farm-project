package com.poultry.broiler_farming_system.service.inventory;

import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.InventoryItem;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final int QUANTITY_SCALE = 2;
    private static final String DEFAULT_UNIT = "unit";

    private final InventoryItemRepository inventoryItemRepository;
    private final BatchRepository batchRepository;

    @Override
    public InventoryItem restock(Long batchId, String itemName, String unit, BigDecimal quantityToAdd) {
        InventoryItem item = getOrCreate(batchId, itemName, unit);
        BigDecimal amount = requirePositive(quantityToAdd, "quantity");
        item.setQuantityInStock(item.getQuantityInStock().add(amount).setScale(QUANTITY_SCALE, RoundingMode.HALF_UP));
        return inventoryItemRepository.save(item);
    }

    @Override
    public InventoryItem deduct(Long batchId, String itemName, BigDecimal quantityToDeduct) {
        InventoryItem item = getOrCreate(batchId, itemName, null);
        BigDecimal amount = requirePositive(quantityToDeduct, "quantity");
        item.setQuantityInStock(item.getQuantityInStock().subtract(amount).setScale(QUANTITY_SCALE, RoundingMode.HALF_UP));
        return inventoryItemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> listForBatch(Long batchId) {
        return inventoryItemRepository.findByBatchIdOrderByItemNameAsc(batchId);
    }

    private InventoryItem getOrCreate(Long batchId, String itemName, String unitForCreation) {
        requireItemName(itemName);
        return inventoryItemRepository.findByBatchIdAndItemNameIgnoreCase(batchId, itemName)
                .orElseGet(() -> {
                    InventoryItem created = new InventoryItem();
                    created.setBatch(getBatch(batchId));
                    created.setItemName(itemName.trim());
                    created.setUnit(StringUtils.hasText(unitForCreation) ? unitForCreation.trim() : DEFAULT_UNIT);
                    created.setQuantityInStock(BigDecimal.ZERO);
                    return created;
                });
    }

    private Batch getBatch(Long batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + batchId + " was not found."));
    }

    private void requireItemName(String itemName) {
        if (!StringUtils.hasText(itemName)) {
            throw new IllegalArgumentException("itemName is required.");
        }
    }

    private BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
        return value;
    }
}
