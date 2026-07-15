package com.poultry.broiler_farming_system.service.medicine;

import com.poultry.broiler_farming_system.dto.medicine.MedicineEstimateRequest;
import com.poultry.broiler_farming_system.dto.medicine.MedicineEstimateResponse;
import com.poultry.broiler_farming_system.dto.medicine.MedicineTaskCompletionRequest;
import com.poultry.broiler_farming_system.dto.medicine.MedicineTaskCompletionResponse;

import java.math.BigDecimal;

public interface MedicineEstimationService {

    /**
     * Ad-hoc "what would this cost right now" calculation for a batch,
     * driven by its latest daily_logs entry and the Admin's dosage/price
     * formula. Not tied to any specific alarm and never persists anything.
     * callerId must own request.batchId() (or be an Admin).
     */
    MedicineEstimateResponse estimate(Long callerId, MedicineEstimateRequest request);

    /**
     * Fetches the latest remaining-chicken count and applies the Admin's
     * formula for a specific pending medicine alarm, recording the result
     * as that alarm's calculated_requirement so it's available when the
     * farmer opens the task. callerId must own the alarm's batch (or be an
     * Admin).
     */
    MedicineEstimateResponse estimateForAlarm(Long callerId, Long batchAlarmId, BigDecimal userPricePerUnit);

    /**
     * "Mark as Done": records the farmer's manually confirmed/overridden
     * quantity and cost on the batch_alarms row, and writes the matching
     * batch_expenses ledger entry. callerId must own the alarm's batch (or
     * be an Admin).
     */
    MedicineTaskCompletionResponse completeTask(Long callerId, MedicineTaskCompletionRequest request);
}
