package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Provides operations for calculating adjustment totals associated with an
 * {@link Appropriation}.
 *
 * <p>This service queries appropriation adjustments and calculates totals for
 * their one-year and two-year amounts. Aggregate methods return zero when an
 * appropriation has no corresponding adjustments.</p>
 *
 * @author Doug Mitchell
 */
@Component("fis_AppropriationAdjustmentService")
public class AppropriationAdjustmentService {

    private final DataManager dataManager;

    public AppropriationAdjustmentService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Calculates the total one-year adjustment amount for an appropriation.
     *
     * @param appropriation the appropriation whose adjustments are totaled
     * @return the sum of the one-year adjustment amounts, or zero if there are
     * no corresponding adjustments
     */
    public BigDecimal sumOneYearAdjustments(Appropriation appropriation) {
        return dataManager.loadValue(
                        "SELECT COALESCE(SUM(e.oneYearAmount),0)"
                                + " FROM fis_AppropriationAdjustment e"
                                + " WHERE e.appropriation = :appropriation", BigDecimal.class)
                .parameter("appropriation", appropriation)
                .one();
    }

    /**
     * Calculates the total two-year adjustment amount for an appropriation.
     *
     * @param appropriation the appropriation whose adjustments are totaled
     * @return the sum of the two-year adjustment amounts, or zero if there are
     * no corresponding adjustments
     */
    public BigDecimal sumTwoYearAdjustments(Appropriation appropriation) {
        return dataManager.loadValue(
                        "SELECT COALESCE(SUM(e.twoYearAmount),0)"
                                + " FROM fis_AppropriationAdjustment e"
                                + " WHERE e.appropriation = :appropriation", BigDecimal.class)
                .parameter("appropriation", appropriation)
                .one();
    }
}
