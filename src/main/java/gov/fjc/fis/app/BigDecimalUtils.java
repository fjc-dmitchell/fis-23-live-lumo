package gov.fjc.fis.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * FIS utility class for dealing with BigDecimals.
 */
public final class BigDecimalUtils {

    private BigDecimalUtils() {
    }

    /**
     * Aggregates BigDecimal values from a collection, filtering and extracting values using the provided functions.
     *
     * @param source    The collection of elements to aggregate.
     * @param filter    A predicate to filter elements before aggregation.
     * @param extractor A function to extract BigDecimal values from each element.
     * @param <T>       The type of elements in the collection. Zero for empty collections.
     * @return The aggregated BigDecimal value.
     */
    public static <T> BigDecimal sumFiltered(Collection<T> source,
                                             Predicate<? super T> filter,
                                             Function<? super T, BigDecimal> extractor) {
        return source.stream()
                .filter(filter)
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Aggregates BigDecimal values from a collection, using the provided extractor function.
     *
     * @param source    The collection of elements to aggregate.
     * @param extractor A function to extract BigDecimal values from each element.
     * @param <T>       The type of elements in the collection. Zero for null or empty collections.
     * @return The aggregated BigDecimal value.
     */
    public static <T> BigDecimal sum(Collection<T> source,
                                     Function<? super T, BigDecimal> extractor) {
        return sumFiltered(source, _ -> true, extractor);
    }

    /**
     * Returns the sum of provided BigDecimal values, treating a null amount as zero. Needed because users may leave amount fields empty in the UI.
     *
     * @param amounts an array of BigDecimal values to sum; may contain nulls
     * @return the sum; never null
     */
    public static BigDecimal sumNullSafe(BigDecimal... amounts) {
        return Arrays.stream(amounts)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Compares two amounts by numeric value, ignoring scale, so 2.5 and 2.50 are equal.
     * <p>
     * Two nulls are considered equal; a null and a non-null are not.
     *
     * @param first  the first amount, may be null
     * @param second the second amount, may be null
     * @return true if both are null or both have the same numeric value
     */
    public static boolean equalsIgnoreScale(BigDecimal first, BigDecimal second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.compareTo(second) == 0;
    }

    /**
     * Rounds an amount to the given scale using {@link RoundingMode#HALF_UP}.
     *
     * @param value the amount to round, may be null
     * @param scale the number of digits to keep after the decimal point
     * @return the rounded amount, or null if {@code value} is null
     */
    public static BigDecimal roundToScale(BigDecimal value, int scale) {
        return roundToScale(value, scale, RoundingMode.HALF_UP);
    }

    /**
     * Rounds an amount to the given scale using the supplied rounding mode.
     *
     * @param value        the amount to round, may be null
     * @param scale        the number of digits to keep after the decimal point
     * @param roundingMode how to round when digits are discarded
     * @return the rounded amount, or null if {@code value} is null
     */
    public static BigDecimal roundToScale(BigDecimal value, int scale, RoundingMode roundingMode) {
        return value == null ? null : value.setScale(scale, roundingMode);
    }

    /**
     * Checks whether at least one of the given amounts is non-null and not zero.
     * <p>
     * Null values are ignored, and scale is irrelevant, so {@code 0.00} counts as zero.
     * Positive and negative values both count as non-zero.
     *
     * @param decimals the amounts to check, may contain nulls
     * @return true if any amount is non-null and non-zero; false if every amount is null or
     * zero, or if no amounts are given
     */
    public static boolean anyNonZero(BigDecimal... decimals) {
        return Arrays.stream(decimals)
                .anyMatch(bd -> bd != null && bd.signum() != 0);
    }
}
