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

//    public List<PayPeriod> getPayPeriods(Appropriation appropriation, PayPeriod payPeriod, int nothing) {
//        if (appropriation == null) {
//            throw new IllegalArgumentException("Appropriation cannot be null");
//        }
//        if (payPeriod == null) {
//            throw new IllegalArgumentException("Pay Period cannot be null");
//        }
//        var year = Integer.parseInt(appropriation.getBudgetFiscalYear());
//
//        if (payPeriod.getAppropriation().equals(appropriation)) {
//            var priorPayPeriodStart = payPeriod.getStartDate().minusWeeks(2);
//
//            return dataManager.load(PayPeriod.class)
//                    .query("SELECT p FROM fis_PayPeriod p"
//                            + " WHERE p.appropriation = :appropriation AND p.startDate >= :startDate"
//                            + " ORDER BY p.startDate DESC")
//                    .parameter("appropriation", appropriation)
//                    .parameter("startDate", priorPayPeriodStart)
//                    .list();
//        } else {
//            return dataManager.load(PayPeriod.class)
//                    .query("SELECT p FROM fis_PayPeriod p"
//                            + " WHERE p.payYear = :priorYear AND EXTRACT(MONTH FROM p.startDate) IN (10, 11, 12)"
//                            + " OR p.payYear = :year AND EXTRACT(MONTH FROM p.startDate) < 10"
//                            + " ORDER BY p.startDate DESC")
//                    .parameter("year", year)
//                    .parameter("priorYear", year - 1)
//                    .list();
//        }
//    }

//    public List<PayPeriod> getPayPeriods(Appropriation appropriation) {
//        if (appropriation == null) {
//            throw new IllegalArgumentException("Appropriation cannot be null");
//        }
//        var year = Integer.parseInt(appropriation.getBudgetFiscalYear());
//
//        return dataManager.load(PayPeriod.class)
//                .query("SELECT p FROM fis_PayPeriod p"
//                        + " WHERE p.payYear = :priorYear AND EXTRACT(MONTH FROM p.startDate) IN (10, 11, 12)"
//                        + " OR p.payYear = :year AND EXTRACT(MONTH FROM p.startDate) < 10"
//                        + " ORDER BY p.startDate ASC")
//                .parameter("year", year)
//                .parameter("priorYear", year - 1)
//                .list();
//    }

//    public List<PayPeriod> getPayPeriods(PayPeriod payPeriod) {
//        if (payPeriod == null) {
//            throw new IllegalArgumentException("Pay Period cannot be null");
//        }
//        var priorPayPeriodStart = payPeriod.getStartDate().minusDays(14);
//        var appropriation = payPeriod.getAppropriation();
//
//        return dataManager.load(PayPeriod.class)
//                .query("SELECT p FROM fis_PayPeriod p"
//                        + " WHERE p.appropriation = :appropriation AND p.startDate >= :startDate"
//                        + " ORDER BY p.startDate DESC")
//                .parameter("appropriation", appropriation)
//                .parameter("startDate", priorPayPeriodStart)
//                .list();
//    }

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

//    public List<Appropriation> getAppropriations(Appropriation appropriation) {
//        var year = Integer.parseInt(appropriation.getBudgetFiscalYear());
//        return dataManager.load(Appropriation.class)
//                .query("SELECT a FROM fis_Appropriation a"
//                        +" WHERE a.budgetFiscalYear >= :appropriationYear"
//                +" AND EXISTS (SELECT p FROM fis_PayPeriod p WHERE p.payYear )")
//    }

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

    public int countFullPayPeriods(PayPeriod startingPayPeriod, PayPeriod finalPayPeriod) {
        var firstPeriodStartDate = startingPayPeriod.getStartDate();
        var finalPeriodStartDate = finalPayPeriod.getStartDate();
        return dataManager.loadValue(
                        "SELECT COUNT(p) FROM fis_PayPeriod p"
                                + " WHERE p.startDate >= :firstPeriodStartDate"
                                + " AND p.startDate < :finalPeriodStartDate", Integer.class)
                .parameter("firstPeriodStartDate", firstPeriodStartDate)
                .parameter("finalPeriodStartDate", finalPeriodStartDate)
                .optional().orElse(0);
    }

    public DayOfWeek getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek();
    }

    public long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Returns the number of business days in the given pay period that fall
     * before October 1 (i.e., within the current federal fiscal year).
     * <p>
     * Days 6, 7, 13, and 14 of the pay period are Saturday/Sunday and are
     * excluded. All remaining days are weekdays (Mon–Fri).
     *
     * @param payPeriod the last pay period of the fiscal year (may straddle Sep 30)
     * @return count of business days occurring before October 1
     */
//    public int businessDaysInPayPeriod(PayPeriod payPeriod) {
//        LocalDate start = payPeriod.getStartDate();
//        LocalDate fiscalYearEnd = LocalDate.of(start.getYear(), 9, 30);
//
//        // If FY end is in the next calendar year relative to start, adjust.
//        // (Unlikely for a FY-straddling pay period, but defensive.)
//        if (fiscalYearEnd.isBefore(start)) {
//            fiscalYearEnd = fiscalYearEnd.plusYears(1);
//        }
//
//        int count = 0;
//        for (int day = 1; day <= 14; day++) {
//            boolean isWeekend = (day == 6 || day == 7 || day == 13 || day == 14);
//            if (isWeekend) continue;
//
//            LocalDate date = start.plusDays(day - 1);
//            if (!date.isAfter(fiscalYearEnd)) {
//                count++;
//            }
//        }
//        return count;
//    }

    public LocalDate getLastDayOfFiscalYear(PayPeriod payPeriod) {
        LocalDate start = payPeriod.getStartDate();
        LocalDate fiscalYearEnd = LocalDate.of(start.getYear(), 9, 30);

        if (fiscalYearEnd.isBefore(start)) {
            fiscalYearEnd = fiscalYearEnd.plusYears(1);
        }
        return fiscalYearEnd;
    }

    public int businessDaysBetweenOld(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            return 0;
        }

        int businessDays = 0;
        LocalDate date = start;

        while (!date.isAfter(end)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                businessDays++;
            }
            date = date.plusDays(1);
        }

        return businessDays;
    }

    public int businessDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (int) startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }

    //    public PositionAction getRecentPositionAction(PayPeriod payPeriod) {
//        LocalDate startDate = payPeriod.getStartDate();
//        return dataManager.load(PositionAction.class)
//                .query("SELECT a FROM fis_PositionAction a"
//                        +" WHERE a.payPeriod.startDate <= :startDate"
//                        +" ORDER BY a.payPeriod.startDate DESC")
//                .parameter("startDate", startDate)
//                .maxResults(1)
//                .optional()
//                .orElse(null);
//    }
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

//    public int getNumberPaidDays(PayPeriod startingPayPeriod) {
//        int calendarYear;
//        if (startingPayPeriod.getStartDate().getMonthValue() >= 10) {
//            calendarYear = startingPayPeriod.getPayYear() + 1;
//        } else {
//            calendarYear = startingPayPeriod.getPayYear();
//        }
//
//
//        var finalPayPeriod = fetchLastPayPeriodOfFiscalYear(calendarYear);
//        var numberPayPeriods = countFullPayPeriods(startingPayPeriod, finalPayPeriod);
//
//        var numberDays = businessDaysInPayPeriod(finalPayPeriod);
//        return numberPayPeriods * 10 + numberDays;
//    }

//    public List<Position> getPositions(Division division) {
//        return dataManager.load(Position.class)
//                .query("SELECT p from fis_Position p"
//                        + " WHERE p.jlCostOrgCd= :jlCostOrgCd"
//                        + " ORDER BY p.name")
//                .parameter("jlCostOrgCd", division.getBudgetOrg())
//                .list();
//    }

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
