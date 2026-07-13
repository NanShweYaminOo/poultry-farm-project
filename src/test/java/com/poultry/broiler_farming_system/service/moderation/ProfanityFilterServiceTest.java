package com.poultry.broiler_farming_system.service.moderation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProfanityFilterServiceTest {

    private ProfanityFilterService profanityFilterService;

    @BeforeEach
    void setUp() {
        profanityFilterService = new ProfanityFilterService();
    }

    @Test
    void isCleanForOrdinaryFarmingText() {
        assertThat(profanityFilterService.isClean("Selling 200 healthy broilers, ready for pickup next week."))
                .isTrue();
        assertThat(profanityFilterService.findViolation("Selling 200 healthy broilers, ready for pickup next week."))
                .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"You are an idiot", "IDIOT", "stop being so Stupid", "just shut up already"})
    void detectsEnglishTermsCaseInsensitiveOnWordBoundaries(String text) {
        assertThat(profanityFilterService.isClean(text)).isFalse();
        assertThat(profanityFilterService.findViolation(text)).isPresent();
    }

    @Test
    void doesNotFlagWordsThatOnlyContainABannedTermAsASubstring() {
        // "idiot" is banned, but the word-boundary match must not fire on
        // words that merely contain it as a substring.
        assertThat(profanityFilterService.isClean("The idiotically simple fix worked.")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    void blankOrEmptyTextHasNoViolation(String text) {
        assertThat(profanityFilterService.findViolation(text)).isEmpty();
        assertThat(profanityFilterService.isClean(text)).isTrue();
    }

    @Test
    void nullTextHasNoViolation() {
        assertThat(profanityFilterService.findViolation(null)).isEmpty();
        assertThat(profanityFilterService.isClean(null)).isTrue();
    }
}
