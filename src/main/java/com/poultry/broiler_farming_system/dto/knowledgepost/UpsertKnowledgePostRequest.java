package com.poultry.broiler_farming_system.dto.knowledgepost;

import com.poultry.broiler_farming_system.entity.enums.KnowledgePostStatus;
import com.poultry.broiler_farming_system.entity.enums.KnowledgePostType;

// Bound via @ModelAttribute from multipart/form-data fields (sent alongside
// the optional image/document parts), not a JSON @RequestBody -- a request
// can't mix a JSON body with file parts.
public record UpsertKnowledgePostRequest(
        String titleEn,
        String titleMy,
        String contentEn,
        String contentMy,
        KnowledgePostType postType,
        KnowledgePostStatus status
) {
}
