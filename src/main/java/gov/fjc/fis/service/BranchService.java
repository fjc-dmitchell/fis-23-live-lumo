package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides operations for retrieving branches defined within FIS divisions.
 * <p>
 * Branches provide a way to organize related activities within a division.
 * Because divisions and their branches may be defined for multiple fiscal
 * years, this service also supports retrieving the most recent branch
 * associated with each branch code across a collection of appropriations.
 *
 * @author Doug Mitchell
 */
@Component("fis_BranchService")
public class BranchService {

    private final DataManager dataManager;

    public BranchService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Returns the branches available for a division code across the specified
     * fiscal years.
     * <p>
     * If the same branch code exists in more than one fiscal year, only the
     * branch from the most recent budget fiscal year is returned. The resulting
     * list is ordered by branch code.
     *
     * @param fiscalYears the appropriations representing the fiscal years to search
     * @param divCode the code of the division whose branches are requested
     * @return the most recent branch for each branch code, or an empty list if
     *         no fiscal years are provided or no matching branches exist
     */
    public List<Branch> fetchBranchSearchList(Collection<Appropriation> fiscalYears, String divCode) {
        if (fiscalYears.isEmpty()) {
            return List.of();
        }

        List<Branch> branchesNewestYearFirst = dataManager.load(Branch.class)
                .query("SELECT e FROM fis_Branch e"
                        + " WHERE e.division.appropriation IN :years"
                        + " AND e.division.divisionCode = :divCode"
                        + " ORDER BY e.division.appropriation.budgetFiscalYear DESC")
                .parameter("years", fiscalYears)
                .parameter("divCode", divCode)
                .list();

        Map<String, Branch> latestBranchByCode = new TreeMap<>();
        for (Branch branch : branchesNewestYearFirst) {
            latestBranchByCode.putIfAbsent(branch.getBranchCode(), branch);
        }
        return List.copyOf(latestBranchByCode.values());
    }

    /**
     * Determines whether any branches are defined for the specified division.
     *
     * @param division the division to examine
     * @return {@code true} if at least one branch is defined for the division;
     *         otherwise {@code false}
     */
    public boolean branchesExist(Division division) {
        return dataManager.load(Branch.class)
                .query("SELECT e FROM fis_Branch e WHERE e.division = :division")
                .parameter("division", division)
                .maxResults(1)
                .optional()
                .isPresent();
    }

    public List<Branch> fetchBranches(Division division) {
        return dataManager.load(Branch.class)
                .query("SELECT e FROM fis_Branch e"
                        + " WHERE e.division = :division"
                        + " ORDER BY e.branchCode")
                .parameter("division", division)
                .list();
    }
}