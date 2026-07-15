package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.medicine.BatchAlarmResponse;
import com.poultry.broiler_farming_system.dto.medicine.CreateBatchAlarmRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.medicine.BatchAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batch-alarms")
@RequiredArgsConstructor
public class BatchAlarmController {

    private final BatchAlarmService batchAlarmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchAlarmResponse create(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateBatchAlarmRequest request) {
        return batchAlarmService.createAlarm(principal.getId(), request);
    }

    @DeleteMapping("/{batchAlarmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long batchAlarmId) {
        batchAlarmService.cancelAlarm(principal.getId(), batchAlarmId);
    }

    @GetMapping("/batches/{batchId}")
    public List<BatchAlarmResponse> listForBatch(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long batchId) {
        return batchAlarmService.listForBatch(principal.getId(), batchId);
    }
}
