package com.poultry.broiler_farming_system.scheduling;

import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.BatchStatus;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.batch.BatchService;
import com.poultry.broiler_farming_system.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Daily Expiry & Warning Job: runs at midnight every day.
 *  - Evicts batches whose farming cycle (start_date + cycle_duration_days)
 *    has ended, by auto-completing them through the same BatchService path
 *    "Stop Batch" uses (cancels pending Quartz alarms, locks tracking).
 *  - Warns farmers 3 days ahead of a batch cycle ending.
 *  - Warns users 3 days ahead of their posting_extension_expiry.
 * A real Spring bean (not Quartz-instantiated), so plain constructor
 * injection works here -- unlike MedicineAlarmJob, this doesn't need
 * AutowiringSpringBeanJobFactory's field-injection workaround.
 */
@Component
@RequiredArgsConstructor
public class PremiumExpiryCronJob {

    private static final Logger log = LoggerFactory.getLogger(PremiumExpiryCronJob.class);
    private static final int WARNING_DAYS_AHEAD = 3;

    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final BatchService batchService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyExpiryScan() {
        LocalDate today = LocalDate.now();
        List<Batch> activeStartedBatches = batchRepository.findByStatusAndIsStartedTrue(BatchStatus.ACTIVE);

        evictBatchesPastCycleEnd(activeStartedBatches, today);
        warnBatchesNearingCycleEnd(activeStartedBatches, today);
        warnPostingExtensionsNearingExpiry(today);
    }

    private void evictBatchesPastCycleEnd(List<Batch> batches, LocalDate today) {
        for (Batch batch : batches) {
            LocalDate cycleEndDate = cycleEndDate(batch);
            if (cycleEndDate != null && !today.isBefore(cycleEndDate)) {
                log.info("Batch {} reached the end of its {}-day cycle (started {}); auto-completing.",
                        batch.getId(), batch.getCycleDurationDays(), batch.getStartDate());
                batchService.stopBatch(batch.getId(), BatchStatus.COMPLETED);
            }
        }
    }

    private void warnBatchesNearingCycleEnd(List<Batch> batches, LocalDate today) {
        LocalDate warningTargetDate = today.plusDays(WARNING_DAYS_AHEAD);
        for (Batch batch : batches) {
            LocalDate cycleEndDate = cycleEndDate(batch);
            if (cycleEndDate != null && cycleEndDate.equals(warningTargetDate)) {
                notificationService.notify(batch.getFarmer(),
                        "Your batch '" + batch.getBatchName() + "' cycle ends in " + WARNING_DAYS_AHEAD
                                + " days (" + cycleEndDate + ").");
            }
        }
    }

    private void warnPostingExtensionsNearingExpiry(LocalDate today) {
        LocalDate warningTargetDate = today.plusDays(WARNING_DAYS_AHEAD);
        List<User> usersWithExtension = userRepository.findByPostingExtensionExpiryIsNotNull();
        for (User user : usersWithExtension) {
            LocalDate expiryDate = user.getPostingExtensionExpiry().toLocalDate();
            if (expiryDate.equals(warningTargetDate)) {
                notificationService.notify(user,
                        "Your posting privilege expires in " + WARNING_DAYS_AHEAD + " days (" + expiryDate + ").");
            }
        }
        // No state to evict here: posting_extension_expiry is checked
        // directly against now() wherever posting privilege is enforced, so
        // its own passage is the eviction -- there's no separate flag to clear.
    }

    private LocalDate cycleEndDate(Batch batch) {
        if (batch.getStartDate() == null || batch.getCycleDurationDays() == null) {
            return null;
        }
        return batch.getStartDate().plusDays(batch.getCycleDurationDays());
    }
}
