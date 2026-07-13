package com.poultry.broiler_farming_system.scheduling;

import com.poultry.broiler_farming_system.dto.medicine.MedicineEstimateResponse;
import com.poultry.broiler_farming_system.entity.BatchAlarm;
import com.poultry.broiler_farming_system.repository.BatchAlarmRepository;
import com.poultry.broiler_farming_system.service.medicine.MedicineEstimationService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Fires on a BatchAlarm's cron schedule. Quartz instantiates this class
 * itself via reflection (no-arg constructor) rather than obtaining it from
 * the Spring container, so it is never AOP-proxied -- @Transactional here
 * would be silently ignored. Field injection (via AutowiringSpringBeanJobFactory,
 * see config.QuartzConfig) is used instead of constructor injection for the
 * same reason. Each call below into a real Spring-managed bean
 * (BatchAlarmRepository, MedicineEstimationService) still gets its own
 * proper transaction from that bean's own proxy.
 */
public class MedicineAlarmJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(MedicineAlarmJob.class);

    public static final String DATA_KEY_BATCH_ALARM_ID = "batchAlarmId";

    @Autowired
    private BatchAlarmRepository batchAlarmRepository;

    @Autowired
    private MedicineEstimationService medicineEstimationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long batchAlarmId = context.getJobDetail().getJobDataMap().getLong(DATA_KEY_BATCH_ALARM_ID);

        BatchAlarm alarm = batchAlarmRepository.findById(batchAlarmId).orElse(null);
        if (alarm == null) {
            log.warn("Medicine alarm {} no longer exists; skipping this firing.", batchAlarmId);
            return;
        }
        if (Boolean.TRUE.equals(alarm.getIsCompleted())) {
            log.info("Medicine alarm {} is already marked done; skipping this firing.", batchAlarmId);
            return;
        }

        try {
            MedicineEstimateResponse estimate = medicineEstimationService.estimateForAlarm(batchAlarmId, null);
            log.info("Medicine alarm due -- batch {}: '{}' needs ~{} {} (est. cost {}).",
                    estimate.batchId(), alarm.getMedicineName(), estimate.calculatedQuantity(),
                    estimate.unit(), estimate.estimatedCost());
        } catch (RuntimeException ex) {
            // A missing daily log or admin formula shouldn't kill the
            // trigger's schedule -- log it and let the next firing (or the
            // farmer opening the task manually) try again.
            log.error("Could not compute estimate for medicine alarm {}: {}", batchAlarmId, ex.getMessage());
        }

        Date nextFireTime = context.getNextFireTime();
        if (nextFireTime != null) {
            // Re-fetch rather than reuse `alarm`: estimateForAlarm() above
            // committed calculated_requirement in its own transaction, so
            // this earlier reference is now stale -- saving it directly
            // would silently overwrite that update via merge.
            batchAlarmRepository.findById(batchAlarmId).ifPresent(fresh -> {
                fresh.setScheduledTime(LocalDateTime.ofInstant(nextFireTime.toInstant(), ZoneId.systemDefault()));
                batchAlarmRepository.save(fresh);
            });
        }
    }
}
