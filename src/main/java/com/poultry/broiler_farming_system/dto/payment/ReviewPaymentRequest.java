package com.poultry.broiler_farming_system.dto.payment;

import com.poultry.broiler_farming_system.entity.enums.PaymentStatus;

// adminId is NOT a field here -- it's the authenticated caller (SecurityConfig
// requires ROLE_ADMIN on this endpoint), not client input. A client-supplied
// adminId would let one admin's request masquerade as another's approval.
// decision must be APPROVED or REJECTED; PENDING is rejected as invalid input.
public record ReviewPaymentRequest(PaymentStatus decision) {
}
