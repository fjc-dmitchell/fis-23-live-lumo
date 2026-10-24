package gov.fjc.fis.app;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * FIS utility class for aggregating BigDecimal values from collections. Be mindful of null values (returning zero scale) and empty collections.
 */
public final class BigDecimalAggregator {

    private BigDecimalAggregator() {
    }

    public static <T> BigDecimal sum(Collection<T> source,
                                     Function<? super T, BigDecimal> extractor) {
        return sumFiltered(source, _ -> true, extractor);
    }

    public static <T> BigDecimal sumFiltered(Collection<T> source,
                                             Predicate<? super T> filter,
                                             Function<? super T, BigDecimal> extractor) {
        if (source == null) {
            return BigDecimal.ZERO;
        }
        return source.stream()
                .filter(filter)
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
