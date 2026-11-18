package gov.fjc.fis.app;

import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * FIS utility class for sanitizing text values by converting Unicode characters
 * to their nearest ASCII equivalents and removing unsupported characters.
 * <p>
 * This class is intended for use when importing, persisting, or processing
 * data that must contain only printable ASCII characters. Common Unicode
 * punctuation marks are replaced with ASCII equivalents, accented characters
 * are transliterated to their base characters, and any remaining unsupported
 * characters are removed.
 * </p>
 * <p>
 * Typical usage includes sanitizing Jmix entity attributes prior to database
 * persistence, integration processing, report generation, or export to
 * external systems that require ASCII-only content.
 * </p>
 *
 * @author Doug Mitchell
 * @since 2.3
 */
public final class AsciiSanitizer {

    private AsciiSanitizer() {
    }

    private static final Map<Character, String> PUNCTUATION_MAP = Map.ofEntries(
            Map.entry('\u2018', "'"), Map.entry('\u2019', "'"),    // ‘ ’
            Map.entry('\u201C', "\""), Map.entry('\u201D', "\""),  // “ ”
            Map.entry('\u2013', "-"), Map.entry('\u2014', "-"),    // – —
            Map.entry('\u2026', "..."),                                  // …
            Map.entry('\u00A0', " "),                                    // NBSP
            Map.entry('\u2022', "-"),                                    // •
            Map.entry('\u2212', "-")                                     // − (unicode minus sign)
    );

    private static final Pattern DISALLOWED = Pattern.compile("[^\\x20-\\x7E\\t\\n\\r]");

    /**
     * Sanitizes the supplied text by:
     * <ul>
     *     <li>Normalizing Unicode characters using NFKD normalization.</li>
     *     <li>Replacing common Unicode punctuation with ASCII equivalents.</li>
     *     <li>Removing diacritical marks from accented characters.</li>
     *     <li>Removing any remaining non-printable or non-ASCII characters.</li>
     * </ul>
     *
     * <p>Examples:</p>
     * <ul>
     *     <li>{@code "José"} becomes {@code "Jose"}</li>
     *     <li>{@code "“Hello”"} becomes {@code "\"Hello\""}</li>
     *     <li>{@code "A–B"} becomes {@code "A-B"}</li>
     *     <li>{@code "ﬁle"} becomes {@code "file"}</li>
     * </ul>
     *
     * @param input the text to sanitize; may be {@code null}
     * @return the sanitized ASCII representation of the input, or
     *         {@code null} if the supplied input is {@code null}
     */
    public static String sanitize(String input) {
        if (input == null) return null;

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFKD); // ﬁ -> fi, é -> e + ́

        StringBuilder sb = new StringBuilder(normalized.length());
        for (char c : normalized.toCharArray()) {
            sb.append(PUNCTUATION_MAP.getOrDefault(c, String.valueOf(c)));
        }

        String withoutDiacritics = sb.toString().replaceAll("\\p{Mn}+", "");
        return DISALLOWED.matcher(withoutDiacritics).replaceAll("");
    }
}
