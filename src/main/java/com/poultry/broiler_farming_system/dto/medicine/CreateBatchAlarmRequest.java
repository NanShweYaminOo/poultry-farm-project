package com.poultry.broiler_farming_system.dto.medicine;

public record CreateBatchAlarmRequest(
        Long batchId,
        String medicineName,
        String cronExpression
) {
}
