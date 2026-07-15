package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    // All calculators must compute strictly off the latest record for a batch.
    Optional<DailyLog> findTopByBatchIdOrderByLogDateDesc(Long batchId);

    boolean existsByBatchIdAndLogDate(Long batchId, LocalDate logDate);

    Optional<DailyLog> findByBatchIdAndLogDate(Long batchId, LocalDate logDate);

    List<DailyLog> findByBatchIdOrderByLogDateDesc(Long batchId);
}
