package gov.fjc.fis.security;

import gov.fjc.fis.entity.Appropriation;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPredicate;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

import java.time.Year;

@RowLevelRole(name = "AppropriationYearsData", code = AppropriationYearsDataRole.CODE)
public interface AppropriationYearsDataRole {
    String CODE = "data-appropriation-years";

    int YEARS_LOOKBACK = 5;

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