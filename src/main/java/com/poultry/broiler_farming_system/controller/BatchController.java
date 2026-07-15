package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.batch.BatchResponse;
import com.poultry.broiler_farming_system.dto.batch.CreateBatchRequest;
import com.poultry.broiler_farming_system.dto.batch.StopBatchRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.batch.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @GetMapping
    public List<BatchResponse> listMyBatches(@AuthenticationPrincipal UserPrincipal principal) {
        return batchService.listMyBatches(principal.getId());
    }

    // farmerId is the authenticated caller, not a request field -- see
    // CreateBatchRequest.
    // e.g. { "batchName": "Coop A", "initialChickenCount": 500, "cycleDurationDays": 45 }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchResponse createBatch(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateBatchRequest request) {
        return batchService.createBatch(principal.getId(), request);
    }

    @PostMapping("/{batchId}/start")
    public BatchResponse startBatch(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long batchId) {
        return batchService.startBatch(principal.getId(), batchId);
    }

    // e.g. { "finalStatus": "COMPLETED" } or { "finalStatus": "CANCELLED" }
    @PostMapping("/{batchId}/stop")
    public BatchResponse stopBatch(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long batchId,
            @RequestBody StopBatchRequest request) {
        return batchService.stopBatch(principal.getId(), batchId, request.finalStatus());
    }
}
