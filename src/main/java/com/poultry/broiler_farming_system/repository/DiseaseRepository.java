package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.Disease;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
}
