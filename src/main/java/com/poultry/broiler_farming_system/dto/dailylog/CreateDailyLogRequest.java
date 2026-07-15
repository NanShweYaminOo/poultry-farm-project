package com.poultry.broiler_farming_system.dto.dailylog;

import java.time.LocalDate;

// farmerId is NOT a field here -- it's the authenticated caller, taken from
// the security context in DailyLogController. logDate may be omitted to
// default to today (see DailyLogServiceImpl.create).
public record CreateDailyLogRequest(
        Long batchId,
        LocalDate logDate,
        Integer dailyMortalityCount,
        Integer totalRemainingChickenCount
) {
}
