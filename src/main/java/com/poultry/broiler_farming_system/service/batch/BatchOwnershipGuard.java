package com.poultry.broiler_farming_system.service.batch;

import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Single choke point for "does this caller own this batch". Role-based
// security (PAID/ADMIN) only proves *a* farmer is calling, not that this
// batch is theirs -- every batch-scoped operation (BatchService,
// BatchAlarmService, InventoryService, MedicineEstimationService) must call
// this before acting on a batchId taken from the request. ADMIN bypasses
// ownership entirely.
@Component
@RequiredArgsConstructor
public class BatchOwnershipGuard {

    private final UserRepository userRepository;

    public void requireOwnership(Long callerId, Batch batch) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + callerId + " was not found."));
        if (caller.getRole() == UserRole.ADMIN) {
            return;
        }
        if (!batch.getFarmer().getId().equals(caller.getId())) {
            throw new UnauthorizedActionException(
                    "User " + caller.getId() + " does not own batch " + batch.getId() + ".");
        }
    }
}
