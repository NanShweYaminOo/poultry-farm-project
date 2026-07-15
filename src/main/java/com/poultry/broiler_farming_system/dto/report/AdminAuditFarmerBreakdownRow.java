package com.poultry.broiler_farming_system.dto.report;

import java.math.BigDecimal;

public record AdminAuditFarmerBreakdownRow(
        Long farmerId,
        String username,
        String fullName,
        BigDecimal totalRevenue,
        long approvedCount
) {
}
