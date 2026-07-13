package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.BuyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuyRequestRepository extends JpaRepository<BuyRequest, Long> {

    List<BuyRequest> findAllByOrderByCreatedDateDesc();
}
