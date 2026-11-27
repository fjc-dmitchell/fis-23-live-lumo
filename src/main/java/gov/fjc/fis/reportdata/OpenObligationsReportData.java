package gov.fjc.fis.reportdata;

import gov.fjc.fis.app.StringUtils;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.ObligationDto;
import gov.fjc.fis.service.report.OpenObligationsReportService;
import gov.fjc.fis.view.report.openobligationsreport.OpenObligationsReportView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static gov.fjc.fis.app.BigDecimalUtils.sum;
import static gov.fjc.fis.app.FisUtilities.*;
import static gov.fjc.fis.app.DateUtils.*;

/**
 * Contains the data used to generate an Open Obligations Report.
 * <p>
 * The report identifies obligations that remain open beyond a specified aging
 * threshold. In addition to the obligation records included in the report,
 * this class stores the report parameters, organizational scope, report
 * generation timestamp, and summary information used by the report template.
 * </p>
 *
 * <p>
 * The organizational scope may represent one or more divisions or a specific
 * branch when the report is generated for a single division and branch.
 * </p>
 *
 * @author Doug Mitchell
 * @see OpenObligationsReportService
 * @see OpenObligationsReportView
 */
public class OpenObligationsReportData {

    /**
     * Budget fiscal year associated with the report.
     */
    private final String budgetFiscalYear;

    /**
     * Division or division-and-branch identifier describing the report scope.
     */
    private final String divisionAndBranch;

    /**
     * Division or division-and-branch identifier describing the report scope.
     */
    private final LocalDateTime reportDateTime;

    /**
     * Open obligations included in the report.
     */
    private final List<ObligationDto> obligations;

    /**
     * Aging threshold, in days, used to identify open obligations.
     */
    private final int numberOfDays;

    /**
     * Creates report data for the Open Obligations Report.
     *
     * @param appropriation appropriation associated with the report
     * @param divisions     divisions included in the report scope
     * @param branch        branch included in the report scope, or {@code null} when the
     *                      report spans multiple divisions or all branches
     * @param obligations   open obligations included in the report
     * @param numberOfDays  aging threshold, in days, used to determine whether an
     *                      obligation is considered open
     */
    public OpenObligationsReportData(Appropriation appropriation,
                                     Set<Division> divisions,
                                     Branch branch,
                                     List<ObligationDto> obligations,
                                     int numberOfDays) {
        budgetFiscalYear = appropriation == null ? "" : appropriation.getBudgetFiscalYear();
        String divisionTitles = StringUtils.joinWithAnd(divisions.stream()
                .sorted(Comparator.comparing(Division::getDivisionCode))
                .map(Division::getShortTitle).toList(), ", ");

        divisionAndBranch = (divisions.size() == 1 && branch != null)
                ? divisionTitles + "-" + branch.getTitle()
                : divisionTitles;

        this.obligations = obligations;
        this.numberOfDays = numberOfDays;

        reportDateTime = getDateTime();
    }

    /**
     * Returns the budget fiscal year associated with the report.
     *
     * @return the budget fiscal year
     */
    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    /**
     * Returns the open obligations included in the report.
     *
     * @return the report obligations, or an empty list if none are available
     */
    public List<ObligationDto> getObligations() {
        return obligations == null ? List.of() : obligations;
    }

    /**
     * Returns the number of obligations included in the report.
     *
     * @return the obligation count
     */
    public int getNumberOfObligations() {
        return obligations == null ? 0 : obligations.size();
    }

    /**
     * Returns the aging threshold used to identify open obligations.
     *
     * @return the number of days used by the report
     */
    public int getNumberOfDays() {
        return numberOfDays;
    }

    /**
     * Returns the total obligated amount represented by all obligations in the
     * report.
     *
     * @return the total obligated amount
     */
    public BigDecimal getTotalObligated() {
        return sum(obligations, ObligationDto::getAmount);
    }

    /**
     * Returns the organizational scope of the report.
     *
     * @return the report division and branch identifier
     */
    public String getDivisionAndBranch() {
        return divisionAndBranch;
    }

    /**
     * Returns the report generation date and time formatted for display.
     *
     * @return the formatted report generation date and time
     */
    public String getReportDateTime() {
        return formatReportDateTime(reportDateTime);
    }

    /**
     * Returns the suggested file name for the generated report.
     *
     * @return the report file name without an extension
     */
    public String getFileName() {
        return String.format(
                "%s open obligations for FY %s as of %s",
                divisionAndBranch,
                budgetFiscalYear,
                formatFilenameDateTime(reportDateTime)
        );
    }
}
