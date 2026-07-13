package com.poultry.broiler_farming_system.service.moderation;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Stateless text scanner for the dual-layer (English + Burmese) automated
 * moderation filter. Word lists are loaded from classpath resources rather
 * than hard-coded, so they can be replaced/extended without a rebuild.
 */
@Component
public class ProfanityFilterService {

    private static final String EN_WORDLIST = "moderation/profanity-en.txt";
    private static final String MY_WORDLIST = "moderation/profanity-my.txt";

    private final Set<String> bannedTerms;

    public ProfanityFilterService() {
        Set<String> combined = new HashSet<>();
        combined.addAll(loadWordList(EN_WORDLIST));
        combined.addAll(loadWordList(MY_WORDLIST));
        this.bannedTerms = Collections.unmodifiableSet(combined);
    }

    public Optional<String> findViolation(String text) {
        if (!StringUtils.hasText(text)) {
            return Optional.empty();
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        for (String term : bannedTerms) {
            if (matches(normalized, term)) {
                return Optional.of(term);
            }
        }
        return Optional.empty();
    }

    public boolean isClean(String text) {
        return findViolation(text).isEmpty();
    }

    private boolean matches(String normalizedText, String term) {
        // ASCII terms (English) are matched on word boundaries to avoid
        // false positives inside unrelated words; Burmese (and any other
        // non-Latin) terms fall back to a substring match since the script
        // has no whitespace-based word segmentation.
        boolean isAscii = term.chars().allMatch(c -> c < 128);
        if (isAscii) {
            return Pattern.compile("\\b" + Pattern.quote(term) + "\\b").matcher(normalizedText).find();
        }
        return normalizedText.contains(term);
    }

    private Set<String> loadWordList(String classpathLocation) {
        Set<String> terms = new HashSet<>();
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            return terms;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                terms.add(trimmed.toLowerCase(Locale.ROOT));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load profanity word list: " + classpathLocation, ex);
        }
        return terms;
    }
}
