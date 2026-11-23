package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Provides queries for retrieving and summarizing division allocation data.
 *
 * <p>This service returns allocation data as {@link KeyValueEntity} instances
 * for use in reports and other application services.</p>
 *
 * @author Doug Mitchell
 */
@Component("fis_DivisionAllocationService")
public class DivisionAllocationService {

    private final DataManager dataManager;

    public DivisionAllocationService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Retrieves division allocations for the specified appropriation and funds.
     *
     * <p>Each returned {@link KeyValueEntity} contains the following
     * properties:</p>
     * <ul>
     *     <li>{@code majorObjectClass} - the major object class</li>
     *     <li>{@code divisionCode} - the division code</li>
     *     <li>{@code oneYearAmount} - the one-year allocation amount</li>
     *     <li>{@code twoYearAmount} - the two-year allocation amount</li>
     * </ul>
     *
     * <p>Null allocation amounts are returned as zero. If {@code funds} is
     * empty, this method returns an empty list without executing a query.</p>
     *
     * @param appropriation the appropriation whose allocations are retrieved
     * @param funds the funds included in the query
     * @return the allocation data, or an empty list if no funds are supplied
     */
    public List<KeyValueEntity> fetchAllocations(Appropriation appropriation, List<Fund> funds) {

        if (funds.isEmpty()) {
            return Collections.emptyList();
        }

        return dataManager.loadValues(
                        "SELECT cat.majorObjectClass, div.divisionCode,"
                                + " COALESCE(e.oneYearAmount, 0), COALESCE(e.twoYearAmount, 0)"
                                + " FROM fis_DivisionAllocation e"
                                + " INNER JOIN fis_Division div ON div = e.division"
                                + " INNER JOIN fis_ObjectCategory cat ON cat = e.objectCategory"
                                + " WHERE div.appropriation = :appropriation"
                                + " AND div.fund IN :funds")
                .properties("majorObjectClass", "divisionCode", "oneYearAmount", "twoYearAmount")
                .parameter("appropriation", appropriation)
                .parameter("funds", funds)
                .list();
    }

    /**
     * Calculates the total one-year and two-year allocation amounts for each
     * specified division.
     *
     * <p>Divisions without allocation records are included with allocation
     * totals of zero. Each returned {@link KeyValueEntity} contains the
     * following properties:</p>
     * <ul>
     *     <li>{@code division} - the division</li>
     *     <li>{@code title} - the division title</li>
     *     <li>{@code oneYearAllocations} - the total one-year allocations</li>
     *     <li>{@code twoYearAllocations} - the total two-year allocations</li>
     * </ul>
     *
     * @param divisions the divisions whose allocation totals are calculated
     * @return the allocation totals for the specified divisions, ordered by
     *         division code
     */
    public List<KeyValueEntity> sumDivisionAllocations(List<Division> divisions) {
        return dataManager.loadValues(
                        "SELECT e, e.title, COALESCE(SUM(alloc.oneYearAmount),0), COALESCE(SUM(alloc.twoYearAmount),0)"
                                + " FROM fis_Division e"
                                + " LEFT JOIN fis_DivisionAllocation alloc ON e=alloc.division"
                                + " WHERE e IN :divisions"
                                + " GROUP BY e.id, e.divisionCode, e.title"
                                + " ORDER BY e.divisionCode")
                .parameter("divisions", divisions)
                .properties("division", "title", "oneYearAllocations", "twoYearAllocations")
                .list();
    }
}