package com.poultry.broiler_farming_system.dto.analytics;

import java.math.BigDecimal;

// totalRevenue may be null for a location where every approved transaction
// predates fee configuration -- see PaymentTransaction.amount.
public record LocationRevenueRow(String location, BigDecimal totalRevenue, long transactionCount) {
}
