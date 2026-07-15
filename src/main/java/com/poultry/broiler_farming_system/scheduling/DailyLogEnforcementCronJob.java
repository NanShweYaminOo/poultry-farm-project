package com.poultry.broiler_farming_system.scheduling;

import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.enums.BatchStatus;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.repository.DailyLogRepository;
import com.poultry.broiler_farming_system.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Daily Log Enforcement Warning: runs every day at exactly 18:00. Scans
 * every active, started batch; any batch with no daily_logs row for today
 * gets an SMS warning sent to its farmer's stored phone_number.
 */
@Component
@RequiredArgsConstructor
public class DailyLogEnforcementCronJob {

    private static final Logger log = LoggerFactory.getLogger(DailyLogEnforcementCronJob.class);

    private final BatchRepository batchRepository;
    private final DailyLogRepository dailyLogRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 18 * * *")
    public void enforceDailyLogging() {
        LocalDate today = LocalDate.now();

        for (Batch batch : batchRepository.findByStatusAndIsStartedTrue(BatchStatus.ACTIVE)) {
            boolean loggedToday = dailyLogRepository.existsByBatchIdAndLogDate(batch.getId(), today);
            if (!loggedToday) {
                log.warn("Batch {} has no daily log for {}; notifying farmer + sending SMS warning.",
                        batch.getId(), today);
                // Persists an in-app notification synchronously and, since
                // this is the one trigger the spec explicitly requires SMS
                // for, also publishes an event that sends the SMS
                // asynchronously after commit -- see
                // NotificationServiceImpl.notifyDailyLogMissing and
                // SmsRequestedEventListener.
                notificationService.notifyDailyLogMissing(batch, today);
            }
        }
    }
}
