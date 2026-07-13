package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.BatchAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchAlarmRepository extends JpaRepository<BatchAlarm, Long> {

    List<BatchAlarm> findByBatchIdAndIsCompletedFalse(Long batchId);

    List<BatchAlarm> findByBatchIdOrderByScheduledTimeAsc(Long batchId);
}
