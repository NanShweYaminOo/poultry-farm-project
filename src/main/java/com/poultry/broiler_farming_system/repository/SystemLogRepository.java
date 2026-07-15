package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
}
