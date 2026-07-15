package com.poultry.broiler_farming_system.dto.dailylog;

import java.time.LocalDate;

public record DailyLogResponse(
        Long id,
        Long batchId,
        String batchName,
        LocalDate logDate,
        Integer dailyMortalityCount,
        Integer totalRemainingChickenCount
) {
}
