package com.poultry.broiler_farming_system.dto.batch;

import com.poultry.broiler_farming_system.entity.enums.BatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BatchResponse(
        Long id,
        Long farmerId,
        String batchName,
        Integer initialChickenCount,
        Integer cycleDurationDays,
        Boolean isStarted,
        LocalDate startDate,
        BatchStatus status,
        LocalDateTime createdDate
) {
}
