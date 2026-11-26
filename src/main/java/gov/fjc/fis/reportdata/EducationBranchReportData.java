package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.dto.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import gov.fjc.fis.service.report.EducationBranchReportService;
import gov.fjc.fis.view.report.educationbranchreport.EducationBranchReportView;

import static gov.fjc.fis.app.FisUtilities.*;
import static gov.fjc.fis.app.DateUtils.*;

/**
 * Contains the data used to generate an Education Branch Report.
 * <p>
 * The report summarizes projected, obligated, and disbursed amounts for a
 * branch across the prior two-year, current one-year, and current two-year
 * funding periods. Detailed report data is organized into groups containing
 * the activities and budget object class information included in the report.
 * </p>
 *
 * <p>
 * Fiscal-year and branch information is initialized from the branch supplied
 * at construction. The report date and time are also captured during
 * construction so that the displayed report date and generated file name refer
 * to the same point in time.
 * </p>
 * @see EducationBranchReportService
 * @see EducationBranchReportView
 * @author Doug Mitchell
 */
public class EducationBranchReportData {

    /**
     * Budget fiscal year associated with the branch's current appropriation.
     */
    private final String budgetFiscalYear;

    /**
     * Budget fiscal year associated with the preceding appropriation.
     */
    private String priorBudgetFiscalYear;

    /**
     * Title of the branch for which the report is generated.
     */
    private final String branchTitle;

    /**
     * Combined division and branch title used to identify the report's
     * organizational scope.
     */
    private final String divisionAndBranch;

    /**
     * Date and time at which the report data object was created.
     */
    private final LocalDateTime reportDateTime;

    /**
     * Total projected amount for the prior two-year funding period.
     */
    private BigDecimal priorTwoYearProjected = BigDecimal.ZERO;

    /**
     * Total obligated amount for the prior two-year funding period.
     */
    private BigDecimal priorTwoYearObligated = BigDecimal.ZERO;

    /**
     * Total disbursed amount for the prior two-year funding period.
     */
    private BigDecimal priorTwoYearDisbursed = BigDecimal.ZERO;

    /**
     * Total projected amount for the current one-year funding period.
     */
    private BigDecimal currentOneYearProjected = BigDecimal.ZERO;

    /**
     * Total obligated amount for the current one-year funding period.
     */
    private BigDecimal currentOneYearObligated = BigDecimal.ZERO;

    /**
     * Total disbursed amount for the current one-year funding period.
     */
    private BigDecimal currentOneYearDisbursed = BigDecimal.ZERO;

    /**
     * Total projected amount for the current two-year funding period.
     */
    private BigDecimal currentTwoYearProjected = BigDecimal.ZERO;

    /**
     * Total obligated amount for the current two-year funding period.
     */
    private BigDecimal currentTwoYearObligated = BigDecimal.ZERO;

    /**
     * Total disbursed amount for the current two-year funding period.
     */
    private BigDecimal currentTwoYearDisbursed = BigDecimal.ZERO;

    /**
     * Groups containing the activity details included in the report.
     */
    private List<GroupDto> groups;

    /**
     * Creates report data for the specified branch.
     * <p>
     * The current budget fiscal year, branch title, and combined division and
     * branch title are derived from the supplied branch. The report date and
     * time are captured when this object is created.
     * </p>
     *
     * @param branch the branch for which the report is generated
     */
    public EducationBranchReportData(Branch branch) {
        budgetFiscalYear = branch.getDivision().getAppropriation().getBudgetFiscalYear();
        branchTitle = branch.getTitle();
        var divisionTitle = branch.getDivision().getTitle();
        divisionAndBranch = divisionTitle.concat("-").concat(branch.getTitle());
        reportDateTime = getDateTime();
    }

    /**
     * Returns the budget fiscal year associated with the branch's current
     * appropriation.
     *
     * @return the current budget fiscal year
     */
    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    /**
     * Returns the budget fiscal year associated with the preceding
     * appropriation.
     *
     * @return the prior budget fiscal year
     */
    public String getPriorBudgetFiscalYear() {
        return priorBudgetFiscalYear;
    }

    /**
     * Sets the budget fiscal year associated with the preceding appropriation.
     *
     * @param priorBudgetFiscalYear the prior budget fiscal year
     */
    public void setPriorBudgetFiscalYear(String priorBudgetFiscalYear) {
        this.priorBudgetFiscalYear = priorBudgetFiscalYear;
    }

    /**
     * Returns the groups containing the report's activity details.
     *
     * @return the report groups
     */
    public List<GroupDto> getGroups() {
        return groups;
    }

    /**
     * Sets the groups containing the report's activity details.
     *
     * @param groups the report groups
     */
    public void setGroups(List<GroupDto> groups) {
        this.groups = groups;
    }

    /**
     * Returns the total projected amount for the prior two-year funding period.
     *
     * @return the prior two-year projected amount
     */
    public BigDecimal getPriorTwoYearProjected() {
        return priorTwoYearProjected;
    }

    /**
     * Sets the total projected amount for the prior two-year funding period.
     *
     * @param priorTwoYearProjected the prior two-year projected amount
     */
    public void setPriorTwoYearProjected(BigDecimal priorTwoYearProjected) {
        this.priorTwoYearProjected = priorTwoYearProjected;
    }

    /**
     * Returns the total obligated amount for the prior two-year funding period.
     *
     * @return the prior two-year obligated amount
     */
    public BigDecimal getPriorTwoYearObligated() {
        return priorTwoYearObligated;
    }

    /**
     * Sets the total obligated amount for the prior two-year funding period.
     *
     * @param priorTwoYearObligated the prior two-year obligated amount
     */
    public void setPriorTwoYearObligated(BigDecimal priorTwoYearObligated) {
        this.priorTwoYearObligated = priorTwoYearObligated;
    }

    /**
     * Returns the total disbursed amount for the prior two-year funding period.
     *
     * @return the prior two-year disbursed amount
     */
    public BigDecimal getPriorTwoYearDisbursed() {
        return priorTwoYearDisbursed;
    }

    /**
     * Sets the total disbursed amount for the prior two-year funding period.
     *
     * @param priorTwoYearDisbursed the prior two-year disbursed amount
     */
    public void setPriorTwoYearDisbursed(BigDecimal priorTwoYearDisbursed) {
        this.priorTwoYearDisbursed = priorTwoYearDisbursed;
    }

    /**
     * Returns the total projected amount for the current one-year funding
     * period.
     *
     * @return the current one-year projected amount
     */
    public BigDecimal getCurrentOneYearProjected() {
        return currentOneYearProjected;
    }

    /**
     * Sets the total projected amount for the current one-year funding period.
     *
     * @param currentOneYearProjected the current one-year projected amount
     */
    public void setCurrentOneYearProjected(BigDecimal currentOneYearProjected) {
        this.currentOneYearProjected = currentOneYearProjected;
    }

    /**
     * Returns the total obligated amount for the current one-year funding
     * period.
     *
     * @return the current one-year obligated amount
     */
    public BigDecimal getCurrentOneYearObligated() {
        return currentOneYearObligated;
    }

    /**
     * Sets the total obligated amount for the current one-year funding period.
     *
     * @param currentOneYearObligated the current one-year obligated amount
     */
    public void setCurrentOneYearObligated(BigDecimal currentOneYearObligated) {
        this.currentOneYearObligated = currentOneYearObligated;
    }

    /**
     * Returns the total disbursed amount for the current one-year funding
     * period.
     *
     * @return the current one-year disbursed amount
     */
    public BigDecimal getCurrentOneYearDisbursed() {
        return currentOneYearDisbursed;
    }

    /**
     * Sets the total disbursed amount for the current one-year funding period.
     *
     * @param currentOneYearDisbursed the current one-year disbursed amount
     */
    public void setCurrentOneYearDisbursed(BigDecimal currentOneYearDisbursed) {
        this.currentOneYearDisbursed = currentOneYearDisbursed;
    }

    /**
     * Returns the total projected amount for the current two-year funding
     * period.
     *
     * @return the current two-year projected amount
     */
    public BigDecimal getCurrentTwoYearProjected() {
        return currentTwoYearProjected;
    }

    /**
     * Sets the total projected amount for the current two-year funding period.
     *
     * @param currentTwoYearProjected the current two-year projected amount
     */
    public void setCurrentTwoYearProjected(BigDecimal currentTwoYearProjected) {
        this.currentTwoYearProjected = currentTwoYearProjected;
    }

    /**
     * Returns the total obligated amount for the current two-year funding
     * period.
     *
     * @return the current two-year obligated amount
     */
    public BigDecimal getCurrentTwoYearObligated() {
        return currentTwoYearObligated;
    }

    /**
     * Sets the total obligated amount for the current two-year funding period.
     *
     * @param currentTwoYearObligated the current two-year obligated amount
     */
    public void setCurrentTwoYearObligated(BigDecimal currentTwoYearObligated) {
        this.currentTwoYearObligated = currentTwoYearObligated;
    }

    /**
     * Returns the total disbursed amount for the current two-year funding
     * period.
     *
     * @return the current two-year disbursed amount
     */
    public BigDecimal getCurrentTwoYearDisbursed() {
        return currentTwoYearDisbursed;
    }

    /**
     * Sets the total disbursed amount for the current two-year funding period.
     *
     * @param currentTwoYearDisbursed the current two-year disbursed amount
     */
    public void setCurrentTwoYearDisbursed(BigDecimal currentTwoYearDisbursed) {
        this.currentTwoYearDisbursed = currentTwoYearDisbursed;
    }

    /**
     * Returns the combined projected, obligated, and disbursed amounts for the
     * prior two-year funding period.
     *
     * @return the total amount for the prior two-year funding period
     */
    public BigDecimal getPriorTwoYearTotal() {
        return this.priorTwoYearProjected
                .add(this.priorTwoYearObligated)
                .add(this.priorTwoYearDisbursed);
    }

    /**
     * Returns the combined projected, obligated, and disbursed amounts for the
     * current one-year funding period.
     *
     * @return the total amount for the current one-year funding period
     */
    public BigDecimal getCurrentOneYearTotal() {
        return this.currentOneYearProjected
                .add(this.currentOneYearObligated)
                .add(this.currentOneYearDisbursed);
    }

    /**
     * Returns the combined projected, obligated, and disbursed amounts for the
     * current two-year funding period.
     *
     * @return the total amount for the current two-year funding period
     */
    public BigDecimal getCurrentTwoYearTotal() {
        return this.currentTwoYearProjected
                .add(this.currentTwoYearObligated)
                .add(this.currentTwoYearDisbursed);
    }

    /**
     * Returns the report creation date and time formatted for display.
     *
     * @return the formatted report date and time
     */
    public String getReportDateTime() {
        return formatReportDateTime(reportDateTime);
    }

    /**
     * Returns the combined division and branch title identifying the report's
     * organizational scope.
     *
     * @return the combined division and branch title
     */
    public String getDivisionAndBranch() {
        return divisionAndBranch;
    }

    /**
     * Returns the suggested report file name.
     * <p>
     * The file name includes the branch title, current budget fiscal year, and
     * report creation date and time.
     * </p>
     *
     * @return the suggested report file name without an extension
     */
    public String getFileName() {
        return String.format(
                "%s programs for FY %s as of %s",
                branchTitle,
                budgetFiscalYear,
                formatFilenameDateTime(reportDateTime)
        );
    }
}
