package com.poultry.broiler_farming_system.dto.feedback;

import com.poultry.broiler_farming_system.entity.enums.TicketStatus;

import java.time.LocalDateTime;

public record FeedbackTicketResponse(
        Long id,
        String content,
        TicketStatus status,
        String submittedByFullName,
        String submittedByProfileImageUrl,
        LocalDateTime createdDate,
        LocalDateTime resolvedDate
) {
}
