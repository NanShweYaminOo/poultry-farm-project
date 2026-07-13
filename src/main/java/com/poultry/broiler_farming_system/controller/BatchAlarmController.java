package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.medicine.BatchAlarmResponse;
import com.poultry.broiler_farming_system.dto.medicine.CreateBatchAlarmRequest;
import com.poultry.broiler_farming_system.service.medicine.BatchAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batch-alarms")
@RequiredArgsConstructor
public class BatchAlarmController {

    private final BatchAlarmService batchAlarmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchAlarmResponse create(@RequestBody CreateBatchAlarmRequest request) {
        return batchAlarmService.createAlarm(request);
    }

    @DeleteMapping("/{batchAlarmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long batchAlarmId) {
        batchAlarmService.cancelAlarm(batchAlarmId);
    }

    @GetMapping("/batches/{batchId}")
    public List<BatchAlarmResponse> listForBatch(@PathVariable Long batchId) {
        return batchAlarmService.listForBatch(batchId);
    }
}
