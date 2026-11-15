package gov.fjc.fis.app;

import io.jmix.flowui.data.aggregation.AggregationStrategy;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

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
