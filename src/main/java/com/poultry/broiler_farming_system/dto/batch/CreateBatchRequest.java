package com.poultry.broiler_farming_system.dto.batch;

// farmerId is NOT a field here -- it's the authenticated caller, taken from
// the security context in BatchController, not from client input. Letting
// the client name an arbitrary farmerId would let anyone create batches
// "as" someone else.
// cycleDurationDays is optional -- if omitted, falls back to the Admin's
// configured default_farming_cycle_duration_days (per-breed override still
// possible by passing it explicitly).
public record CreateBatchRequest(
        String batchName,
        Integer initialChickenCount,
        Integer cycleDurationDays
) {
}
