package com.poultry.broiler_farming_system.service.feedback;

import com.poultry.broiler_farming_system.dto.feedback.FeedbackTicketResponse;
import com.poultry.broiler_farming_system.dto.feedback.UpdateFeedbackTicketStatusRequest;

import java.util.List;

public interface FeedbackTicketService {

    List<FeedbackTicketResponse> listAll();

    FeedbackTicketResponse updateStatus(Long ticketId, Long adminId, UpdateFeedbackTicketStatusRequest request);
}
