package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.ObligationDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static gov.fjc.fis.FisUtilities.*;

public class OpenObligationsReportData {

    private final String budgetFiscalYear;
    private final String divisionAndBranch;
    private final LocalDateTime reportDateTime;

    private List<ObligationDto> obligations;

    private int numberOfDays;
    private Date latestTravelDate;
    private BigDecimal totalObligated;
    private float averageDays;

    public OpenObligationsReportData(Appropriation appropriation, Division division, Branch branch) {
        budgetFiscalYear = appropriation == null ? "" : appropriation.getBudgetFiscalYear();
        String divisionTitle = division == null ? "" : division.getTitle();
        String branchTitle = branch == null ? null : ": ".concat(branch.getTitle());
        divisionAndBranch = branch == null ? divisionTitle : divisionTitle.concat("-").concat(branch.getTitle());
        reportDateTime = getDateTime();
    }

    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    public List<ObligationDto> getObligations() {
        return obligations;
    }

    public void setObligations(List<ObligationDto> obligations) {
        this.obligations = obligations;
    }

    public Integer getNumberOfObligations() {
        return obligations == null ? 0 : obligations.size();
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public Date getLatestTravelDate() {
        return latestTravelDate;
    }

    public void setLatestTravelDate(Date latestTravelDate) {
        this.latestTravelDate = latestTravelDate;
    }

    public BigDecimal getTotalObligated() {
        return totalObligated;
    }

    public void setTotalObligated(BigDecimal totalObligated) {
        this.totalObligated = totalObligated;
    }

    public float getAverageDays() {
        return averageDays;
    }

    public void setAverageDays(float averageDays) {
        this.averageDays = averageDays;
    }

    public String getDivisionAndBranch() {
        return divisionAndBranch;
    }

    public String getReportDateTime() {
        return getDateTimeReportString(reportDateTime);
    }

    public String getFileName() {
        return String.format(
                "%s open obligations for FY %s as of %s",
                divisionAndBranch,
                budgetFiscalYear,
                getDateTimeFilenameString(reportDateTime)
        );
    }
}
