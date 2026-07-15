package com.poultry.broiler_farming_system.dto.analytics;

import com.poultry.broiler_farming_system.entity.enums.AccountType;
import com.poultry.broiler_farming_system.entity.enums.UserRole;

// location is normalized (UPPER+TRIM, blank/null bucketed to "UNSPECIFIED")
// at the query level -- see UserRepository.countUsersByLocationAccountTypeAndRole
// -- since User.location is free text captured at registration with no
// enforced format.
public record LocationUserCountRow(
        String location,
        AccountType accountType,
        UserRole role,
        long count
) {
}
