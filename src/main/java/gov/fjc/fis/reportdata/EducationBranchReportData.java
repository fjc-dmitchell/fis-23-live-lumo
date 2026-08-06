package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.dto.ActivityDto;
import gov.fjc.fis.entity.dto.ActivityProjectionDto;
import gov.fjc.fis.entity.dto.ActivityReimbursementDto;
import gov.fjc.fis.entity.dto.ObligationDto;

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

    private List<ActivityDto> activities;
    private List<ActivityReimbursementDto> reimbursements;
    private List<ActivityProjectionDto> projections;
    private List<ObligationDto> obligations;

    public EducationBranchReportData(Branch branch, List<ActivityDto> activities) {
        budgetFiscalYear = branch.getDivision().getAppropriation().getBudgetFiscalYear();
        divisionTitle = branch.getDivision().getTitle();
        branchTitle = branch.getTitle();
        divisionAndBranch = branch == null ? divisionTitle : divisionTitle.concat("-").concat(branch.getTitle());
        this.activities = activities;

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

    public List<ActivityDto> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityDto> activities) {
        this.activities = activities;
    }

    public String getReportDateTime() {
        return getDateTimeReportString(reportDateTime);
    }

    public String getDivisionAndBranch() {
        return divisionAndBranch;
    }

    public String getFileName() {
        return branchTitle
                .concat(" programs for FY ")
                .concat(budgetFiscalYear)
                .concat(" as of ")
                .concat(getDateTimeFilenameString(reportDateTime));
    }
}
