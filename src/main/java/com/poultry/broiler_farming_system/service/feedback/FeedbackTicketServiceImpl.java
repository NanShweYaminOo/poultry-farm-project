package com.poultry.broiler_farming_system.service.feedback;

import com.poultry.broiler_farming_system.dto.feedback.FeedbackTicketResponse;
import com.poultry.broiler_farming_system.dto.feedback.UpdateFeedbackTicketStatusRequest;
import com.poultry.broiler_farming_system.entity.FeedbackTicket;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.TicketStatus;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.FeedbackTicketRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackTicketServiceImpl implements FeedbackTicketService {

    private final FeedbackTicketRepository feedbackTicketRepository;
    private final UserRepository userRepository;

    @Override
    public List<FeedbackTicketResponse> listAll() {
        return feedbackTicketRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FeedbackTicketResponse updateStatus(Long ticketId, Long adminId, UpdateFeedbackTicketStatusRequest request) {
        if (request.status() == null) {
            throw new IllegalArgumentException("status is required.");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user " + adminId + " was not found."));
        // SecurityConfig already restricts this endpoint to ROLE_ADMIN; this
        // is a cheap defense-in-depth re-check, not the primary gate.
        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException(
                    "User " + admin.getId() + " is not an Admin and cannot update feedback tickets.");
        }

        FeedbackTicket ticket = feedbackTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback ticket " + ticketId + " was not found."));

        ticket.setStatus(request.status());
        ticket.setHandledByAdmin(admin);
        if (request.status() == TicketStatus.RESOLVED) {
            ticket.setResolvedDate(LocalDateTime.now());
        }

        return toResponse(ticket);
    }

    private FeedbackTicketResponse toResponse(FeedbackTicket ticket) {
        User submittedBy = ticket.getSubmittedBy();
        return new FeedbackTicketResponse(
                ticket.getId(),
                ticket.getContent(),
                ticket.getStatus(),
                submittedBy.getFullName(),
                submittedBy.getProfileImageUrl(),
                ticket.getCreatedDate(),
                ticket.getResolvedDate());
    }
}
