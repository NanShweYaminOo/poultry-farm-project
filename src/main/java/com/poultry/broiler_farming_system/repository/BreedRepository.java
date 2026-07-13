package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.Breed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BreedRepository extends JpaRepository<Breed, Long> {
}
