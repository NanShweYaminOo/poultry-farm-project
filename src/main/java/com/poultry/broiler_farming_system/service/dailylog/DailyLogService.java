package com.poultry.broiler_farming_system.service.dailylog;

import com.poultry.broiler_farming_system.dto.dailylog.CreateDailyLogRequest;
import com.poultry.broiler_farming_system.dto.dailylog.DailyLogResponse;
import com.poultry.broiler_farming_system.dto.dailylog.DailyLogTodayStatusResponse;

import java.util.List;

public interface DailyLogService {

    // Farmer logs today's (or an explicitly given past) mortality/remaining
    // count for their own started, active batch. Rejects duplicate
    // batch+date entries and counts that don't make sense against the
    // latest recorded record.
    DailyLogResponse create(Long farmerId, CreateDailyLogRequest request);

    // Full history for one batch, most recent first. Callable by the
    // owning farmer or an Admin.
    List<DailyLogResponse> listByBatch(Long callerId, Long batchId);

    // Whether today's log has already been filed for this batch -- same
    // owner-or-Admin auth rule as listByBatch. Backs the frontend's
    // outstanding-log warning ahead of the 18:00 SMS reminder job.
    DailyLogTodayStatusResponse getTodayStatus(Long callerId, Long batchId);
}
