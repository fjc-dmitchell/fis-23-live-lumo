package gov.fjc.fis.app;

import io.jmix.flowui.data.aggregation.AggregationStrategy;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

/**
 * Provides a fixed label for a data grid aggregation row.
 * <p>
 * This strategy returns {@code "Total:"} regardless of the values in the
 * aggregated column. It can be applied to a nonnumeric column adjacent to
 * columns containing calculated totals to identify the aggregation row.
 *
 * @author Doug Mitchell
 */
public class TotalLabelAggregationStrategy implements AggregationStrategy<Object, String> {
    @Override
    public String aggregate(@NonNull Collection<Object> propertyValues) {
        return "Total:";
    }

    @Override
    public Class<String> getResultClass() {
        return String.class;
    }
}
