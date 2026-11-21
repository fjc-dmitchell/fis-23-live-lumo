package gov.fjc.fis.app;

import java.util.Locale;

public class StringUtils {
    private StringUtils() {
    }

    /**
     * Converts a {@code String} to uppercase using {@link Locale#ROOT}.
     *
     * <p>A {@code null} input string returns {@code null}.
     *
     * <pre>
     * StringUtils.toUpperCase(null)  = null
     * StringUtils.toUpperCase("")    = ""
     * StringUtils.toUpperCase("aBc") = "ABC"
     * </pre>
     *
     * @param input the {@code String} to convert, may be {@code null}
     * @return the uppercase {@code String}, or {@code null} if the input was {@code null}
     */
    public static String toUpperCase(String input) {
        return input == null ? null : input.toUpperCase(Locale.ROOT);
    }

}
