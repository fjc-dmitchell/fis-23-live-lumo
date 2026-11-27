package gov.fjc.fis.reportdata;

import gov.fjc.fis.app.StringUtils;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.ObligationDto;
import gov.fjc.fis.service.report.OpenTravelObligationsReportService;
import gov.fjc.fis.view.report.opentravelobligationsreport.OpenTravelObligationsReportView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gov.fjc.fis.app.BigDecimalUtils.sum;
import static gov.fjc.fis.app.DateUtils.*;
import static gov.fjc.fis.app.FisUtilities.*;

/**
 * Contains the data displayed in the open travel obligations report.
 *
 * <p>The report data identifies the budget fiscal year and selected divisions, preserves the
 * optional travel-end-date range used to generate the report, and provides the matching travel
 * obligations and their total obligated amount. It also supplies formatted values used for the
 * report timestamp, date-range description, and generated file name.</p>
 *
 * <p>The selected division titles are ordered by division code when this object is created. The
 * report date and time are also captured at construction so that the displayed timestamp and file
 * name remain consistent.</p>
 *
 * @see OpenTravelObligationsReportService
 * @see OpenTravelObligationsReportView
 * @author Doug Mitchell
 */
public class OpenTravelObligationsReportData {
    /**
     * The budget fiscal year associated with the report appropriation.
     */
    private final String budgetFiscalYear;
    /**
     * The formatted titles of the divisions included in the report.
     */
    private final String divisionTitles;
    /**
     * The date and time at which the report data object was created.
     */
    private final LocalDateTime reportDateTime;
    /**
     * The earliest travel end date included in the report, or {@code null} if unbounded.
     */
    private final LocalDate fromDate;
    /**
     * The latest travel end date included in the report, or {@code null} if unbounded.
     */
    private final LocalDate toDate;
    /**
     * The open travel obligations included in the report.
     */
    private List<ObligationDto> obligations = List.of();
    /**
     * The total amount of the obligations included in the report.
     */
    private BigDecimal totalObligated = BigDecimal.ZERO;

    /**
     * Creates report data for the specified appropriation, divisions, and optional date range.
     *
     * <p>Division titles are ordered by division code and joined into a display-friendly list. The
     * current date and time are captured for use in the report timestamp and generated file name.</p>
     *
     * @param appropriation the appropriation whose budget fiscal year is displayed
     * @param divisions     the divisions included in the report
     * @param fromDate      the earliest travel end date included, or {@code null} for no lower bound
     * @param toDate        the latest travel end date included, or {@code null} for no upper bound
     * @throws NullPointerException if {@code appropriation} or {@code divisions} is {@code null}
     */
    public OpenTravelObligationsReportData(Appropriation appropriation, Set<Division> divisions,
                                           LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(appropriation, "appropriation");
        Objects.requireNonNull(divisions, "divisions");

        budgetFiscalYear = appropriation.getBudgetFiscalYear();

        divisionTitles = StringUtils.joinWithAnd(divisions.stream()
                .sorted(Comparator.comparing(Division::getDivisionCode))
                .map(Division::getShortTitle)
                .toList(), ", ");

        this.fromDate = fromDate;
        this.toDate = toDate;

        reportDateTime = getDateTime();
    }

    /**
     * Returns the budget fiscal year associated with the report appropriation.
     *
     * @return the budget fiscal year
     */
    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    /**
     * Returns the open travel obligations included in the report.
     *
     * @return an unmodifiable list of report obligations
     */
    public List<ObligationDto> getObligations() {
        return obligations;
    }

    /**
     * Sets the obligations included in the report and recalculates the total obligated amount.
     *
     * <p>A defensive, unmodifiable copy of the supplied list is retained so that the obligation
     * list and calculated total cannot become inconsistent through subsequent list changes.</p>
     *
     * @param obligations the obligations to include in the report
     * @throws NullPointerException if {@code obligations} is {@code null}
     */
    public void setObligations(List<ObligationDto> obligations) {
        this.obligations = List.copyOf(obligations);
        this.totalObligated = sum(this.obligations, ObligationDto::getAmount);
    }

    /**
     * Returns the number of obligations included in the report.
     *
     * @return the obligation count
     */
    public Integer getNumberOfObligations() {
        return obligations.size();
    }

    /**
     * Returns the total amount of all obligations included in the report.
     *
     * @return the total obligated amount
     */
    public BigDecimal getTotalObligated() {
        return totalObligated;
    }

    /**
     * Returns the earliest travel end date included in the report.
     *
     * @return the lower date boundary, or {@code null} if no lower boundary was specified
     */
    public LocalDate getFromDate() {
        return fromDate;
    }

    /**
     * Returns the latest travel end date included in the report.
     *
     * @return the upper date boundary, or {@code null} if no upper boundary was specified
     */
    public LocalDate getToDate() {
        return toDate;
    }

    /**
     * Returns the formatted titles of the divisions included in the report.
     *
     * @return the division titles, ordered by division code and joined for display
     */
    public String getDivisionTitles() {
        return divisionTitles;
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
     * Returns a display-friendly description of the report's travel-end-date range.
     *
     * <p>When both dates are supplied, the result describes the complete range. When only one date
     * is supplied, the result describes the applicable lower or upper boundary. An empty string is
     * returned when the report has no date boundaries.</p>
     *
     * @return the formatted date-range description, or an empty string if no dates were specified
     */
    public String getDateString() {
        if (fromDate != null && toDate != null) {
            return fromDate + " to " + toDate;
        }
        if (fromDate != null) {
            return "from " + fromDate;
        }
        if (toDate != null) {
            return "through " + toDate;
        }
        return "";
    }

    /**
     * Builds the base file name for the generated open travel obligations report.
     *
     * <p>The file name contains the selected division titles, the report description, the optional
     * date-range description, and the report creation timestamp. Blank components are omitted.</p>
     *
     * @return the generated report file name without a file extension
     */
    public String getFileName() {
        return Stream.of(divisionTitles,
                        "open travel authorizations",
                        getDateString(),
                        "as of " + formatFilenameDateTime(reportDateTime))
                .filter(part -> !part.isBlank())
                .collect(Collectors.joining(" "));
    }
}