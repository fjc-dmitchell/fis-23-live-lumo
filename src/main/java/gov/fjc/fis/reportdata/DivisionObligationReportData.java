package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.ActivityDto;

import java.time.LocalDateTime;
import java.util.List;

import static gov.fjc.fis.FisUtilities.*;

public class DivisionObligationReportData {
    private final String budgetFiscalYear;
    private String priorBudgetFiscalYear;
    private final String divisionTitle;
    private final String branchTitle;
    private final String divisionAndBranch;
    private final LocalDateTime reportDateTime;

    private List<ActivityDto> activityDtos;
    private List<ActivityDto> activitiesWithObligations;

    public DivisionObligationReportData(Appropriation appropriation, Division division, Branch branch) {
        budgetFiscalYear = appropriation == null ? "" : appropriation.getBudgetFiscalYear();
        divisionTitle = division == null ? "" : division.getTitle();
        branchTitle = branch == null ? null : ": ".concat(branch.getTitle());
        divisionAndBranch = branch == null ? divisionTitle : divisionTitle.concat("-").concat(branch.getTitle());
        reportDateTime = getDateTime();
    }

    public List<ActivityDto> getActivityDtos() {
        return activityDtos;
    }

    public void setActivityDtos(List<ActivityDto> activityDtos) {
        this.activityDtos = activityDtos;
    }

    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    public List<ActivityDto> getActivitiesWithObligations() {
        return activitiesWithObligations;
    }

    public void setActivitiesWithObligations(List<ActivityDto> activitiesWithObligations) {
        this.activitiesWithObligations = activitiesWithObligations;
    }

    public String getDivisionAndBranch() {
        return divisionAndBranch;
    }

    public String getReportDateTime() {
        return getDateTimeReportString(reportDateTime);
    }

    public String getFileName() {
        return String.format(
                "%s obligations for FY %s as of %s",
                divisionAndBranch,
                budgetFiscalYear,
                getDateTimeFilenameString(reportDateTime)
        );
    }
}
