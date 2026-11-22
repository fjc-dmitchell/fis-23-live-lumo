package gov.fjc.fis.security;

import gov.fjc.fis.entity.Appropriation;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPredicate;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

import java.time.Year;

/**
 * Row-level security role that restricts access to recent budget fiscal years.
 * <p>
 * Users assigned this role may view only {@link Appropriation} records whose
 * budget fiscal year falls within a configurable lookback period. This role
 * is intended for users who should access current and recent financial data
 * but do not require visibility into the full historical set of FIS
 * appropriations.
 * </p>
 * <p>
 * The role is currently assigned to the Education Administrator and
 * Technology Director security roles to limit access to appropriations
 * outside the permitted fiscal-year range.
 * </p>
 * @author Doug Mitchell
 */
@RowLevelRole(name = "AppropriationYearsData", code = AppropriationYearsDataRole.CODE)
public interface AppropriationYearsDataRole {
    String CODE = "data-appropriation-years";

    int YEARS_LOOKBACK = 5;

    /**
     * Creates a row-level predicate that limits appropriation visibility to the
     * configured lookback period.
     * <p>
     * Appropriations with invalid or missing budget fiscal years are excluded.
     * </p>
     *
     * @return a predicate that permits only appropriations within the allowed
     * fiscal-year range
     */
    @PredicateRowLevelPolicy(
            entityClass = Appropriation.class,
            actions = {RowLevelPolicyAction.READ})
    default RowLevelPredicate<Appropriation> withinLookbackYears() {
        return appropriation -> {
            Integer fiscalYear = parseFiscalYear(appropriation.getBudgetFiscalYear());
            if (fiscalYear == null) {
                return false;
            }
            int currentYear = Year.now().getValue();
            int earliestAllowedYear = currentYear - YEARS_LOOKBACK + 1;
            return fiscalYear >= earliestAllowedYear;
        };
    }

    /**
     * Parses a budget fiscal year string into its numeric representation.
     *
     * @param budgetFiscalYear the budget fiscal year to parse
     * @return the parsed fiscal year, or {@code null} if the value is blank
     * or cannot be parsed
     */
    private static Integer parseFiscalYear(String budgetFiscalYear) {
        if (budgetFiscalYear == null || budgetFiscalYear.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(budgetFiscalYear.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}