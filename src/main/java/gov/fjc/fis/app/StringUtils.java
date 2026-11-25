package gov.fjc.fis.app;

import java.util.List;
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

    /**
     * Joins a list of strings into a human-readable phrase using the supplied
     * delimiter between items and an Oxford comma before the final item,
     * which is preceded by the word {@code "and"}.
     *
     * <p>This method is intended for displaying lists in user-facing messages,
     * validation errors, reports, and other text where natural English grammar
     * is preferred.</p>
     *
     * <p>Examples:</p>
     * <ul>
     *     <li>{@code []} returns {@code ""}</li>
     *     <li>{@code ["Finance"]} returns {@code "Finance"}</li>
     *     <li>{@code ["Finance", "Budget"]} returns
     *         {@code "Finance and Budget"}</li>
     *     <li>{@code ["Finance", "Budget", "Payroll"]} with a delimiter of
     *         {@code ", "} returns
     *         {@code "Finance, Budget, and Payroll"}</li>
     * </ul>
     *
     * @param items the items to join; may be {@code null} or empty
     * @param delimiter the delimiter used between items preceding the final item
     * @return a human-readable string containing the supplied items joined using
     *         the specified delimiter and the word {@code "and"}, including an
     *         Oxford comma when three or more items are present; returns an empty
     *         string if the list is {@code null} or empty
     */
    public static String joinWithAnd(List<String> items, String delimiter) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        if (items.size() == 1) {
            return items.getFirst();
        }

        if (items.size() == 2) {
            return items.getFirst() + " and " + items.getLast();
        }

        List<String> leading = items.subList(0, items.size() - 1);
        return String.join(delimiter, leading) + delimiter + "and " + items.getLast();
    }
}
