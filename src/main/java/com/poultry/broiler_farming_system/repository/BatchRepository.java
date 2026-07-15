package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.dto.analytics.LocationBatchCountRow;
import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    List<Batch> findByStatusAndIsStartedTrue(BatchStatus status);

    List<Batch> findByFarmerIdOrderByCreatedDateDesc(Long farmerId);

    boolean existsByFarmerIdAndIsStartedTrueAndStatus(Long farmerId, BatchStatus status);

    long countByFarmerId(Long farmerId);

    // Grouped by the owning farmer's normalized location -- see
    // UserRepository.countUsersByLocationAccountTypeAndRole for the same
    // normalization rule.
    @Query("SELECT new com.poultry.broiler_farming_system.dto.analytics.LocationBatchCountRow(" +
            "COALESCE(NULLIF(UPPER(TRIM(b.farmer.location)), ''), 'UNSPECIFIED'), COUNT(b)) " +
            "FROM Batch b " +
            "WHERE b.status = com.poultry.broiler_farming_system.entity.enums.BatchStatus.ACTIVE " +
            "GROUP BY COALESCE(NULLIF(UPPER(TRIM(b.farmer.location)), ''), 'UNSPECIFIED')")
    List<LocationBatchCountRow> countActiveBatchesByLocation();
}
