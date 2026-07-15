package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.medicine.MedicineEstimateRequest;
import com.poultry.broiler_farming_system.dto.medicine.MedicineEstimateResponse;
import com.poultry.broiler_farming_system.dto.medicine.MedicineTaskCompletionRequest;
import com.poultry.broiler_farming_system.dto.medicine.MedicineTaskCompletionResponse;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.medicine.MedicineEstimationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/medicine-estimation")
@RequiredArgsConstructor
public class MedicineEstimationController {

    private final MedicineEstimationService medicineEstimationService;

    // Ad-hoc calculator: e.g. { "batchId": 1, "medicineName": "Vitamin C", "userPricePerUnit": 15.00 }
    @PostMapping("/estimate")
    public MedicineEstimateResponse estimate(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody MedicineEstimateRequest request) {
        return medicineEstimationService.estimate(principal.getId(), request);
    }

    // Pulled when a farmer opens a pending medicine alarm task.
    @PostMapping("/alarms/{batchAlarmId}/estimate")
    public MedicineEstimateResponse estimateForAlarm(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long batchAlarmId,
            @RequestParam(required = false) BigDecimal userPricePerUnit) {
        return medicineEstimationService.estimateForAlarm(principal.getId(), batchAlarmId, userPricePerUnit);
    }

    // "Mark as Done" with the farmer's manually confirmed/overridden figures.
    @PostMapping("/alarms/complete")
    public MedicineTaskCompletionResponse completeTask(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody MedicineTaskCompletionRequest request) {
        return medicineEstimationService.completeTask(principal.getId(), request);
    }
}
