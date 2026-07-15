package com.poultry.broiler_farming_system.dto.dailylog;

import java.time.LocalDate;

// submitted=false, log=null means today's daily log for this batch is still
// outstanding -- the frontend uses this to show a warning before the 18:00
// SMS reminder fires.
public record DailyLogTodayStatusResponse(
        Long batchId,
        LocalDate date,
        boolean submitted,
        DailyLogResponse log
) {
}
