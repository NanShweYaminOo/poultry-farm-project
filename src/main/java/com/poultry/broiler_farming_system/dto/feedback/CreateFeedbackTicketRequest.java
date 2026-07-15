package com.poultry.broiler_farming_system.dto.feedback;

// submittedById is NOT a field here -- it's the authenticated caller, taken
// from the security context in FeedbackTicketController, same as every
// other "create as me" request in this system.
public record CreateFeedbackTicketRequest(String content) {
}
