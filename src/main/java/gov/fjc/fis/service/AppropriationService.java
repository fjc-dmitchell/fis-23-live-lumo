package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.session.SessionData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.*;

@Component("fis_AppropriationService")
public class AppropriationService {

    private final DataManager dataManager;

    public AppropriationService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    // refactor to reconciliation service, use unconstrained datamanager(?)
    public List<Appropriation> getReconciliationAppropriations(int numberOfYears) {
        String currentBfy = getCurrentBfy();
        return dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.budgetFiscalYear <= :currentBfy"
                        + " ORDER BY a.budgetFiscalYear DESC")
                .parameter("currentBfy", currentBfy)
                .maxResults(numberOfYears)
                .list();
    }

    public List<Appropriation> getAppropriations() {
        return dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a ORDER BY a.budgetFiscalYear DESC")
                .list();
    }

    /**
     * adhoc request for multi-year program analysis.
     *
     * @param budgetFiscalYears list of fiscal year strings
     * @return List of appropriation entities
     */
    public List<Appropriation> getAppropriations(List<String> budgetFiscalYears) {
        return dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.budgetFiscalYear IN :budgetFiscalYears"
                        + " ORDER BY a.budgetFiscalYear DESC")
                .parameter("budgetFiscalYears", budgetFiscalYears)
                .list();
    }

    public Appropriation getAppropriation(Activity activity) {
        return dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " INNER JOIN fis_Division dv ON dv.appropriation = a"
                        + " INNER JOIN fis_Activity act ON act.division = dv"
                        + " WHERE act=:activity")
                .parameter("activity", activity)
                .one();
    }

    /**
     * determine if appropriation is affected by One Big Beautiful Bill Act
     * enacted by 119th Congress and signed into law on 7/4/2025
     *
     * @param appropriation
     * @return boolean
     */
    public Boolean isOneBigBeautifulBillAct(Appropriation appropriation) {
        List<String> obbbaYears = Arrays.asList("2025", "2026", "2027", "2028");
        return obbbaYears.contains(appropriation.getBudgetFiscalYear());
    }

    public Appropriation getBfyEntryAppropriation(SessionData sessionData) {
        Appropriation bfyEntry = (Appropriation) sessionData.getAttribute("bfyEntry");
        if (bfyEntry == null) {
            throw new RuntimeException("Unable to fetch Appropriation for data entry");
        }
        return bfyEntry;
    }

    public List<Appropriation> getOpenAppropriations() {
        return dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a" +
                        " WHERE a.status = TRUE" +
                        " ORDER BY a.budgetFiscalYear DESC")
                .list();
    }

//    public List<Appropriation> getBfySearchAppropriations(SessionData sessionData) {
//        Appropriation entryBfy = getBfyEntryAppropriation(sessionData);
//        List<Appropriation> searchBfys = (List<Appropriation>) sessionData.getAttribute("bfyPicker");
//        if (searchBfys != null && searchBfys.size() > 0) {
//            // ToDo: code Comparator class for here and in MainScreen.java
//            searchBfys.sort((o1, o2) -> {
//                String year1 = o1.getBudgetFiscalYear();
//                String year2 = o2.getBudgetFiscalYear();
//                if (year1 == null) {
//                    return 1;
//                } else if (year2 == null) {
//                    return -1;
//                }
//                return year2.compareTo(year1);
//            });
//            return searchBfys;
//        } else {
//            List<Appropriation> entryCollection = new ArrayList<>();
//            entryCollection.add(entryBfy);
//            return entryCollection;
//        }
//    }

    public List<Appropriation> getBfyFilterField(SessionData sessionData) {
        Appropriation entryBfy = (Appropriation) sessionData.getAttribute("bfyEntry");
        // ToDo - unchecked cast here; should check type and throw exception (it'll never happen)
        var bfySearchSet = (Set<Appropriation>) sessionData.getAttribute("bfySearch");
        if (bfySearchSet != null && !bfySearchSet.isEmpty()) {
            List<Appropriation> searchBfys = new ArrayList<>(bfySearchSet);
            // ToDo: code Comparator class for here and in MainScreen.java
            searchBfys.sort((o1, o2) -> {
                String year1 = o1.getBudgetFiscalYear();
                String year2 = o2.getBudgetFiscalYear();
                if (year1 == null) {
                    return 1;
                } else if (year2 == null) {
                    return -1;
                }
                return year2.compareTo(year1);
            });
            return searchBfys;
        } else {
            List<Appropriation> entryCollection = new ArrayList<>();
            entryCollection.add(entryBfy);
            return entryCollection;
        }
    }

    public Date lastDayOfFiscalYear() {
        String cfy = getCurrentBfy();
        try {
            return new SimpleDateFormat("MM/dd/yyyy").parse("9/30/".concat(cfy));
        } catch (ParseException ex) {
            throw new RuntimeException("AppropriationService: unable to parse date");
        }
    }

//    public String getCurrentBfy() {
//        SimpleDateFormat mdyFormat = new SimpleDateFormat("MM/dd/yyyy");
//        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
//        try {
//            Date today = mdyFormat.parse(mdyFormat.format(new Date()));
//            String thisYear = yearFormat.format(today);
//            int thisYearInt = Integer.parseInt(thisYear);
//
//            Date firstDayOfCalendarYear = mdyFormat.parse("01/01/" + thisYear);
//            Date lastDayOfFiscalYear = mdyFormat.parse("09/30/" + thisYear);
//            if ((firstDayOfCalendarYear.compareTo(today)) * (today.compareTo(lastDayOfFiscalYear)) >= 0) {
//                return thisYear;
//            } else {
//                return String.valueOf(thisYearInt + 1);
//            }
//        } catch (ParseException ex) {
//            throw new RuntimeException("AppropriationService: unable to parse date");
//        }
//    }

    public String getCurrentBfy() {
        LocalDate today = LocalDate.now(ZoneId.of("America/New_York"));
        int year = today.getYear();
        // FY runs Oct 1 – Sep 30, so Oct–Dec belong to the *next* calendar year's FY
        return String.valueOf(today.getMonthValue() >= 10 ? year + 1 : year);
    }

    public String getLimitBfy() {
        // used to limit the number of fiscal years available to staff
        int year = Year.now().getValue() - 7;
        return Integer.toString(year);
    }

    public Appropriation getCurrentBudgetFiscalYear() {
        return dataManager.load(Appropriation.class)
                .query("select a from fis_Appropriation a" +
                        " where a.budgetFiscalYear = :bFy")
                .parameter("bFy", getCurrentBfy())
                .optional().orElse(null);
    }

    /**
     * used by main view. Returns null if there are no open Appropriations. Don't let this happen!
     *
     * @return Appropriation for the entryBfy selector
     */
    public Appropriation getCurrentOrLatestOpenBudgetFiscalYear() {
        return dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a" +
                        " WHERE a.status=TRUE AND a.budgetFiscalYear = :bFy")
                .parameter("bFy", getCurrentBfy())
                .optional().orElse(dataManager.load(Appropriation.class)
                        .query("SELECT a FROM fis_Appropriation a"
                                + " WHERE a.budgetFiscalYear ="
                                + " (SELECT MAX(e.budgetFiscalYear) FROM fis_Appropriation e WHERE e.status=TRUE)")
                        .optional().orElse(null));
    }

    /**
     * get Appropriation for previous fiscal year
     *
     * @param appropriation starting fiscal year entity
     * @return previous fiscal year Appropriation
     */
    public Appropriation getPreviousFiscalYear(Appropriation appropriation) {
        int priorFiscalYear;
        try {
            priorFiscalYear = Integer.parseInt(appropriation.getBudgetFiscalYear()) - 1;
        } catch (NumberFormatException e) {
            priorFiscalYear = 0;
        }
        return dataManager.load(Appropriation.class)
                .query("select a from fis_Appropriation a" +
                        " where a.budgetFiscalYear = :priorFiscalYear")
                .parameter("priorFiscalYear", Integer.toString(priorFiscalYear))
                .optional()
                .orElse(dataManager.create(Appropriation.class));
    }

    /**
     * getReportFiscalYears provides a list of appropriations that are "open" or
     * are in the list of search years, obtained via session
     *
     * @param sessionData
     * @return List of Appropriations
     */
    public List<Appropriation> fetchReportFiscalYears(SessionData sessionData) {
        // ToDo - unchecked cast here; should check type and throw exception (it'll never happen)
        Set<Appropriation> searchYears = (Set<Appropriation>) sessionData.getAttribute("bfySearch");
        return dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.status = TRUE OR a IN :searchYears"
                        + " ORDER BY a.budgetFiscalYear DESC")
                .parameter("searchYears", searchYears)
                .list();
    }

    /**
     * returns the first day of a given appropriation fiscal year (e.g. 10/1/2022)
     *
     * @param appropriation
     * @return java.util.Date to be used in JPQL
     */
    public Date getFirstDayOfAppropriationBfy(Appropriation appropriation) {
        Date day;
        try {
            int priorYear = Integer.parseInt(appropriation.getBudgetFiscalYear()) - 1;
            day = new SimpleDateFormat("MM/dd/yyyy").parse("10/1/" + priorYear);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("AppropriationService: unable to parse budget fiscal year");
        } catch (ParseException ex) {
            throw new RuntimeException("AppropriationService: unable to parse first day of fiscal year");
        }
        return day;
    }

    /**
     * returns the last day of a given appropriation fiscal year (e.g. 10/1/2022)
     *
     * @param appropriation
     * @return java.util.Date to be used in JPQL
     */
    public Date getLastDayOfAppropriationBfy(Appropriation appropriation) {
        Date day;
        try {
            day = new SimpleDateFormat("MM/dd/yyyy").parse("9/30/" + appropriation.getBudgetFiscalYear());
        } catch (ParseException ex) {
            throw new RuntimeException("AppropriationService: unable to parse current fiscal year");
        }
        return day;
    }

    /**
     * Determine if Appropriation is for fiscal year prior to 2014. Used to determine
     * how allocations should be performed
     *
     * @param appropriation Appropriation to check status of
     * @return Boolean
     */
    public Boolean isAppropriationBefore2014(Appropriation appropriation) {
        var oldAppropriations = dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.budgetFiscalYear < '2014'")
                .list();
        return oldAppropriations.contains(appropriation);
    }

    /**
     * getSpendingAuthority
     *
     * @param appropriation
     * @return
     */
    public KeyValueEntity getSpendingAuthority(Appropriation appropriation) {
        return dataManager.loadValues(
                        "SELECT app.oneYearAmount, app.twoYearAmount,"
                                + " COALESCE(SUM(adj.oneYearAmount),0), COALESCE(SUM(adj.twoYearAmount),0),"
                                + " app.oneYearAmount+COALESCE(SUM(adj.oneYearAmount),0), app.twoYearAmount+COALESCE(SUM(adj.twoYearAmount),0)"
                                + " FROM fis_Appropriation app"
                                + " LEFT JOIN fis_AppropriationAdjustment adj ON adj.appropriation=app"
                                + " WHERE app=:appropriation"
                                + " GROUP BY app.oneYearAmount, app.twoYearAmount")
                .parameter("appropriation", appropriation)
                .properties("one_year_appropriation", "two_year_appropriation", "one_year_adjust", "two_year_adjust", "one_year_total", "two_year_total")
                .optional().orElse(createDefaultSpendingAuthority());
    }

    private KeyValueEntity createDefaultSpendingAuthority() {
        KeyValueEntity defaultResult = dataManager.create(KeyValueEntity.class);
        defaultResult.setValue("one_year_appropriation", BigDecimal.ZERO);
        defaultResult.setValue("two_year_appropriation", BigDecimal.ZERO);
        defaultResult.setValue("one_year_adjust", BigDecimal.ZERO);
        defaultResult.setValue("two_year_adjust", BigDecimal.ZERO);
        defaultResult.setValue("one_year_total", BigDecimal.ZERO);
        defaultResult.setValue("two_year_total", BigDecimal.ZERO);
        return defaultResult;
    }


//    public BigDecimal getSpendingAuthority(Appropriation appropriation, AppropriationType appropriationType) {
//        List<KeyValueEntity> keyValueEntity = dataManager.loadValues(
//                        "SELECT a.oneYearAmount, a.twoYearAmount," +
//                                " COALESCE(SUM(adj.oneYearAmount),0), COALESCE(SUM(adj.twoYearAmount),0)" +
//                                " FROM fis_Appropriation a" +
//                                " LEFT JOIN fis_AppropriationAdjustment adj ON adj.appropriation=a" +
//                                " WHERE a = :appropriation" +
//                                " GROUP BY a.oneYearAmount, a.twoYearAmount")
//                .parameter("appropriation", appropriation)
//                .properties("oneYearAppropriation", "twoYearAppropriation", "oneYearAdjustments", "twoYearAdjustments")
//                .list();
//
//        BigDecimal spendingAuthority = BigDecimal.ZERO;
//
//        if (keyValueEntity.size() == 1) {
//            BigDecimal oneYearAppropriation = keyValueEntity.get(0).getValue("oneYearAppropriation");
//            BigDecimal oneYearAdjustments = keyValueEntity.get(0).getValue("oneYearAdjustments");
//            BigDecimal twoYearAppropriation = keyValueEntity.get(0).getValue("twoYearAppropriation");
//            BigDecimal twoYearAdjustments = keyValueEntity.get(0).getValue("twoYearAdjustments");
//            spendingAuthority = switch (appropriationType) {
//                case ONE_YEAR_FUND -> oneYearAppropriation.add(oneYearAdjustments);
//                case TWO_YEAR_FUND -> twoYearAppropriation.add(twoYearAdjustments);
//                default -> oneYearAppropriation.add(twoYearAppropriation)
//                        .add(oneYearAdjustments).add(twoYearAdjustments);
//            };
//            return spendingAuthority;
//        }
//        return spendingAuthority;
//    }

    //        return dataManager.loadValue(
//                "select sum(o.amount) from sample_Order o where o.date >= :date",
//                BigDecimal.class
//            )
//            .store("main")
//            .parameter("date", toDate)
//            .one();

    public Boolean isAppropriationOpen(ObjectCategory entity) {
        return dataManager.loadValue(
                        "SELECT app.status FROM fis_ObjectCategory e"
                                + " INNER JOIN fis_Appropriation app ON app=e.appropriation"
                                + " WHERE e = :entity", Boolean.class)
                .parameter("entity", entity)
                .one();
    }

    public Boolean isAppropriationOpen(ObjectClass entity) {
        return dataManager.loadValue(
                        "SELECT app.status FROM fis_ObjectClass e"
                                + " INNER JOIN fis_ObjectCategory cat ON cat=e.objectCategory"
                                + " INNER JOIN fis_Appropriation app ON app=cat.appropriation"
                                + " WHERE e = :entity", Boolean.class)
                .parameter("entity", entity)
                .one();
    }

    public Boolean isAppropriationOpen(JitfTransfer entity) {
        return dataManager.loadValue(
                        "SELECT app.status FROM fis_JitfTransfer e"
                                + " INNER JOIN fis_ObjectClass obj ON obj=e.objectClass"
                                + " INNER JOIN fis_ObjectCategory cat ON cat=obj.objectCategory"
                                + " INNER JOIN fis_Appropriation app ON app=cat.appropriation"
                                + " WHERE e = :entity", Boolean.class)
                .parameter("entity", entity)
                .one();
    }

    public Boolean isAppropriationOpen(Division entity) {
        return dataManager.loadValue(
                        "SELECT app.status FROM fis_Division e"
                                + " INNER JOIN fis_Appropriation app ON app=e.appropriation"
                                + " WHERE e = :entity", Boolean.class)
                .parameter("entity", entity)
                .one();
    }

    public Boolean isAppropriationOpen(Branch entity) {
        return dataManager.loadValue(
                        "SELECT app.status FROM fis_Branch e"
                                + " INNER JOIN fis_Division dv ON dv=e.division"
                                + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                + " WHERE e = :entity", Boolean.class)
                .parameter("entity", entity)
                .one();
    }

    public Boolean isAppropriationOpen(Group entity) {
        return dataManager.loadValue(
                        "SELECT app.status FROM fis_Group e"
                                + " INNER JOIN fis_Division dv ON dv=e.division"
                                + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                + " WHERE e = :entity", Boolean.class)
                .parameter("entity", entity)
                .one();
    }

    public Boolean isAppropriationOpen(Activity entity) {
        return dataManager.loadValue(
                        "SELECT app.status FROM fis_Activity e"
                                + " INNER JOIN fis_Division dv ON dv=e.division"
                                + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                + " WHERE e = :entity", Boolean.class)
                .parameter("entity", entity)
                .one();
    }

    public Boolean isAppropriationOpen(Obligation entity) {
        return dataManager.loadValue(
                        "SELECT app.status FROM fis_Obligation e"
                                + " INNER JOIN fis_Activity act ON act=e.activity"
                                + " INNER JOIN fis_Division dv ON dv=act.division"
                                + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                + " WHERE e = :entity", Boolean.class)
                .parameter("entity", entity)
                .one();
    }

    public Boolean isAppropriationOpen(Invoice entity) {
        return dataManager.loadValue(
                        "SELECT app.status FROM fis_Invoice e"
                                + " INNER JOIN fis_Obligation obl ON obl=e.obligation"
                                + " INNER JOIN fis_Activity act ON act=obl.activity"
                                + " INNER JOIN fis_Division dv ON dv=act.division"
                                + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                + " WHERE e = :entity", Boolean.class)
                .parameter("entity", entity)
                .one();
    }

    public Boolean isAppropriationOpen(FundControlNotice entity) {
        return dataManager.loadValue(
                        "SELECT app.status FROM fis_FundControlNotice e"
                                + " INNER JOIN fis_Obligation obl ON obl=e.obligation"
                                + " INNER JOIN fis_Activity act ON act=obl.activity"
                                + " INNER JOIN fis_Division dv ON dv=act.division"
                                + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                + " WHERE e = :entity", Boolean.class)
                .parameter("entity", entity)
                .one();
    }
}