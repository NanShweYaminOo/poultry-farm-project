package com.poultry.broiler_farming_system.service.feedback;

import com.poultry.broiler_farming_system.dto.feedback.CreateFeedbackTicketRequest;
import com.poultry.broiler_farming_system.dto.feedback.FeedbackTicketResponse;
import com.poultry.broiler_farming_system.dto.feedback.UpdateFeedbackTicketStatusRequest;

import java.util.List;

public interface FeedbackTicketService {

    // Any authenticated user files a ticket about themselves. Always lands
    // as PENDING.
    FeedbackTicketResponse create(Long userId, CreateFeedbackTicketRequest request);

    // The caller's own tickets, most recent first.
    List<FeedbackTicketResponse> listMine(Long userId);

    List<FeedbackTicketResponse> listAll();

    FeedbackTicketResponse updateStatus(Long ticketId, Long adminId, UpdateFeedbackTicketStatusRequest request);
}
