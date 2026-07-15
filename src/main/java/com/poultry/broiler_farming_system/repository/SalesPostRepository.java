package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.dto.analytics.LocationCountRow;
import com.poultry.broiler_farming_system.entity.SalesPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SalesPostRepository extends JpaRepository<SalesPost, Long> {

    List<SalesPost> findAllByOrderByCreatedDateDesc();

    long countByCreatorId(Long creatorId);

    void deleteByCreatorId(Long creatorId);

    // Grouped by the creator's normalized location -- see
    // UserRepository.countUsersByLocationAccountTypeAndRole for the rule.
    @Query("SELECT new com.poultry.broiler_farming_system.dto.analytics.LocationCountRow(" +
            "COALESCE(NULLIF(UPPER(TRIM(s.creator.location)), ''), 'UNSPECIFIED'), COUNT(s)) " +
            "FROM SalesPost s " +
            "GROUP BY COALESCE(NULLIF(UPPER(TRIM(s.creator.location)), ''), 'UNSPECIFIED')")
    List<LocationCountRow> countByLocation();
}
