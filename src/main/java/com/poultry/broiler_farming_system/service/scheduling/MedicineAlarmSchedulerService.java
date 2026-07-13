package com.poultry.broiler_farming_system.service.scheduling;

import com.poultry.broiler_farming_system.entity.BatchAlarm;

import java.time.LocalDateTime;

public interface MedicineAlarmSchedulerService {

    // Schedules (or reschedules, if already scheduled) the given alarm's
    // Quartz trigger from its cron_expression. Returns the computed next
    // fire time.
    LocalDateTime scheduleAlarm(BatchAlarm alarm);

    // Unschedules a single alarm's job/trigger. No-op if it was never
    // scheduled or already fired its last time. Called both when a farmer
    // completes a task (stop future firings) and when an alarm is cancelled
    // outright before ever completing.
    void cancelAlarm(Long batchAlarmId);

    // Unschedules every still-pending (not completed) alarm for a batch --
    // the hook a future "Stop Batch" action should call.
    void cancelAllForBatch(Long batchId);
}
