package com.poultry.broiler_farming_system.dto.payment;

import com.poultry.broiler_farming_system.entity.enums.PaymentStatus;
import com.poultry.broiler_farming_system.entity.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentTransactionResponse(
        Long id,
        Long userId,
        String userUsername,
        Long batchId,
        String batchName,
        PaymentType paymentType,
        String screenshotUrl,
        PaymentStatus status,
        LocalDateTime transactionTimestamp,
        Long reviewedByAdminId,
        LocalDateTime reviewedAt,
        BigDecimal amount
) {
}
