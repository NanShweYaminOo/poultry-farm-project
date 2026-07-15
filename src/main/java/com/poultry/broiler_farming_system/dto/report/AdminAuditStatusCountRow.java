package com.poultry.broiler_farming_system.dto.report;

import com.poultry.broiler_farming_system.entity.enums.PaymentStatus;

public record AdminAuditStatusCountRow(PaymentStatus status, long count) {
}
