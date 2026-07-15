package com.poultry.broiler_farming_system.dto.accountupgrade;

// requestedBy is NOT a field here -- it's the authenticated caller, same
// rationale as CreatePaymentTransactionRequest.userId. reason is optional
// free text the Guest can use to tell the admin why they want to become a
// Farmer (e.g. "I run a 500-bird broiler farm in Mandalay").
public record CreateAccountUpgradeRequestRequest(String reason) {
}
