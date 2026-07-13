package com.poultry.broiler_farming_system.dto.auth;

// role (subscription tier) is deliberately not a field here -- every
// self-registration lands as FREE (see UserRole/entity defaults); promotion
// to PAID only happens via the payment-approval flow, never via direct
// client input. accountType is a separate axis (GUEST/FARMER) and is
// intentionally client-supplied at registration time.
public record RegisterRequest(
        String fullName,
        String username,
        String phoneNumber,
        String email,
        String password,
        String location,
        String preferredLanguage,
        String accountType
) {
}
