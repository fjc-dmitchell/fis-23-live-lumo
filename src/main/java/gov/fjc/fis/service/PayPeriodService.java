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
                .query("SELECT e FROM fis_PayPeriod e"
                +" WHERE e.payYear IN :years"
                +" ORDER BY e.payYear DESC, e.payPeriod")
                .parameter("years", yearList)
                .list();
    }

    public List<PayPeriod> getPayPeriods(PayPeriod currentPayPeriod) {
        if (currentPayPeriod == null) {
            throw new IllegalArgumentException("Current Pay Period cannot be null");
        }

        PayPeriod oldest = dataManager.load(PayPeriod.class)
                .query("SELECT DISTINCT p from fis_PositionAction e INNER JOIN fis_PayPeriod p ON p=e.payPeriod ORDER BY p.startDate ASC")
                .maxResults(1)
                .optional().orElse(currentPayPeriod);

        return dataManager.load(PayPeriod.class)
                .query("SELECT e FROM fis_PayPeriod e WHERE e.startDate >= :first")
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
                .query("SELECT e FROM fis_PayPeriod e"
                        + " WHERE e.endDate >= :firstDayOfFiscalYear and e.startDate <=:lastDayOfFiscalYear"
                        + " AND e.startDate >= :priorPayPeriodStartDate"
                        + " ORDER BY e.startDate")
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
                .query("SELECT e FROM fis_Appropriation e"
                        + " WHERE e.budgetFiscalYear >= :budgetFiscalYear"
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
                .query("SELECT e FROM fis_PayPeriod e"
                        + " WHERE e.startDate <= :today"
                        + " ORDER BY e.startDate DESC")
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
                .query("SELECT e FROM fis_PayPeriod e"
                        + " WHERE e.payYear = :year AND e.startDate < :cutoff"
                        + " ORDER BY e.startDate desc")
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
                .query("SELECT e FROM fis_Position e"
                        + " WHERE e.jlCostOrgCd = :jlCostOrgCd"
                        + " AND e.status <> 'I'"
                        + " ORDER BY e.name")
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
                        "SELECT e.projection-e.awarded FROM fis_BonusProjection e"
                                + " WHERE e.division = :division", Double.class)
                .parameter("division", division)
                .optional().orElse((double) 0);
    }

    public List<ActionCode> getActionCodes() {
        return dataManager.load(ActionCode.class)
                .query("SELECT e FROM fis_ActionCode e ORDER BY e.natureOfActionCode")
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
                .query("SELECT e FROM fis_PayPeriod  e"
                        + " WHERE e.startDate BETWEEN :firstStartDate AND :lastStartDate"
                        + " ORDER BY e.startDate")
                .parameter("firstStartDate", startingPayPeriod.getStartDate())
                .parameter("lastStartDate", finalPayPeriod.getStartDate())
                .list();
    }
}
