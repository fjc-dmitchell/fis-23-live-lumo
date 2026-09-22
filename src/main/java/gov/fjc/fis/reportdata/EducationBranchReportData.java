package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.dto.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static gov.fjc.fis.FisUtilities.*;

public class EducationBranchReportData {
    private final String budgetFiscalYear;
    private String priorBudgetFiscalYear;
    private final String divisionTitle;
    private final String branchTitle;
    private final String divisionAndBranch;
    private final LocalDateTime reportDateTime;

    private BigDecimal priorTwoYearProjected = BigDecimal.ZERO;
    private BigDecimal priorTwoYearObligated = BigDecimal.ZERO;
    private BigDecimal priorTwoYearDisbursed = BigDecimal.ZERO;
    private BigDecimal currentOneYearProjected = BigDecimal.ZERO;
    private BigDecimal currentOneYearObligated = BigDecimal.ZERO;
    private BigDecimal currentOneYearDisbursed = BigDecimal.ZERO;
    private BigDecimal currentTwoYearProjected = BigDecimal.ZERO;
    private BigDecimal currentTwoYearObligated = BigDecimal.ZERO;
    private BigDecimal currentTwoYearDisbursed = BigDecimal.ZERO;

    private List<GroupDto> groups;

    public EducationBranchReportData(Branch branch) {
        budgetFiscalYear = branch.getDivision().getAppropriation().getBudgetFiscalYear();
        divisionTitle = branch.getDivision().getTitle();
        branchTitle = branch.getTitle();
        divisionAndBranch = branch == null ? divisionTitle : divisionTitle.concat("-").concat(branch.getTitle());
        reportDateTime = getDateTime();
    }

    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    public String getPriorBudgetFiscalYear() {
        return priorBudgetFiscalYear;
    }

    public void setPriorBudgetFiscalYear(String priorBudgetFiscalYear) {
        this.priorBudgetFiscalYear = priorBudgetFiscalYear;
    }

    public List<GroupDto> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDto> groups) {
        this.groups = groups;
    }

    public BigDecimal getPriorTwoYearProjected() {
        return priorTwoYearProjected;
    }

    public void setPriorTwoYearProjected(BigDecimal priorTwoYearProjected) {
        this.priorTwoYearProjected = priorTwoYearProjected;
    }

    public BigDecimal getPriorTwoYearObligated() {
        return priorTwoYearObligated;
    }

    public void setPriorTwoYearObligated(BigDecimal priorTwoYearObligated) {
        this.priorTwoYearObligated = priorTwoYearObligated;
    }

    public BigDecimal getPriorTwoYearDisbursed() {
        return priorTwoYearDisbursed;
    }

    public void setPriorTwoYearDisbursed(BigDecimal priorTwoYearDisbursed) {
        this.priorTwoYearDisbursed = priorTwoYearDisbursed;
    }

    public BigDecimal getCurrentOneYearProjected() {
        return currentOneYearProjected;
    }

    public void setCurrentOneYearProjected(BigDecimal currentOneYearProjected) {
        this.currentOneYearProjected = currentOneYearProjected;
    }

    public BigDecimal getCurrentOneYearObligated() {
        return currentOneYearObligated;
    }

    public void setCurrentOneYearObligated(BigDecimal currentOneYearObligated) {
        this.currentOneYearObligated = currentOneYearObligated;
    }

    public BigDecimal getCurrentOneYearDisbursed() {
        return currentOneYearDisbursed;
    }

    public void setCurrentOneYearDisbursed(BigDecimal currentOneYearDisbursed) {
        this.currentOneYearDisbursed = currentOneYearDisbursed;
    }

    public BigDecimal getCurrentTwoYearProjected() {
        return currentTwoYearProjected;
    }

    public void setCurrentTwoYearProjected(BigDecimal currentTwoYearProjected) {
        this.currentTwoYearProjected = currentTwoYearProjected;
    }

    public BigDecimal getCurrentTwoYearObligated() {
        return currentTwoYearObligated;
    }

    public void setCurrentTwoYearObligated(BigDecimal currentTwoYearObligated) {
        this.currentTwoYearObligated = currentTwoYearObligated;
    }

    public BigDecimal getCurrentTwoYearDisbursed() {
        return currentTwoYearDisbursed;
    }

    public void setCurrentTwoYearDisbursed(BigDecimal currentTwoYearDisbursed) {
        this.currentTwoYearDisbursed = currentTwoYearDisbursed;
    }

    public BigDecimal getPriorTwoYearTotal() {
        return this.priorTwoYearProjected.add(this.priorTwoYearObligated).add(this.priorTwoYearDisbursed);
    }

    public BigDecimal getCurrentOneYearTotal() {
        return this.currentOneYearProjected.add(this.currentOneYearObligated).add(this.currentOneYearDisbursed);
    }

    public BigDecimal getCurrentTwoYearTotal() {
        return this.currentTwoYearProjected.add(this.currentTwoYearObligated).add(this.currentTwoYearDisbursed);
    }

    public String getReportDateTime() {
        return getDateTimeReportString(reportDateTime);
    }

    public String getDivisionAndBranch() {
        return divisionAndBranch;
    }

    public String getFileName() {
        return String.format(
                "%s programs for FY %s as of %s",
                branchTitle,
                budgetFiscalYear,
                getDateTimeFilenameString(reportDateTime)
        );
    }
}
