package com.poultry.broiler_farming_system.dto.knowledgepost;

import com.poultry.broiler_farming_system.entity.enums.KnowledgePostStatus;
import com.poultry.broiler_farming_system.entity.enums.KnowledgePostType;

import java.time.LocalDateTime;

public record KnowledgePostResponse(
        Long id,
        KnowledgePostType postType,
        String titleEn,
        String titleMy,
        String contentEn,
        String contentMy,
        KnowledgePostStatus status,
        String authorUsername,
        String imageUrl,
        String documentUrl,
        LocalDateTime createdDate
) {
}
