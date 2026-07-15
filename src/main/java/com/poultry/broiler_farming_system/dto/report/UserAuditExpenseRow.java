package com.poultry.broiler_farming_system.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// calculatedRequirement/overriddenQuantity/quantityVariance are all null for
// expenses entered without a batch alarm (manual expenses have no "system
// calculated" figure to compare against) -- see
// UserAuditReportService.buildExpenseRow. quantityVariance =
// overriddenQuantity - calculatedRequirement; positive means the farmer used
// more than the system estimated, negative means less. This is the "variance"
// the audit trail exists to surface.
public record UserAuditExpenseRow(
        String batchName,
        String description,
        BigDecimal calculatedRequirement,
        BigDecimal overriddenQuantity,
        BigDecimal quantityVariance,
        BigDecimal finalCostIncurred,
        LocalDateTime expenseDate
) {
}
