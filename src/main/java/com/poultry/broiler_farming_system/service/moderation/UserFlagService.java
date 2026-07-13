package com.poultry.broiler_farming_system.service.moderation;

import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists the isFlaggedForReview flag in its own committed transaction.
 * Kept as a separate bean (not a private method on ContentModerationService)
 * so REQUIRES_NEW actually goes through the Spring AOP proxy -- a call from
 * within the same class would bypass it (self-invocation) and silently join
 * the caller's transaction instead. That matters here because the flag must
 * survive even when the caller's transaction rolls back after rejecting the
 * offending request.
 */
@Component
@RequiredArgsConstructor
public class UserFlagService {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void flagForReview(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsFlaggedForReview(true);
            userRepository.save(user);
        });
    }
}
