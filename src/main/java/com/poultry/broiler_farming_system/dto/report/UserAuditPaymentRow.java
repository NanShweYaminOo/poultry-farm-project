package com.poultry.broiler_farming_system.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserAuditPaymentRow(
        String batchName,
        String paymentType,
        String status,
        BigDecimal amount,
        LocalDateTime transactionTimestamp
) {
}
