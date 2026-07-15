package com.poultry.broiler_farming_system.dto.accountupgrade;

import com.poultry.broiler_farming_system.entity.enums.RequestStatus;

// adminId is NOT a field here -- it's the authenticated caller (SecurityConfig
// requires ROLE_ADMIN on this endpoint). decision must be APPROVED or
// REJECTED; PENDING is rejected as invalid input. adminNote is optional,
// e.g. a rejection reason shown back to the Guest.
public record ReviewAccountUpgradeRequestRequest(RequestStatus decision, String adminNote) {
}
