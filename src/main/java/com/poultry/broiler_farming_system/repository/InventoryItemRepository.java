package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByBatchIdAndItemNameIgnoreCase(Long batchId, String itemName);

    List<InventoryItem> findByBatchIdOrderByItemNameAsc(Long batchId);
}
