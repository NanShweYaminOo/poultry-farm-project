package com.poultry.broiler_farming_system.dto.faq;

import java.time.LocalDateTime;

public record FaqResponse(Long id, String question, String answer, LocalDateTime createdDate) {
}
