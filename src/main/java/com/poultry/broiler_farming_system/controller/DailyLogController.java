package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.dailylog.CreateDailyLogRequest;
import com.poultry.broiler_farming_system.dto.dailylog.DailyLogResponse;
import com.poultry.broiler_farming_system.dto.dailylog.DailyLogTodayStatusResponse;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.dailylog.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Farmers record their own batch's daily mortality/remaining-count here.
// SecurityConfig restricts this whole path to PAID/ADMIN, same as the other
// active-batch-management endpoints (batch-alarms, inventory, ...).
@RestController
@RequestMapping("/api/v1/daily-logs")
@RequiredArgsConstructor
public class DailyLogController {

    private final DailyLogService dailyLogService;

    // e.g. { "batchId": 4, "logDate": "2026-07-14", "dailyMortalityCount": 2, "totalRemainingChickenCount": 498 }
    // logDate may be omitted to default to today.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DailyLogResponse create(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateDailyLogRequest request) {
        return dailyLogService.create(principal.getId(), request);
    }

    @GetMapping("/batch/{batchId}")
    public List<DailyLogResponse> listByBatch(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long batchId) {
        return dailyLogService.listByBatch(principal.getId(), batchId);
    }

    @GetMapping("/batch/{batchId}/today/status")
    public DailyLogTodayStatusResponse todayStatus(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long batchId) {
        return dailyLogService.getTodayStatus(principal.getId(), batchId);
    }
}
