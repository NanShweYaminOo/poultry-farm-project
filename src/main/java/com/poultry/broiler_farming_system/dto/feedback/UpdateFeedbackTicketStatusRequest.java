package com.poultry.broiler_farming_system.dto.feedback;

import com.poultry.broiler_farming_system.entity.enums.TicketStatus;

// adminId is NOT a field here -- it's the authenticated caller (SecurityConfig
// requires ROLE_ADMIN on this endpoint), not client input.
public record UpdateFeedbackTicketStatusRequest(TicketStatus status) {
}
