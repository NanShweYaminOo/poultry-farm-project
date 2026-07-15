package com.poultry.broiler_farming_system.dto.report;

import com.poultry.broiler_farming_system.entity.enums.PaymentType;

import java.math.BigDecimal;

// totalRevenue may be null -- SUM() over a group where every row's amount is
// null (fee wasn't configured yet when those transactions were submitted;
// see PaymentTransaction.amount) returns SQL NULL, not zero. Left null
// rather than coalesced in the query so the report template can flag it
// explicitly instead of silently showing a misleading "0.00" of real revenue.
public record AdminAuditRevenueByTypeRow(
        PaymentType paymentType,
        BigDecimal totalRevenue,
        long approvedCount
) {
}
