package com.poultry.broiler_farming_system.service.batch;

import com.poultry.broiler_farming_system.dto.batch.BatchResponse;
import com.poultry.broiler_farming_system.dto.batch.CreateBatchRequest;
import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.SystemConfiguration;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.BatchStatus;
import com.poultry.broiler_farming_system.exception.InvalidBatchStateException;
import com.poultry.broiler_farming_system.exception.MissingSystemConfigurationException;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.repository.SystemConfigurationRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.scheduling.MedicineAlarmSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class BatchServiceImpl implements BatchService {

    private static final Set<BatchStatus> VALID_FINAL_STATUSES = EnumSet.of(BatchStatus.COMPLETED, BatchStatus.CANCELLED);
    private static final String DEFAULT_CYCLE_DURATION_DAYS_KEY = "default_farming_cycle_duration_days";

    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final MedicineAlarmSchedulerService schedulerService;

    @Override
    public BatchResponse createBatch(Long farmerId, CreateBatchRequest request) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + farmerId + " was not found."));

        if (request.initialChickenCount() != null && request.initialChickenCount() <= 0) {
            throw new IllegalArgumentException("initialChickenCount must be greater than zero when provided.");
        }

        Integer cycleDurationDays = request.cycleDurationDays();
        if (cycleDurationDays == null) {
            cycleDurationDays = readDefaultCycleDurationDays();
        } else if (cycleDurationDays <= 0) {
            throw new IllegalArgumentException("cycleDurationDays must be greater than zero when provided.");
        }

        Batch batch = new Batch();
        batch.setFarmer(farmer);
        batch.setBatchName(request.batchName());
        batch.setInitialChickenCount(request.initialChickenCount());
        batch.setCycleDurationDays(cycleDurationDays);
        // isStarted=false, status=ACTIVE, adminApprovedAt=null via entity defaults --
        // "ACTIVE" here just means "not yet stopped", not "currently running";
        // startBatch() still gates on admin_approved_at before the cycle can begin.
        Batch saved = batchRepository.save(batch);

        return toResponse(saved);
    }

    private int readDefaultCycleDurationDays() {
        String raw = systemConfigurationRepository.findByConfigKey(DEFAULT_CYCLE_DURATION_DAYS_KEY)
                .map(SystemConfiguration::getConfigValue)
                .orElseThrow(() -> new MissingSystemConfigurationException(
                        "No cycleDurationDays was provided, and the Admin has not configured '"
                                + DEFAULT_CYCLE_DURATION_DAYS_KEY + "' yet."));
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new MissingSystemConfigurationException(
                    "Configured value for '" + DEFAULT_CYCLE_DURATION_DAYS_KEY + "' is not a valid integer: '" + raw + "'.");
        }
    }

    @Override
    public BatchResponse startBatch(Long batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + batchId + " was not found."));

        if (Boolean.TRUE.equals(batch.getIsStarted())) {
            throw new InvalidBatchStateException("Batch " + batchId + " has already been started.");
        }
        if (batch.getAdminApprovedAt() == null) {
            throw new InvalidBatchStateException(
                    "Batch " + batchId + " has not been approved by an admin yet; cannot start.");
        }
        if (batch.getStatus() != BatchStatus.ACTIVE) {
            throw new InvalidBatchStateException(
                    "Batch " + batchId + " is " + batch.getStatus() + " and cannot be started.");
        }

        // The farming cycle duration is computed from this moment, not from
        // whenever the admin approved the payment.
        batch.setIsStarted(true);
        batch.setStartDate(LocalDate.now());
        batch.setStatus(BatchStatus.ACTIVE);
        Batch saved = batchRepository.save(batch);

        return toResponse(saved);
    }

    @Override
    public BatchResponse stopBatch(Long batchId, BatchStatus finalStatus) {
        if (finalStatus == null || !VALID_FINAL_STATUSES.contains(finalStatus)) {
            throw new IllegalArgumentException("finalStatus must be COMPLETED or CANCELLED.");
        }

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + batchId + " was not found."));

        if (!Boolean.TRUE.equals(batch.getIsStarted())) {
            throw new InvalidBatchStateException(
                    "Batch " + batchId + " has not been started yet; there is nothing to stop.");
        }
        if (batch.getStatus() != BatchStatus.ACTIVE) {
            throw new InvalidBatchStateException(
                    "Batch " + batchId + " is already " + batch.getStatus() + " and cannot be stopped again.");
        }

        // Instantly evict every pending Quartz medicine alarm for this batch
        // before the status flips, so nothing can fire in the gap.
        schedulerService.cancelAllForBatch(batchId);

        batch.setStatus(finalStatus);
        Batch saved = batchRepository.save(batch);

        return toResponse(saved);
    }

    private BatchResponse toResponse(Batch batch) {
        return new BatchResponse(
                batch.getId(),
                batch.getFarmer().getId(),
                batch.getBatchName(),
                batch.getInitialChickenCount(),
                batch.getCycleDurationDays(),
                batch.getIsStarted(),
                batch.getStartDate(),
                batch.getStatus(),
                batch.getCreatedDate()
        );
    }
}
