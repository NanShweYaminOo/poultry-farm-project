package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.dto.analytics.LocationCountRow;
import com.poultry.broiler_farming_system.entity.BuyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BuyRequestRepository extends JpaRepository<BuyRequest, Long> {

    List<BuyRequest> findAllByOrderByCreatedDateDesc();

    long countByCreatorId(Long creatorId);

    void deleteByCreatorId(Long creatorId);

    // Grouped by the creator's normalized location -- see
    // UserRepository.countUsersByLocationAccountTypeAndRole for the rule.
    @Query("SELECT new com.poultry.broiler_farming_system.dto.analytics.LocationCountRow(" +
            "COALESCE(NULLIF(UPPER(TRIM(b.creator.location)), ''), 'UNSPECIFIED'), COUNT(b)) " +
            "FROM BuyRequest b " +
            "GROUP BY COALESCE(NULLIF(UPPER(TRIM(b.creator.location)), ''), 'UNSPECIFIED')")
    List<LocationCountRow> countByLocation();
}
