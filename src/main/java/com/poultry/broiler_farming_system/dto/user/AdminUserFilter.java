package com.poultry.broiler_farming_system.dto.user;

import com.poultry.broiler_farming_system.entity.enums.AccountType;
import com.poultry.broiler_farming_system.entity.enums.UserRole;

// Every field is optional -- AdminUserServiceImpl builds a Specification
// combining only the filters that are actually present. Bound from
// individual @RequestParams in AdminUserController, not a JSON body.
public record AdminUserFilter(
        UserRole role,
        AccountType accountType,
        Boolean isFlaggedForReview,
        Boolean isBanned,
        String location
) {
}
