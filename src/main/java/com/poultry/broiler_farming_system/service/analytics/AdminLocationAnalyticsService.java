package com.poultry.broiler_farming_system.service.analytics;

import com.poultry.broiler_farming_system.dto.analytics.LocationBatchCountRow;
import com.poultry.broiler_farming_system.dto.analytics.LocationCountRow;
import com.poultry.broiler_farming_system.dto.analytics.LocationMarketplaceVolumeRow;
import com.poultry.broiler_farming_system.dto.analytics.LocationRevenueRow;
import com.poultry.broiler_farming_system.dto.analytics.LocationUserCountRow;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.repository.BuyRequestRepository;
import com.poultry.broiler_farming_system.repository.PaymentTransactionRepository;
import com.poultry.broiler_farming_system.repository.SalesPostRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

// Every query here is a DTO-projected, SQL-side GROUP BY (see the @Query
// methods on each repository) -- never a full entity load followed by
// in-memory aggregation. The one exception is marketplaceVolumeByLocation,
// which merges two independent single-entity aggregates (SalesPost,
// BuyRequest) by location key in Java, since there's no single JPQL query
// that GROUPs BY across two unrelated entities without a cross-join.
@Service
@RequiredArgsConstructor
public class AdminLocationAnalyticsService {

    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final SalesPostRepository salesPostRepository;
    private final BuyRequestRepository buyRequestRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Transactional(readOnly = true)
    public List<LocationUserCountRow> userCountsByLocation() {
        return userRepository.countUsersByLocationAccountTypeAndRole();
    }

    @Transactional(readOnly = true)
    public List<LocationBatchCountRow> activeBatchCountsByLocation() {
        return batchRepository.countActiveBatchesByLocation();
    }

    @Transactional(readOnly = true)
    public List<LocationMarketplaceVolumeRow> marketplaceVolumeByLocation() {
        Map<String, Long> salesByLocation = toLocationMap(salesPostRepository.countByLocation());
        Map<String, Long> buyByLocation = toLocationMap(buyRequestRepository.countByLocation());

        TreeSet<String> allLocations = new TreeSet<>();
        allLocations.addAll(salesByLocation.keySet());
        allLocations.addAll(buyByLocation.keySet());

        return allLocations.stream()
                .map(location -> new LocationMarketplaceVolumeRow(
                        location,
                        salesByLocation.getOrDefault(location, 0L),
                        buyByLocation.getOrDefault(location, 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LocationRevenueRow> revenueByLocation() {
        return paymentTransactionRepository.revenueByLocation();
    }

    private Map<String, Long> toLocationMap(List<LocationCountRow> rows) {
        return rows.stream().collect(Collectors.toMap(LocationCountRow::location, LocationCountRow::count));
    }
}
