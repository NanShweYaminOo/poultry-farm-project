package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.inventory.InventoryItemResponse;
import com.poultry.broiler_farming_system.dto.inventory.RestockRequest;
import com.poultry.broiler_farming_system.entity.InventoryItem;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/restock")
    public InventoryItemResponse restock(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody RestockRequest request) {
        InventoryItem item = inventoryService.restock(
                principal.getId(), request.batchId(), request.itemName(), request.unit(), request.quantity());
        return toResponse(item);
    }

    @GetMapping("/batches/{batchId}")
    public List<InventoryItemResponse> listForBatch(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long batchId) {
        return inventoryService.listForBatch(principal.getId(), batchId).stream().map(this::toResponse).toList();
    }

    private InventoryItemResponse toResponse(InventoryItem item) {
        return new InventoryItemResponse(
                item.getId(),
                item.getBatch().getId(),
                item.getItemName(),
                item.getUnit(),
                item.getQuantityInStock(),
                item.getUpdatedAt()
        );
    }
}
