package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.feedback.CreateFeedbackTicketRequest;
import com.poultry.broiler_farming_system.dto.feedback.FeedbackTicketResponse;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.feedback.FeedbackTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Any authenticated user can file/view their own feedback tickets here.
// Admin-wide listing + status updates stay on AdminFeedbackController.
@RestController
@RequestMapping("/api/v1/feedback-tickets")
@RequiredArgsConstructor
public class FeedbackTicketController {

    private final FeedbackTicketService feedbackTicketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackTicketResponse create(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateFeedbackTicketRequest request) {
        return feedbackTicketService.create(principal.getId(), request);
    }

    @GetMapping
    public List<FeedbackTicketResponse> listMine(@AuthenticationPrincipal UserPrincipal principal) {
        return feedbackTicketService.listMine(principal.getId());
    }
}
