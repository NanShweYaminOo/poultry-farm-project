package com.poultry.broiler_farming_system.service.moderation;

import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFlagServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserFlagService userFlagService;

    @Test
    void setsIsFlaggedForReviewOnTheExistingUserAndSavesIt() {
        User user = User.builder().id(1L).isFlaggedForReview(false).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userFlagService.flagForReview(1L);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getIsFlaggedForReview()).isTrue();
    }

    @Test
    void doesNothingWhenUserNoLongerExists() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        userFlagService.flagForReview(99L);

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
