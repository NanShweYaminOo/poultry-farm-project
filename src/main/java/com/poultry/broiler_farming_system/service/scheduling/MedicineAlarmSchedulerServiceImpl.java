package com.poultry.broiler_farming_system.service.scheduling;

import com.poultry.broiler_farming_system.entity.BatchAlarm;
import com.poultry.broiler_farming_system.exception.SchedulingException;
import com.poultry.broiler_farming_system.repository.BatchAlarmRepository;
import com.poultry.broiler_farming_system.scheduling.MedicineAlarmJob;
import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.quartz.JobBuilder.newJob;

@Service
@RequiredArgsConstructor
public class MedicineAlarmSchedulerServiceImpl implements MedicineAlarmSchedulerService {

    private static final String JOB_GROUP = "medicine-alarms";
    private static final String TRIGGER_GROUP = "medicine-alarms";

    private final Scheduler scheduler;
    private final BatchAlarmRepository batchAlarmRepository;

    @Override
    public LocalDateTime scheduleAlarm(BatchAlarm alarm) {
        String cronExpression = alarm.getCronExpression();
        if (!StringUtils.hasText(cronExpression) || !CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException(
                    "'" + cronExpression + "' is not a valid cron expression for batch alarm " + alarm.getId() + ".");
        }

        JobKey jobKey = jobKey(alarm.getId());
        try {
            // Idempotent: deleting a non-existent job is a no-op, so this
            // handles both first-time scheduling and rescheduling the same way.
            scheduler.deleteJob(jobKey);

            JobDetail jobDetail = newJob(MedicineAlarmJob.class)
                    .withIdentity(jobKey)
                    .usingJobData(MedicineAlarmJob.DATA_KEY_BATCH_ALARM_ID, alarm.getId())
                    .storeDurably(false)
                    .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey(alarm.getId()))
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            var nextFireTime = scheduler.scheduleJob(jobDetail, trigger);
            return LocalDateTime.ofInstant(nextFireTime.toInstant(), ZoneId.systemDefault());
        } catch (SchedulerException ex) {
            throw new SchedulingException("Failed to schedule medicine alarm " + alarm.getId() + ".", ex);
        }
    }

    @Override
    public void cancelAlarm(Long batchAlarmId) {
        try {
            scheduler.deleteJob(jobKey(batchAlarmId));
        } catch (SchedulerException ex) {
            throw new SchedulingException("Failed to cancel medicine alarm " + batchAlarmId + ".", ex);
        }
    }

    @Override
    public void cancelAllForBatch(Long batchId) {
        List<BatchAlarm> pending = batchAlarmRepository.findByBatchIdAndIsCompletedFalse(batchId);
        pending.forEach(alarm -> cancelAlarm(alarm.getId()));
    }

    private JobKey jobKey(Long batchAlarmId) {
        return new JobKey("alarm-" + batchAlarmId, JOB_GROUP);
    }

    private TriggerKey triggerKey(Long batchAlarmId) {
        return new TriggerKey("alarm-" + batchAlarmId + "-trigger", TRIGGER_GROUP);
    }
}
