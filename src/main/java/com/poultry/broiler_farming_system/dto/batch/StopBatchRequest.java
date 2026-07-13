package com.poultry.broiler_farming_system.dto.batch;

import com.poultry.broiler_farming_system.entity.enums.BatchStatus;

// finalStatus must be COMPLETED (successful early finish) or CANCELLED
// (terminated); ACTIVE is rejected since that isn't a "stop".
public record StopBatchRequest(BatchStatus finalStatus) {
}
