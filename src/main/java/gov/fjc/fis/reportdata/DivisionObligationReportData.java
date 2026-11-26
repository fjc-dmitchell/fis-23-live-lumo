package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.ActivityDto;

import java.time.LocalDateTime;
import java.util.List;

import static gov.fjc.fis.app.FisUtilities.*;
import static gov.fjc.fis.app.DateUtils.*;

/**
 * Contains the data used to generate a division obligations report.
 * <p>
 * The report data identifies the applicable budget fiscal year, division,
 * and optional branch. It also contains the activities included in the report,
 * the subset of those activities having obligations, and the date and time at
 * which the report data was created.
 * </p>
 * <p>
 * Display values derived from the supplied entities are captured when this
 * object is constructed. The report timestamp is retained internally so that
 * the displayed report date and generated filename use the same date and time.
 * </p>
 * @see gov.fjc.fis.service.report.DivisionObligationsReportService
 * @author Doug Mitchell
 */
public class DivisionObligationReportData {

    /**
     * The budget fiscal year displayed in the report.
     */
    private final String budgetFiscalYear;

    /**
     * The combined division and optional branch description used to identify
     * the report's organizational scope.
     */
    private final String divisionAndBranch;

    /**
     * The date and time at which the report data was created.
     */
    private final LocalDateTime reportDateTime;

    /**
     * All activities included within the report's organizational scope.
     */
    private List<ActivityDto> activityDtos;

    /**
     * The activities within the report's organizational scope that have one
     * or more obligations.
     */
    private List<ActivityDto> activitiesWithObligations;

    /**
     * Creates the data object for a division or branch obligations report.
     * <p>
     * When no branch is supplied, the report is identified by the division
     * title. When a branch is supplied, the division and branch titles are
     * combined to identify the report's organizational scope.
     * </p>
     *
     * @param appropriation the appropriation that supplies the budget fiscal
     *                      year, or {@code null} when no appropriation is
     *                      available
     * @param division      the division included in the report, or {@code null}
     *                      when no division is available
     * @param branch        the branch included in the report, or {@code null}
     *                      when the report covers the entire division
     */
    public DivisionObligationReportData(Appropriation appropriation, Division division, Branch branch) {
        budgetFiscalYear = appropriation == null ? "" : appropriation.getBudgetFiscalYear();
        var divisionTitle = division == null ? "" : division.getTitle();
        divisionAndBranch = branch == null ? divisionTitle : divisionTitle.concat("-").concat(branch.getTitle());
        reportDateTime = getDateTime();
    }

    /**
     * Returns all activities included within the report's organizational
     * scope.
     *
     * @return the activities included in the report
     */
    public List<ActivityDto> getActivityDtos() {
        return activityDtos;
    }

    /**
     * Sets all activities included within the report's organizational scope.
     *
     * @param activityDtos the activities to include in the report
     */
    public void setActivityDtos(List<ActivityDto> activityDtos) {
        this.activityDtos = activityDtos;
    }

    /**
     * Returns the budget fiscal year displayed in the report.
     *
     * @return the budget fiscal year, or an empty string when no appropriation
     *         was supplied
     */
    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    /**
     * Returns the activities that have one or more obligations.
     *
     * @return the activities having obligations
     */
    public List<ActivityDto> getActivitiesWithObligations() {
        return activitiesWithObligations;
    }

    /**
     * Sets the activities that have one or more obligations.
     *
     * @param activitiesWithObligations the activities having obligations
     */
    public void setActivitiesWithObligations(List<ActivityDto> activitiesWithObligations) {
        this.activitiesWithObligations = activitiesWithObligations;
    }

    /**
     * Returns the combined division and optional branch description.
     *
     * @return the division title when the report covers an entire division, or
     *         the combined division and branch titles when the report covers a
     *         branch
     */
    public String getDivisionAndBranch() {
        return divisionAndBranch;
    }

    /**
     * Returns the report creation date and time formatted for display.
     *
     * @return the formatted report creation date and time
     */
    public String getReportDateTime() {
        return formatReportDateTime(reportDateTime);
    }

    /**
     * Returns the suggested filename for the generated report.
     * <p>
     * The filename identifies the division and optional branch, budget fiscal
     * year, and report creation date and time.
     * </p>
     *
     * @return the suggested report filename without a file extension
     */
    public String getFileName() {
        return String.format(
                "%s obligations for FY %s as of %s",
                divisionAndBranch,
                budgetFiscalYear,
                formatFilenameDateTime(reportDateTime)
        );
    }
}
