package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.BatchExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BatchExpenseRepository extends JpaRepository<BatchExpense, Long> {

    Optional<BatchExpense> findByBatchAlarmId(Long batchAlarmId);
}
