package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.medicine.MedicineEstimateRequest;
import com.poultry.broiler_farming_system.dto.medicine.MedicineEstimateResponse;
import com.poultry.broiler_farming_system.dto.medicine.MedicineTaskCompletionRequest;
import com.poultry.broiler_farming_system.dto.medicine.MedicineTaskCompletionResponse;
import com.poultry.broiler_farming_system.service.medicine.MedicineEstimationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/medicine-estimation")
@RequiredArgsConstructor
public class MedicineEstimationController {

    private final MedicineEstimationService medicineEstimationService;

    // Ad-hoc calculator: e.g. { "batchId": 1, "medicineName": "Vitamin C", "userPricePerUnit": 15.00 }
    @PostMapping("/estimate")
    public MedicineEstimateResponse estimate(@RequestBody MedicineEstimateRequest request) {
        return medicineEstimationService.estimate(request);
    }

    // Pulled when a farmer opens a pending medicine alarm task.
    @PostMapping("/alarms/{batchAlarmId}/estimate")
    public MedicineEstimateResponse estimateForAlarm(
            @PathVariable Long batchAlarmId,
            @RequestParam(required = false) BigDecimal userPricePerUnit) {
        return medicineEstimationService.estimateForAlarm(batchAlarmId, userPricePerUnit);
    }

    // "Mark as Done" with the farmer's manually confirmed/overridden figures.
    @PostMapping("/alarms/complete")
    public MedicineTaskCompletionResponse completeTask(@RequestBody MedicineTaskCompletionRequest request) {
        return medicineEstimationService.completeTask(request);
    }
}
