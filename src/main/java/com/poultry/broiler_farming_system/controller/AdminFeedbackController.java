package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.feedback.FeedbackTicketResponse;
import com.poultry.broiler_farming_system.dto.feedback.UpdateFeedbackTicketStatusRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.feedback.FeedbackTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Admin-only view over all feedback tickets. Access is restricted to
// ROLE_ADMIN by the existing "/api/v1/admin/**" rule in SecurityConfig.
@RestController
@RequestMapping("/api/v1/admin/feedback-tickets")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final FeedbackTicketService feedbackTicketService;

    @GetMapping
    public List<FeedbackTicketResponse> listAll() {
        return feedbackTicketService.listAll();
    }

    // adminId is the authenticated caller (SecurityConfig requires ROLE_ADMIN
    // here) -- see UpdateFeedbackTicketStatusRequest.
    // e.g. { "status": "RESOLVED" }
    @PostMapping("/{ticketId}/status")
    public FeedbackTicketResponse updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long ticketId,
            @RequestBody UpdateFeedbackTicketStatusRequest request) {
        return feedbackTicketService.updateStatus(ticketId, principal.getId(), request);
    }
}
