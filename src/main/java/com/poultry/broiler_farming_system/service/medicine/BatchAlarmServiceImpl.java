package com.poultry.broiler_farming_system.service.medicine;

import com.poultry.broiler_farming_system.dto.medicine.BatchAlarmResponse;
import com.poultry.broiler_farming_system.dto.medicine.CreateBatchAlarmRequest;
import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.BatchAlarm;
import com.poultry.broiler_farming_system.entity.enums.BatchStatus;
import com.poultry.broiler_farming_system.exception.InvalidAlarmStateException;
import com.poultry.broiler_farming_system.exception.InvalidBatchStateException;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.BatchAlarmRepository;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.service.scheduling.MedicineAlarmSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BatchAlarmServiceImpl implements BatchAlarmService {

    private final BatchRepository batchRepository;
    private final BatchAlarmRepository batchAlarmRepository;
    private final MedicineAlarmSchedulerService schedulerService;

    @Override
    public BatchAlarmResponse createAlarm(CreateBatchAlarmRequest request) {
        if (!StringUtils.hasText(request.medicineName())) {
            throw new IllegalArgumentException("medicineName is required.");
        }
        if (!StringUtils.hasText(request.cronExpression())) {
            throw new IllegalArgumentException("cronExpression is required.");
        }

        Batch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + request.batchId() + " was not found."));
        if (batch.getStatus() != BatchStatus.ACTIVE) {
            throw new InvalidBatchStateException(
                    "Batch " + batch.getId() + " is " + batch.getStatus() + " and is locked from further tracking; cannot schedule a new medicine alarm.");
        }

        BatchAlarm alarm = new BatchAlarm();
        alarm.setBatch(batch);
        alarm.setMedicineName(request.medicineName().trim());
        alarm.setCronExpression(request.cronExpression().trim());
        alarm.setIsCompleted(false);
        alarm = batchAlarmRepository.save(alarm);

        // scheduleAlarm validates the cron expression itself; if it's
        // invalid this throws and the alarm insert above rolls back too.
        LocalDateTime nextFireTime = schedulerService.scheduleAlarm(alarm);
        alarm.setScheduledTime(nextFireTime);
        alarm = batchAlarmRepository.save(alarm);

        return toResponse(alarm);
    }

    @Override
    public void cancelAlarm(Long batchAlarmId) {
        BatchAlarm alarm = batchAlarmRepository.findById(batchAlarmId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch alarm " + batchAlarmId + " was not found."));
        if (Boolean.TRUE.equals(alarm.getIsCompleted())) {
            throw new InvalidAlarmStateException(
                    "Medicine task " + batchAlarmId + " has already been completed and cannot be cancelled.");
        }
        schedulerService.cancelAlarm(batchAlarmId);
        batchAlarmRepository.delete(alarm);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchAlarmResponse> listForBatch(Long batchId) {
        return batchAlarmRepository.findByBatchIdOrderByScheduledTimeAsc(batchId).stream()
                .map(this::toResponse)
                .toList();
    }

    private BatchAlarmResponse toResponse(BatchAlarm alarm) {
        return new BatchAlarmResponse(
                alarm.getId(),
                alarm.getBatch().getId(),
                alarm.getMedicineName(),
                alarm.getCronExpression(),
                alarm.getScheduledTime(),
                alarm.getCalculatedRequirement(),
                alarm.getOverriddenQuantity(),
                alarm.getFinalCostIncurred(),
                Boolean.TRUE.equals(alarm.getIsCompleted()),
                alarm.getCompletedAt()
        );
    }
}
