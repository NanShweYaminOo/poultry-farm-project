package com.poultry.broiler_farming_system.dto.analytics;

// Shared shape for the two independent SalesPost/BuyRequest per-location
// count queries -- AdminLocationAnalyticsService merges one of each into a
// single LocationMarketplaceVolumeRow per location, since a sales post and a
// buy request are different entities/tables with no single JPQL query that
// can count both without a cross-join.
public record LocationCountRow(String location, long count) {
}
