package com.poultry.broiler_farming_system.service.medicine;

import com.poultry.broiler_farming_system.dto.medicine.BatchAlarmResponse;
import com.poultry.broiler_farming_system.dto.medicine.CreateBatchAlarmRequest;

import java.util.List;

public interface BatchAlarmService {

    // Creates the batch_alarms row and schedules its recurring Quartz
    // trigger from cron_expression in one step.
    BatchAlarmResponse createAlarm(CreateBatchAlarmRequest request);

    // Cancels a still-pending alarm outright: unschedules it and deletes the
    // row. Rejects alarms that have already been completed (that history
    // lives on in batch_alarms/batch_expenses and shouldn't be deleted).
    void cancelAlarm(Long batchAlarmId);

    List<BatchAlarmResponse> listForBatch(Long batchId);
}
