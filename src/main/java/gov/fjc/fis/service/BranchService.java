package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.Branch;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("fis_BranchService")
public class BranchService {

    private final DataManager dataManager;

    public BranchService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public List<Branch> fetchBranchSearchList(List<Appropriation> fiscalYears, String divCode) {
        var descendingYears = fiscalYears.stream()
                .sorted(Comparator.comparing(Appropriation::getBudgetFiscalYear).reversed())
                .toList();
        List<Branch> branchList = new ArrayList<>();
        Set<String> branchCodes = null;

        for (Appropriation year : descendingYears) {
            List<Branch> branchesInBfyList;
            if(branchCodes == null) {
                // done for safety for first iteration since null set
                // behavior is not defined by JPQL specification
                branchesInBfyList = dataManager.load(Branch.class)
                        .query("SELECT e FROM fis_Branch e"
                                + " WHERE e.division.appropriation = :year"
                                + " AND e.division.divisionCode = :divCode")
                        .parameter("year", year)
                        .parameter("divCode", divCode)
                        .list();
            } else {
                branchesInBfyList = dataManager.load(Branch.class)
                        .query("SELECT e FROM fis_Branch e"
                                + " WHERE e.division.appropriation = :year"
                                + " AND e.division.divisionCode = :divCode"
                                + " AND e.branchCode NOT IN :branchCodes")
                        .parameter("year", year)
                        .parameter("divCode", divCode)
                        .parameter("branchCodes", branchCodes)
                        .list();
            }
            branchList.addAll(branchesInBfyList);
            branchCodes = branchList.stream().map(Branch::getBranchCode).collect(Collectors.toSet());
        }
        return branchList.stream().sorted(Comparator.comparing(Branch::getBranchCode)).toList();
    }

    public boolean branchesExist(Division division) {
        return !dataManager.load(Branch.class)
                .query("SELECT e FROM fis_Branch e WHERE e.division = :division")
                .parameter("division", division)
                .maxResults(1)
                .list()
                .isEmpty();
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