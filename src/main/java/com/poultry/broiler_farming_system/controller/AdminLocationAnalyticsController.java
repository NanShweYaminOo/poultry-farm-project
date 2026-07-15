package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.analytics.LocationBatchCountRow;
import com.poultry.broiler_farming_system.dto.analytics.LocationMarketplaceVolumeRow;
import com.poultry.broiler_farming_system.dto.analytics.LocationRevenueRow;
import com.poultry.broiler_farming_system.dto.analytics.LocationUserCountRow;
import com.poultry.broiler_farming_system.service.analytics.AdminLocationAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Admin-only (covered by SecurityConfig's existing "/api/v1/admin/**" ->
// hasRole("ADMIN") rule, no separate matcher needed). Farmer/buyer analytics
// by location, per spec -- every endpoint is a DTO-projected aggregate
// query, see AdminLocationAnalyticsService and each repository's @Query.
@RestController
@RequestMapping("/api/v1/admin/analytics/location")
@RequiredArgsConstructor
public class AdminLocationAnalyticsController {

    private final AdminLocationAnalyticsService analyticsService;

    @GetMapping("/users")
    public List<LocationUserCountRow> userCounts() {
        return analyticsService.userCountsByLocation();
    }

    @GetMapping("/batches")
    public List<LocationBatchCountRow> activeBatches() {
        return analyticsService.activeBatchCountsByLocation();
    }

    @GetMapping("/marketplace")
    public List<LocationMarketplaceVolumeRow> marketplaceVolume() {
        return analyticsService.marketplaceVolumeByLocation();
    }

    @GetMapping("/revenue")
    public List<LocationRevenueRow> revenue() {
        return analyticsService.revenueByLocation();
    }
}
