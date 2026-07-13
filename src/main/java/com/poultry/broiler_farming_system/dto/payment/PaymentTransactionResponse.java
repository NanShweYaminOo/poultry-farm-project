package com.poultry.broiler_farming_system.dto.payment;

import com.poultry.broiler_farming_system.entity.enums.PaymentStatus;
import com.poultry.broiler_farming_system.entity.enums.PaymentType;

import java.time.LocalDateTime;

public record PaymentTransactionResponse(
        Long id,
        Long userId,
        Long batchId,
        PaymentType paymentType,
        String screenshotUrl,
        PaymentStatus status,
        LocalDateTime transactionTimestamp,
        Long reviewedByAdminId,
        LocalDateTime reviewedAt
) {
}
