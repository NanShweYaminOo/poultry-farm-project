package com.poultry.broiler_farming_system.service.moderation;

import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.exception.ProfaneContentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentModerationServiceTest {

    @Mock
    private ProfanityFilterService profanityFilterService;

    @Mock
    private UserFlagService userFlagService;

    @InjectMocks
    private ContentModerationService contentModerationService;

    @Test
    void allowsCleanTextWithoutFlaggingTheAuthor() {
        User author = User.builder().id(42L).build();
        when(profanityFilterService.findViolation("healthy flock, no issues")).thenReturn(Optional.empty());

        contentModerationService.moderate(author, "healthy flock, no issues");

        verifyNoInteractions(userFlagService);
    }

    @Test
    void blocksViolatingTextAndFlagsTheAuthorForReview() {
        User author = User.builder().id(42L).build();
        when(profanityFilterService.findViolation("you idiot")).thenReturn(Optional.of("idiot"));

        assertThatThrownBy(() -> contentModerationService.moderate(author, "you idiot"))
                .isInstanceOf(ProfaneContentException.class);

        verify(userFlagService).flagForReview(42L);
    }

    @Test
    void doesNotFlagWhenTextIsClean() {
        User author = User.builder().id(7L).build();
        when(profanityFilterService.findViolation(any())).thenReturn(Optional.empty());

        contentModerationService.moderate(author, "anything");

        verify(userFlagService, never()).flagForReview(any());
    }
}
