package com.poultry.broiler_farming_system.service.moderation;

import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.exception.ProfaneContentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Reusable entry point for the automated-filter half of the dual-layer
 * moderation requirement. Blocks the offending input and flags the
 * author's profile for admin review; the Admin dashboard's manual
 * warning/delete/ban actions are the second layer, outside this service.
 */
@Service
@RequiredArgsConstructor
public class ContentModerationService {

    private final ProfanityFilterService profanityFilterService;
    private final UserFlagService userFlagService;

    public void moderate(User author, String text) {
        Optional<String> violation = profanityFilterService.findViolation(text);
        if (violation.isPresent()) {
            userFlagService.flagForReview(author.getId());
            throw new ProfaneContentException(
                    "This text was blocked by the automated content filter, and your profile has been flagged for review.");
        }
    }
}
