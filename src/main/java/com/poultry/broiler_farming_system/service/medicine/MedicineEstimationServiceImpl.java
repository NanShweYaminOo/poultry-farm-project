package com.poultry.broiler_farming_system.service.medicine;

import com.poultry.broiler_farming_system.dto.medicine.MedicineEstimateRequest;
import com.poultry.broiler_farming_system.dto.medicine.MedicineEstimateResponse;
import com.poultry.broiler_farming_system.dto.medicine.MedicineTaskCompletionRequest;
import com.poultry.broiler_farming_system.dto.medicine.MedicineTaskCompletionResponse;
import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.BatchAlarm;
import com.poultry.broiler_farming_system.entity.BatchExpense;
import com.poultry.broiler_farming_system.entity.DailyLog;
import com.poultry.broiler_farming_system.entity.SystemConfiguration;
import com.poultry.broiler_farming_system.entity.enums.BatchStatus;
import com.poultry.broiler_farming_system.exception.InvalidAlarmStateException;
import com.poultry.broiler_farming_system.exception.InvalidBatchStateException;
import com.poultry.broiler_farming_system.exception.MissingSystemConfigurationException;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.BatchAlarmRepository;
import com.poultry.broiler_farming_system.repository.BatchExpenseRepository;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.repository.DailyLogRepository;
import com.poultry.broiler_farming_system.repository.SystemConfigurationRepository;
import com.poultry.broiler_farming_system.service.inventory.InventoryService;
import com.poultry.broiler_farming_system.service.moderation.ContentModerationService;
import com.poultry.broiler_farming_system.service.scheduling.MedicineAlarmSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineEstimationServiceImpl implements MedicineEstimationService {

    private static final int QUANTITY_SCALE = 2;
    private static final int COST_SCALE = 2;
    private static final Pattern NON_ALNUM = Pattern.compile("[^A-Z0-9]+");

    private final BatchRepository batchRepository;
    private final BatchAlarmRepository batchAlarmRepository;
    private final BatchExpenseRepository batchExpenseRepository;
    private final DailyLogRepository dailyLogRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final ContentModerationService contentModerationService;
    private final InventoryService inventoryService;
    private final MedicineAlarmSchedulerService schedulerService;

    @Override
    public MedicineEstimateResponse estimate(MedicineEstimateRequest request) {
        requireMedicineName(request.medicineName());
        validatePositiveIfPresent(request.userPricePerUnit(), "userPricePerUnit");
        Batch batch = getBatch(request.batchId());
        requireActiveBatch(batch);
        return buildEstimate(batch, null, request.medicineName(), request.userPricePerUnit());
    }

    @Override
    @Transactional
    public MedicineEstimateResponse estimateForAlarm(Long batchAlarmId, BigDecimal userPricePerUnit) {
        validatePositiveIfPresent(userPricePerUnit, "userPricePerUnit");
        BatchAlarm alarm = getAlarm(batchAlarmId);
        ensureNotCompleted(alarm);
        requireActiveBatch(alarm.getBatch());

        MedicineEstimateResponse response =
                buildEstimate(alarm.getBatch(), alarm.getId(), alarm.getMedicineName(), userPricePerUnit);
        alarm.setCalculatedRequirement(response.calculatedQuantity());
        return response;
    }

    @Override
    @Transactional
    public MedicineTaskCompletionResponse completeTask(MedicineTaskCompletionRequest request) {
        BatchAlarm alarm = getAlarm(request.batchAlarmId());
        ensureNotCompleted(alarm);
        requireActiveBatch(alarm.getBatch());

        BigDecimal overriddenQuantity = requirePositive(request.overriddenQuantity(), "overriddenQuantity");
        BigDecimal finalCost = requirePositive(request.finalCostIncurred(), "finalCostIncurred");

        if (alarm.getCalculatedRequirement() == null) {
            // The farmer jumped straight to "Mark as Done" without ever pulling
            // an estimate for this task; compute the system baseline now so
            // calculated_requirement still reflects the latest daily log at
            // completion time, for audit comparison against the override.
            MedicineEstimateResponse baseline =
                    buildEstimate(alarm.getBatch(), alarm.getId(), alarm.getMedicineName(), null);
            alarm.setCalculatedRequirement(baseline.calculatedQuantity());
        }

        String notes = request.notes();
        if (StringUtils.hasText(notes)) {
            contentModerationService.moderate(alarm.getBatch().getFarmer(), notes);
        }

        alarm.setOverriddenQuantity(overriddenQuantity);
        alarm.setFinalCostIncurred(finalCost);
        alarm.setIsCompleted(true);
        alarm.setCompletedAt(LocalDateTime.now());

        // Deduct exactly what the farmer confirmed was actually used -- not
        // the system's original calculated_requirement -- from that batch's
        // tracked stock for this medicine.
        inventoryService.deduct(alarm.getBatch().getId(), alarm.getMedicineName(), overriddenQuantity);

        // Task is done -- stop the recurring cron trigger from firing again.
        schedulerService.cancelAlarm(alarm.getId());

        BatchExpense expense = batchExpenseRepository.findByBatchAlarmId(alarm.getId())
                .orElseGet(() -> {
                    BatchExpense created = new BatchExpense();
                    created.setBatch(alarm.getBatch());
                    created.setBatchAlarm(alarm);
                    return created;
                });
        expense.setDescription(StringUtils.hasText(notes)
                ? alarm.getMedicineName() + " - " + notes.trim()
                : alarm.getMedicineName());
        expense.setAmount(finalCost);
        expense.setExpenseDate(LocalDateTime.now());
        BatchExpense savedExpense = batchExpenseRepository.save(expense);

        return new MedicineTaskCompletionResponse(
                alarm.getId(),
                savedExpense.getId(),
                alarm.getMedicineName(),
                alarm.getCalculatedRequirement(),
                alarm.getOverriddenQuantity(),
                alarm.getFinalCostIncurred(),
                alarm.getCompletedAt()
        );
    }

    // ---- calculation core -------------------------------------------------

    private MedicineEstimateResponse buildEstimate(
            Batch batch, Long batchAlarmId, String medicineName, BigDecimal userPricePerUnit) {
        DailyLog latestLog = getLatestDailyLog(batch.getId());
        AdminFormula formula = resolveFormula(medicineName);

        BigDecimal remaining = BigDecimal.valueOf(latestLog.getTotalRemainingChickenCount());
        BigDecimal calculatedQuantity = formula.dosagePerBird()
                .multiply(remaining)
                .setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);

        boolean userPriceOverridden = userPricePerUnit != null;
        BigDecimal effectivePrice = userPriceOverridden ? userPricePerUnit : formula.defaultPricePerUnit();

        BigDecimal estimatedCost = calculatedQuantity
                .multiply(effectivePrice)
                .setScale(COST_SCALE, RoundingMode.HALF_UP);

        return new MedicineEstimateResponse(
                batch.getId(),
                batchAlarmId,
                medicineName,
                latestLog.getLogDate(),
                latestLog.getTotalRemainingChickenCount(),
                formula.dosagePerBird(),
                formula.unit(),
                calculatedQuantity,
                effectivePrice,
                userPriceOverridden,
                estimatedCost
        );
    }

    private record AdminFormula(BigDecimal dosagePerBird, String unit, BigDecimal defaultPricePerUnit) {
    }

    private AdminFormula resolveFormula(String medicineName) {
        String key = normalizeKey(medicineName);
        BigDecimal dosagePerBird = readDecimalConfig("medicine." + key + ".dosage_per_bird");
        String unit = readStringConfig("medicine." + key + ".unit");
        BigDecimal defaultPricePerUnit = readDecimalConfig("medicine." + key + ".default_price_per_unit");
        return new AdminFormula(dosagePerBird, unit, defaultPricePerUnit);
    }

    private String normalizeKey(String medicineName) {
        return NON_ALNUM.matcher(medicineName.trim().toUpperCase(Locale.ROOT)).replaceAll("_");
    }

    private BigDecimal readDecimalConfig(String configKey) {
        String raw = readStringConfig(configKey);
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            throw new MissingSystemConfigurationException(
                    "Configured value for '" + configKey + "' is not a valid number: '" + raw + "'.");
        }
    }

    private String readStringConfig(String configKey) {
        return systemConfigurationRepository.findByConfigKey(configKey)
                .map(SystemConfiguration::getConfigValue)
                .orElseThrow(() -> new MissingSystemConfigurationException(
                        "Admin has not configured '" + configKey
                                + "' yet. Set this value in System Configurations before estimating this medicine."));
    }

    // ---- lookups & validation ----------------------------------------------

    private Batch getBatch(Long batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + batchId + " was not found."));
    }

    private BatchAlarm getAlarm(Long batchAlarmId) {
        return batchAlarmRepository.findById(batchAlarmId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch alarm " + batchAlarmId + " was not found."));
    }

    private DailyLog getLatestDailyLog(Long batchId) {
        return dailyLogRepository.findTopByBatchIdOrderByLogDateDesc(batchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No daily log has been recorded yet for batch " + batchId
                                + "; log today's remaining chicken count before estimating."));
    }

    private void requireActiveBatch(Batch batch) {
        if (batch.getStatus() != BatchStatus.ACTIVE) {
            throw new InvalidBatchStateException(
                    "Batch " + batch.getId() + " is " + batch.getStatus()
                            + " and is locked from further tracking.");
        }
    }

    private void ensureNotCompleted(BatchAlarm alarm) {
        if (Boolean.TRUE.equals(alarm.getIsCompleted())) {
            throw new InvalidAlarmStateException(
                    "Medicine task " + alarm.getId() + " has already been marked as done.");
        }
    }

    private void requireMedicineName(String medicineName) {
        if (!StringUtils.hasText(medicineName)) {
            throw new IllegalArgumentException("medicineName is required.");
        }
    }

    private void validatePositiveIfPresent(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero when provided.");
        }
    }

    private BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
        return value;
    }
}
