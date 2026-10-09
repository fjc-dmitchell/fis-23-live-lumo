package gov.fjc.fis.app;

import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

public final class AsciiSanitizer {

    private static final Map<Character, String> PUNCTUATION_MAP = Map.ofEntries(
            Map.entry('\u2018', "'"), Map.entry('\u2019', "'"),    // ‘ ’
            Map.entry('\u201C', "\""), Map.entry('\u201D', "\""),  // “ ”
            Map.entry('\u2013', "-"), Map.entry('\u2014', "-"),    // – —
            Map.entry('\u2026', "..."),                             // …
            Map.entry('\u00A0', " "),                               // NBSP
            Map.entry('\u2022', "-")                                // •
    );

    private static final Pattern DISALLOWED = Pattern.compile("[^\\x20-\\x7E\\t\\n\\r]");

    private AsciiSanitizer() {
    }

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
