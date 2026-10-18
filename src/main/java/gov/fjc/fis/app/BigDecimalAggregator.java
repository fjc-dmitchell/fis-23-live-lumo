package gov.fjc.fis.app;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public final class BigDecimalAggregator {

    private BigDecimalAggregator() {
    }

    public static <T> BigDecimal sum(Collection<T> source,
                                     Function<T, BigDecimal> extractor) {
        if (source == null || source.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return source.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static <T> BigDecimal sumFiltered(Collection<T> source,
                                             Predicate<T> filter,
                                             Function<T, BigDecimal> extractor) {
        if (source == null || source.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return source.stream()
                .filter(filter)
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
