package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.ObligationDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static gov.fjc.fis.FisUtilities.*;

public class OpenTravelObligationsReportData {
    private final String budgetFiscalYear;
    private final String divisionTitles;
    private final LocalDateTime reportDateTime;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    private List<ObligationDto> obligations;

    private Date latestTravelDate;
    private BigDecimal totalObligated;

    public OpenTravelObligationsReportData(Appropriation appropriation, Set<Division> divisions,
                                           LocalDate fromDate, LocalDate toDate) {
        budgetFiscalYear = appropriation == null ? "" : appropriation.getBudgetFiscalYear();

        divisionTitles = formatListWithDelimiter(divisions.stream()
                .sorted(Comparator.comparing(Division::getDivisionCode))
                .map(Division::getShortTitle).toList(), ", ");

        this.fromDate = fromDate;
        this.toDate = toDate;

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

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public String getDivisionTitles() {
        return divisionTitles;
    }

    public String getReportDateTime() {
        return getDateTimeReportString(reportDateTime);
    }

    public String getDateString() {
        if (fromDate != null && toDate != null) {
            return fromDate.toString().concat(" to ").concat(toDate.toString());
        } else if (fromDate != null) {
            return fromDate.toString();
        } else if (toDate != null) {
            return toDate.toString();
        } else return "";
    }

    public String getFileName() {
        return String.format(
                "%s open travel authorizations %s as of %s",
                divisionTitles,
                getDateString(),
                getDateTimeFilenameString(reportDateTime)
        );
    }
}
