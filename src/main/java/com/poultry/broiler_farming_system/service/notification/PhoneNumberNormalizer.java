package com.poultry.broiler_farming_system.service.notification;

import java.util.regex.Pattern;

/**
 * Normalizes this app's stored phone_number format (local Myanmar dialing
 * convention, e.g. "09xxxxxxxxx" or "09-xxx-xxxxxx") to E.164
 * ("+959xxxxxxxx") for the SMS gateway. Myanmar mobile subscriber numbers
 * (after the leading 0 trunk prefix is stripped and replaced with the +95
 * country code) are 7-9 digits, so a full E.164 number is 11-13 characters
 * including the leading '+' (+95 + 9 + 7..9 digits).
 */
public final class PhoneNumberNormalizer {

    private static final String MYANMAR_COUNTRY_CODE = "95";
    private static final Pattern NON_DIGIT = Pattern.compile("[^0-9+]");
    private static final Pattern E164_SHAPE = Pattern.compile("^\\+95\\d{8,10}$");

    private PhoneNumberNormalizer() {
    }

    /**
     * @throws IllegalArgumentException if the input isn't blank/null but
     *         doesn't normalize to a plausible Myanmar E.164 number. Callers
     *         on the async SMS path (SmsRequestedEventListener) already
     *         catch RuntimeException broadly, so this is treated the same
     *         as any other delivery failure -- logged, not propagated.
     */
    public static String toE164(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is blank.");
        }

        String cleaned = NON_DIGIT.matcher(rawPhoneNumber.trim()).replaceAll("");
        String e164;
        if (cleaned.startsWith("+")) {
            e164 = cleaned;
        } else if (cleaned.startsWith("00" + MYANMAR_COUNTRY_CODE)) {
            e164 = "+" + cleaned.substring(2);
        } else if (cleaned.startsWith(MYANMAR_COUNTRY_CODE)) {
            e164 = "+" + cleaned;
        } else if (cleaned.startsWith("0")) {
            // Local dialing format: 09xxxxxxxxx -> drop the trunk "0",
            // prepend the country code.
            e164 = "+" + MYANMAR_COUNTRY_CODE + cleaned.substring(1);
        } else {
            // Bare subscriber number with no trunk prefix at all (e.g. a
            // value already stored without the leading 0).
            e164 = "+" + MYANMAR_COUNTRY_CODE + cleaned;
        }

        if (!E164_SHAPE.matcher(e164).matches()) {
            throw new IllegalArgumentException(
                    "'" + rawPhoneNumber + "' does not normalize to a valid Myanmar E.164 number (" + e164 + ").");
        }
        return e164;
    }
}
