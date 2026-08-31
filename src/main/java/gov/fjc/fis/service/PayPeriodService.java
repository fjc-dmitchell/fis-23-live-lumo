package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.personnel.*;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.LoadContext;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component("fis_PayPeriodService")
public class PayPeriodService {
    private final DataManager dataManager;

    public PayPeriodService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    // not good, fiscal years to calendar years
    public List<PayPeriod> getPayPeriods(List<Appropriation> appropriations) {
        List<Integer> yearList = appropriations.stream()
                .map(Appropriation::getBudgetFiscalYear)
                .map(Integer::valueOf)
                .toList();
        return dataManager.load(PayPeriod.class)
                .query("SELECT p FROM fis_PayPeriod p"
                +" WHERE p.payYear IN :years"
                +" ORDER BY p.payYear DESC, p.payPeriod")
                .parameter("years", yearList)
                .list();
    }

    public List<PayPeriod> getPayPeriods(PayPeriod currentPayPeriod) {
        if (currentPayPeriod == null) {
            throw new IllegalArgumentException("Current Pay Period cannot be null");
        }

        PayPeriod oldest = dataManager.load(PayPeriod.class)
                .query("SELECT DISTINCT p from fis_PositionAction a INNER JOIN fis_PayPeriod p ON p=a.payPeriod ORDER BY p.startDate ASC")
                .maxResults(1)
                .optional().orElse(currentPayPeriod);

        return dataManager.load(PayPeriod.class)
                .query("SELECT p FROM fis_PayPeriod p WHERE p.startDate >= :first")
                .parameter("first", oldest.getStartDate())
                .list();
    }

    public List<PayPeriod> getPayPeriods(Appropriation appropriation, PayPeriod currentPayPeriod) {
        if (appropriation == null) {
            throw new IllegalArgumentException("Appropriation cannot be null");
        }
        if (currentPayPeriod == null) {
            throw new IllegalArgumentException("Current Pay Period cannot be null");
        }

        var priorPayPeriodStartDate = currentPayPeriod.getStartDate().minusWeeks(2);
        var year = Integer.parseInt(appropriation.getBudgetFiscalYear());
        var lastYear = year - 1;

        var firstDayOfFiscalYear = LocalDate.of(lastYear, 10, 1);
        var lastDayOfFiscalYear = LocalDate.of(year, 9, 30);

        return dataManager.load(PayPeriod.class)
                .query("SELECT p FROM fis_PayPeriod p"
                        + " WHERE p.endDate >= :firstDayOfFiscalYear and p.startDate <=:lastDayOfFiscalYear"
                        + " AND p.startDate >= :priorPayPeriodStartDate"
                        + " ORDER BY p.startDate")
                .parameter("firstDayOfFiscalYear", firstDayOfFiscalYear)
                .parameter("lastDayOfFiscalYear", lastDayOfFiscalYear)
                .parameter("priorPayPeriodStartDate", priorPayPeriodStartDate)
                .list();
    }

    public List<Appropriation> getAppropriations(Appropriation appropriation) {

        var year = Integer.parseInt(appropriation.getBudgetFiscalYear());
        var lastYear = year - 1;
        var firstDayOfFiscalYear = LocalDate.of(lastYear, 10, 1);
        var lastDayOfFiscalYear = LocalDate.of(year, 9, 30);
        return dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.budgetFiscalYear >= :budgetFiscalYear"
                        + " AND EXISTS (SELECT p FROM fis_PayPeriod p"
                        + " WHERE p.endDate >= :firstDayOfFiscalYear and p.startDate <=:lastDayOfFiscalYear )")
                .parameter("budgetFiscalYear", appropriation.getBudgetFiscalYear())
                .parameter("firstDayOfFiscalYear", firstDayOfFiscalYear)
                .parameter("lastDayOfFiscalYear", lastDayOfFiscalYear)
                .list();
    }

    public PayPeriod fetchCurrentPayPeriod() {
        var today = LocalDate.now();

        return dataManager.load(PayPeriod.class)
                .query("SELECT p FROM fis_PayPeriod p"
                        + " WHERE p.startDate <= :today"
                        + " ORDER BY p.startDate DESC")
                .parameter("today", today)
                .maxResults(1)
                .optional().orElse(null);
    }

    /**
     * returns the final pay period of fiscal year
     *
     * @param calendarYear the calendar year of the pay periods
     * @return pay period that may include days of the next fiscal year
     */
    public PayPeriod fetchLastPayPeriodOfFiscalYear(int calendarYear) {
        LocalDate cutoff = LocalDate.of(calendarYear, 10, 1);

        return dataManager.load(PayPeriod.class)
                .query("SELECT p FROM fis_PayPeriod p"
                        + " WHERE p.payYear = :year AND p.startDate < :cutoff"
                        + " ORDER BY p.startDate desc")
                .parameter("year", calendarYear)
                .parameter("cutoff", cutoff)
                .maxResults(1)
                .optional()
                .orElse(null);
    }

    public LocalDate getLastDayOfFiscalYear(PayPeriod payPeriod) {
        LocalDate start = payPeriod.getStartDate();
        LocalDate fiscalYearEnd = LocalDate.of(start.getYear(), 9, 30);

        if (fiscalYearEnd.isBefore(start)) {
            fiscalYearEnd = fiscalYearEnd.plusYears(1);
        }
        return fiscalYearEnd;
    }

    public int businessDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (int) startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }

    public List<Position> fetchPositions(Division division) {
        Objects.requireNonNull(division, "division must not be null");
        String jlCostOrgCd = division.getBudgetOrg();
        if (jlCostOrgCd == null) {
            return List.of();
        }
        return dataManager.load(Position.class)
                .query("SELECT p FROM fis_Position p"
                        + " WHERE p.jlCostOrgCd = :jlCostOrgCd"
                        + " AND p.status <> 'I'"
                        + " ORDER BY p.name")
                .parameter("jlCostOrgCd", jlCostOrgCd)
                .fetchPlan(fp -> fp
                        .addFetchPlan(FetchPlan.BASE)
                        .add("actions", actionFp -> actionFp
                                .addFetchPlan(FetchPlan.BASE)
                                .add("payPeriod", payPeriodFp -> payPeriodFp
                                        .addFetchPlan(FetchPlan.BASE)
                                        .add("startDate"))))
                .list();
    }

    public Double getBonusProjections(Division division) {
        return dataManager.loadValue(
                        "SELECT b.projection-b.awarded FROM fis_BonusProjection b"
                                + " WHERE b.division = :division", Double.class)
                .parameter("division", division)
                .optional().orElse((double) 0);
    }

    public List<ActionCode> getActionCodes() {
        return dataManager.load(ActionCode.class)
                .query("SELECT a FROM fis_ActionCode a ORDER BY a.natureOfActionCode")
                .list();
    }

    public List<PayPeriod> fetchPayPeriods(PayPeriod startingPayPeriod) {
        int calendarYear;
        if (startingPayPeriod.getStartDate().getMonthValue() >= 10) {
            calendarYear = startingPayPeriod.getPayYear() + 1;
        } else {
            calendarYear = startingPayPeriod.getPayYear();
        }
        var finalPayPeriod = fetchLastPayPeriodOfFiscalYear(calendarYear);
        return dataManager.load(PayPeriod.class)
                .query("SELECT p FROM fis_PayPeriod  p"
                        + " WHERE p.startDate BETWEEN :firstStartDate AND :lastStartDate"
                        + " ORDER BY p.startDate")
                .parameter("firstStartDate", startingPayPeriod.getStartDate())
                .parameter("lastStartDate", finalPayPeriod.getStartDate())
                .list();
    }
}
