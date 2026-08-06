package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import gov.fjc.fis.entity.dto.ActivityDto;
import gov.fjc.fis.entity.dto.ActivityProjectionDto;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component("fis_ActivityProjectionService")
public class ActivityProjectionService {

    private final DataManager dataManager;
    private final FundService fundService;
    private final AppropriationService appropriationService;

    public ActivityProjectionService(DataManager dataManager, FundService fundService,
                                     AppropriationService appropriationService) {
        this.dataManager = dataManager;
        this.fundService = fundService;
        this.appropriationService = appropriationService;
    }

    public ActivityProjection getActivityProjection(Activity activity, ObjectClass objectClass) {
        // try specific projection
        var boc = objectClass.getBudgetObjectClass();
        var projectionOptional = getActivityProjection(activity, boc);
        if (projectionOptional.isPresent()) {
            return projectionOptional.get();
        }

        // try generic projection
        boc = objectClass.getObjectCategory().getMajorObjectClass().concat("00");
        projectionOptional = getActivityProjection(activity, boc);
        if (projectionOptional.isPresent()) {
            return projectionOptional.get();
        }

        // create a zero projection
        ActivityProjection projection = dataManager.create(ActivityProjection.class);
        projection.setActivity(activity);
        projection.setObjectClass(objectClass);
        projection.setAmount(BigDecimal.ZERO);
        return projection;
    }

    private Optional<ActivityProjection> getActivityProjection(Activity activity, String boc) {
        return dataManager.load(ActivityProjection.class)
                .query("SELECT p FROM fis_ActivityProjection p"
                        + " INNER JOIN p.objectClass o"
                        + " WHERE p.activity = :activity AND o.budgetObjectClass = :boc")
                .parameter("activity", activity)
                .parameter("boc", boc)
                .optional();
    }

    public List<ActivityProjection> getActivitiesByProjectionMoc(ObjectCategory category) {
        List<Fund> funds = new ArrayList<>();
        funds.add(fundService.getAppropriationOneYearFund());
        funds.add(fundService.getAppropriationTwoYearFund());
        return getActivitiesByProjectionMoc(category, funds);
    }

    public List<ActivityProjection> getOneYearActivitiesByProjectionMoc(ObjectCategory category) {
        List<Fund> funds = new ArrayList<>();
        funds.add(fundService.getAppropriationOneYearFund());
        return getActivitiesByProjectionMoc(category, funds);
    }

    public List<ActivityProjection> getTwoYearActivitiesByProjectionMoc(ObjectCategory category) {
        List<Fund> funds = new ArrayList<>();
        funds.add(fundService.getAppropriationTwoYearFund());
        return getActivitiesByProjectionMoc(category, funds);
    }

    public List<ActivityProjection> getActivitiesByProjectionMoc(ObjectCategory category, List<Fund> funds) {
        return dataManager.load(ActivityProjection.class)
                .query("SELECT p FROM fis_ActivityProjection p"
                        + " INNER JOIN fis_Activity act ON p.activity=act"
                        + " INNER JOIN fis_Fund fund ON act.fund=fund"
                        + " INNER JOIN fis_Division dv ON act.division=dv"
                        + " INNER JOIN fis_ObjectClass obj ON p.objectClass = obj"
                        + " INNER JOIN fis_ObjectCategory cat ON obj.objectCategory = cat"
                        + " WHERE cat = :category AND p.amount <> 0 AND act.fund in :funds"
                        + " ORDER BY dv.divisionCode, fund.fundCode, act.activityNumber, cat.majorObjectClass, obj.budgetObjectClass")
                .parameter("category", category)
                .parameter("funds", funds)
                .fetchPlan("activityProjection-fetch-plan")
                .list();
    }

    public List<ActivityProjection> getTwoYearActivitiesByProjectionMoc(ObjectCategory category, boolean priorYear) {
        Fund twoYearFund = fundService.getAppropriationTwoYearFund();
        Appropriation appropriation = category.getAppropriation();
        Date bfyStartDate = appropriationService.getFirstDayOfAppropriationBfy(appropriation);
        Date bfyEndDate = appropriationService.getLastDayOfAppropriationBfy(appropriation);

        if (priorYear) {
            appropriation = appropriationService.getPreviousFiscalYear(category.getAppropriation());
        }

        return dataManager.load(ActivityProjection.class)
                .query("SELECT e FROM fis_ActivityProjection e" +
                        " INNER JOIN fis_Activity a ON a = e.activity" +
                        " INNER JOIN fis_ObjectClass o ON o = e.objectClass" +
                        " INNER JOIN fis_ObjectCategory c ON c = o.objectCategory" +
                        " INNER JOIN fis_Appropriation app ON app = c.appropriation" +
                        " WHERE c.majorObjectClass = :moc " +
                        " AND c.appropriation = :appropriation" +
                        " AND a.fund = :twoYearFund" +
                        " AND ((:priorYear=true AND a.endDate >= :bfyStartDate)" +
                        " OR (:priorYear=false AND (a.endDate IS NULL OR a.endDate <= :bfyEndDate)))")
                .parameter("moc", category.getMajorObjectClass())
                .parameter("appropriation", appropriation)
                .parameter("twoYearFund", twoYearFund)
                .parameter("priorYear", priorYear)
                .parameter("bfyStartDate", bfyStartDate)
                .parameter("bfyEndDate", bfyEndDate)

                .list();
    }

    public List<ActivityProjection> getActivitiesByProjectionBoc(ObjectClass objectClass) {
        return dataManager.load(ActivityProjection.class)
                .query("SELECT p FROM fis_ActivityProjection p"
                        + " INNER JOIN fis_Activity act ON p.activity=act"
                        + " INNER JOIN fis_Fund fund ON act.fund=fund"
                        + " INNER JOIN fis_Division dv ON act.division=dv"
                        + " INNER JOIN fis_ObjectClass obj ON p.objectClass = obj"
                        + " INNER JOIN fis_ObjectCategory cat ON obj.objectCategory = cat"
                        + " WHERE obj = :objectClass AND p.amount <> 0"
                        + " ORDER BY dv.divisionCode, fund.fundCode, act.activityNumber, cat.majorObjectClass, obj.budgetObjectClass")
                .parameter("objectClass", objectClass)
                .fetchPlan("activityProjection-fetch-plan")
                .list();
//        return dataManager.load(ActivityProjection.class)
//                .query("select e from fis_ActivityProjection e" +
//                        " where e.objectClass = :objc" +
//                        " order by e.activity.division.divisionCode, e.activity.activityNumber")
//                .parameter("objc", objectClass)
//                .list();
    }

//    public List<ActivityProjection> getActivityProjections(Appropriation currentBfy, ActivityFundingType activityFundingType) {
//        if (!(activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_1YR_FUND)
//                || activityFundingType.equals(ActivityFundingType.PRIOR_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY)
//                || activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY))) {
//            throw new RuntimeException("FIS coding error: ActivityFundingType not allowed");
//        }
//        Fund oneYearFund = fundService.getAppropriationOneYearFund();
//        Fund twoYearFund = fundService.getAppropriationTwoYearFund();
//        Appropriation priorBfy = appropriationService.getPreviousFiscalYear(currentBfy);
//        Date bfyStartDate = appropriationService.getFirstDayOfAppropriationBfy(currentBfy);
//        Date bfyEndDate = appropriationService.getLastDayOfAppropriationBfy(currentBfy);
//        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(ActivityProjection.class, "full");
//
//        return dataManager.load(ActivityProjection.class)
//                .query("SELECT p FROM fis_ActivityProjection p" +
//                        " INNER JOIN fis_Activity act ON act = p.activity" +
//                        " INNER JOIN fis_Fund fund ON fund = act.fund" +
//                        " INNER JOIN fis_Division dv ON dv = act.division" +
//                        " INNER JOIN fis_Appropriation app ON app = dv.appropriation" +
//                        " WHERE (:currentOneYear = true AND app = :currentBfy AND fund = :oneYearFund)" +
//                        " OR (:priorTwoYear = true AND app = :priorBfy AND fund = :twoYearFund AND act.endDate >= :bfyStartDate)" +
//                        " OR (:currentTwoYear = true AND app = :currentBfy AND fund = :twoYearFund AND (act.endDate IS NULL OR act.endDate <= :bfyEndDate))")
//                .parameter("currentBfy", currentBfy)
//                .parameter("priorBfy", priorBfy)
//                .parameter("oneYearFund", oneYearFund)
//                .parameter("twoYearFund", twoYearFund)
//                .parameter("currentOneYear", activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_1YR_FUND))
//                .parameter("priorTwoYear", activityFundingType.equals(ActivityFundingType.PRIOR_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY))
//                .parameter("currentTwoYear", activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY))
//                .parameter("bfyStartDate", bfyStartDate)
//                .parameter("bfyEndDate", bfyEndDate)
//                .fetchPlan(fetchPlan)
//                .list();
//    }

//    public List<KeyValueEntity> aggregateProjectionsByDivisionCode(Appropriation appropriation,
//                                                                   Fund fund,
//                                                                   ActivityFundingType activityFundingType) {
//        if (!(activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_1YR_FUND)
//                || activityFundingType.equals(ActivityFundingType.PRIOR_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY)
//                || activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY))) {
//            throw new RuntimeException("FIS coding error: ActivityFundingType not allowed");
//        }
//
//        Date bfyStartDate = appropriationService.getFirstDayOfAppropriationBfy(appropriation);
//        Date bfyEndDate = appropriationService.getLastDayOfAppropriationBfy(appropriation);
//
//        return dataManager.loadValues(
//                        "SELECT dv.divisionCode, sum(p.amount)" +
//                                " FROM fis_ActivityProjection p" +
//                                " INNER JOIN fis_Activity act ON act = p.activity" +
//                                " INNER JOIN fis_Division dv ON dv = act.division" +
//                                " WHERE dv.appropriation = :appropriation" +
//                                " AND act.fund = :fund" +
//                                " AND (:currentOneYearFund = true" +
//                                " OR (:priorTwoYearFund = true AND act.endDate >= :bfyStartDate)" +
//                                " OR (:currentTwoYearFund = true AND (act.endDate IS NULL OR act.endDate <= :bfyEndDate)))" +
//                                " GROUP BY dv.divisionCode" +
//                                " ORDER BY dv.divisionCode")
//                .parameter("appropriation", appropriation)
//                .parameter("fund", fund)
//                .parameter("currentOneYearFund", activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_1YR_FUND))
//                .parameter("priorTwoYearFund", activityFundingType.equals(ActivityFundingType.PRIOR_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY))
//                .parameter("currentTwoYearFund", activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY))
//                .parameter("bfyStartDate", bfyStartDate)
//                .parameter("bfyEndDate", bfyEndDate)
//                .properties("divcode", "projected")
//                .list();
//    }

//    public List<KeyValueEntity> aggregateProjectionsByDivisionCode(Appropriation currentBfy,
//                                                                   ActivityFundingType activityFundingType) {
//        if (!(activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_1YR_FUND)
//                || activityFundingType.equals(ActivityFundingType.PRIOR_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY)
//                || activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY))) {
//            throw new RuntimeException("FIS coding error: ActivityFundingType not allowed");
//        }
//        Fund oneYearFund = fundService.getAppropriationOneYearFund();
//        Fund twoYearFund = fundService.getAppropriationTwoYearFund();
//        Appropriation priorBfy = appropriationService.getPreviousFiscalYear(currentBfy);
//        Date bfyStartDate = appropriationService.getFirstDayOfAppropriationBfy(currentBfy);
//        Date bfyEndDate = appropriationService.getLastDayOfAppropriationBfy(currentBfy);
//
//        return dataManager.loadValues(
//                        "SELECT dv.divisionCode, coalesce(sum(p.amount),0)" +
//                                " FROM fis_ActivityProjection p" +
//                                " INNER JOIN fis_Activity act ON act = p.activity" +
//                                " INNER JOIN fis_Fund fund ON fund = act.fund" +
//                                " INNER JOIN fis_Division dv ON dv = act.division" +
//                                " INNER JOIN fis_Appropriation app ON app = dv.appropriation" +
//                                " WHERE ((:currentOneYearFund = true AND app = :currentBfy AND fund = :oneYearFund)" +
//                                " OR (:priorTwoYearFund = true AND app = :priorBfy AND fund = :twoYearFund AND act.endDate >= :bfyStartDate)" +
//                                " OR (:currentTwoYearFund = true AND app = :currentBfy AND fund = :twoYearFund AND (act.endDate IS NULL OR act.endDate <= :bfyEndDate)))" +
//                                " GROUP BY dv.divisionCode" +
//                                " ORDER BY dv.divisionCode")
//                .parameter("currentBfy", currentBfy)
//                .parameter("priorBfy", priorBfy)
//                .parameter("oneYearFund", oneYearFund)
//                .parameter("twoYearFund", twoYearFund)
//                .parameter("currentOneYearFund", activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_1YR_FUND))
//                .parameter("priorTwoYearFund", activityFundingType.equals(ActivityFundingType.PRIOR_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY))
//                .parameter("currentTwoYearFund", activityFundingType.equals(ActivityFundingType.CURRENT_YEAR_2YR_FUND_ACTIVITIES_CURRENT_FY))
//                .parameter("bfyStartDate", bfyStartDate)
//                .parameter("bfyEndDate", bfyEndDate)
//                .properties("divcode", "projected")
//                .list();
//    }

    public List<KeyValueEntity> aggregateProjectionsByActivity(Division division, Branch branch) {
        if (division == null && branch == null) {
            throw new RuntimeException("FIS coding error: division and branch cannot both be null");
        }
        return dataManager.loadValues(
                        "SELECT p.activity, COALESCE(SUM(p.amount),0) from fis_ActivityProjection p"
                                + " INNER JOIN fis_Activity a ON a=p.activity"
                                + " WHERE (:anyDivision = true OR a.division = :division)"
                                + " AND (:anyBranch = true OR a.branch = :branch)"
                                + " GROUP BY p.activity")
                .parameter("anyDivision", division == null)
                .parameter("division", division)
                .parameter("anyBranch", branch == null)
                .parameter("branch", branch)
                .properties("activity", "projections")
                .list();
    }

    public List<KeyValueEntity> aggregateProjectionsByActivity(List<Activity> activities) {
        return dataManager.loadValues(
                        "SELECT p.activity, COALESCE(SUM(p.amount),0)"
                                + " FROM fis_ActivityProjection p"
                                + " WHERE p.activity IN :activities"
                                + " GROUP BY p.activity")
                .parameter("activities", activities)
                .properties("activity", "amount")
                .list();
    }

    /**
     * get projections
     *
     * @param appropriation Appropriation, used only to place amount in proper property
     * @param activities    List of Activity entities
     * @return List of KeyValueEntity
     */
    public List<KeyValueEntity> getProjections(Appropriation appropriation, List<Activity> activities, boolean allowZeros) {
        Appropriation priorYear = appropriationService.getPreviousFiscalYear(appropriation);
        Fund oneYearFund = fundService.getAppropriationOneYearFund();
        Fund twoYearFund = fundService.getAppropriationTwoYearFund();
        return dataManager.loadValues(
                        "SELECT fund.fundCode, app.budgetFiscalYear, dv.divisionCode, act.activityNumber,"
                                + " act.title, cat.majorObjectClass, obj.budgetObjectClass, p.amount,"
                                + " CASE WHEN dv.appropriation = :priorYear AND act.fund = :twoYearFund THEN p.amount ELSE 0 END,"
                                + " CASE WHEN dv.appropriation = :appropriation AND act.fund = :oneYearFund THEN p.amount ELSE 0 END,"
                                + " CASE WHEN dv.appropriation = :appropriation AND act.fund = :twoYearFund THEN p.amount ELSE 0 END"
                                + " FROM fis_ActivityProjection p"
                                + " INNER JOIN fis_Activity act ON act=p.activity"
                                + " INNER JOIN fis_Division dv ON dv=act.division"
                                + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                + " INNER JOIN fis_Fund fund ON fund=act.fund"
                                + " INNER JOIN fis_ObjectClass obj ON obj=p.objectClass"
                                + " INNER JOIN fis_ObjectCategory cat ON cat=obj.objectCategory"
                                + " WHERE p.activity IN :activities"
                                + " AND (:allowZeros = true OR p.amount <> 0)"
                                + " ORDER BY app.budgetFiscalYear, fund.fundCode, act.activityNumber, obj.budgetObjectClass")
                .parameter("priorYear", priorYear)
                .parameter("appropriation", appropriation)
                .parameter("oneYearFund", oneYearFund)
                .parameter("twoYearFund", twoYearFund)
                .parameter("activities", activities)
                .parameter("allowZeros", allowZeros)
                .properties("fund", "bfy", "division", "actnum", "title", "moc", "boc", "amount", "prioryear", "oneyear", "twoyear")
                .list();
    }

    /**
     * get projections
     *
     * @param appropriation Appropriation, used only to place amount in proper property
     * @param activities    List of Activity entities
     * @return List of KeyValueEntity
     */
    public List<KeyValueEntity> getProjectionsNew(Appropriation appropriation, List<ActivityDto> activities, boolean allowZeros) {
        Appropriation priorYear = appropriationService.getPreviousFiscalYear(appropriation);
        Fund oneYearFund = fundService.getAppropriationOneYearFund();
        Fund twoYearFund = fundService.getAppropriationTwoYearFund();
        var activityIds = activities.stream().map(ActivityDto::getId).toList();
        return dataManager.loadValues(
                        "SELECT p.id, fund.id, fund.fundCode, app.id, app.budgetFiscalYear, dv.id, dv.divisionCode, act.id, act.activityNumber,"
                                + " act.title, cat.id, cat.majorObjectClass, obj.id, obj.budgetObjectClass, p.amount,"
                                + " CASE WHEN dv.appropriation = :priorYear AND act.fund = :twoYearFund THEN p.amount ELSE 0 END,"
                                + " CASE WHEN dv.appropriation = :appropriation AND act.fund = :oneYearFund THEN p.amount ELSE 0 END,"
                                + " CASE WHEN dv.appropriation = :appropriation AND act.fund = :twoYearFund THEN p.amount ELSE 0 END"
                                + " FROM fis_ActivityProjection p"
                                + " INNER JOIN fis_Activity act ON act=p.activity"
                                + " INNER JOIN fis_Division dv ON dv=act.division"
                                + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                + " INNER JOIN fis_Fund fund ON fund=act.fund"
                                + " INNER JOIN fis_ObjectClass obj ON obj=p.objectClass"
                                + " INNER JOIN fis_ObjectCategory cat ON cat=obj.objectCategory"
                                + " WHERE p.activity.id IN :activities"
                                + " AND (:allowZeros = true OR p.amount <> 0)"
                                + " ORDER BY app.budgetFiscalYear, fund.fundCode, dv.divisionCode, act.activityNumber, obj.budgetObjectClass")
                .parameter("priorYear", priorYear)
                .parameter("appropriation", appropriation)
                .parameter("oneYearFund", oneYearFund)
                .parameter("twoYearFund", twoYearFund)
                .parameter("activities", activityIds)
                .parameter("allowZeros", allowZeros)
                .properties("id", "fundId", "fundCode", "appropriationId", "budgetFiscalYear", "divisionId", "divisionCode", "activityId", "activityNumber", "activityTitle", "categoryId", "majorObjectClass", "objectClassId", "budgetObjectClass", "amount", "prioryear", "oneyear", "twoyear")
                .list();
    }

    public List<KeyValueEntity> fetchProjectionSums(Appropriation appropriation, List<Fund> funds) {
        return dataManager.loadValues(
                        "SELECT cat.majorObjectClass, div.divisionCode, act.fund, sum(proj.amount)"
                                + " FROM fis_ActivityProjection proj"
                                + " INNER JOIN fis_ObjectClass obj ON obj = proj.objectClass"
                                + " INNER JOIN fis_ObjectCategory cat ON cat = obj.objectCategory"
                                + " INNER JOIN fis_Activity act ON act = proj.activity"
                                + " INNER JOIN fis_Division div ON div = act.division"
                                + " WHERE div.appropriation = :appropriation"
                                + " AND act.fund in :funds"
                                + " GROUP BY cat.majorObjectClass, div.divisionCode, act.fund"
                                + " ORDER BY cat.majorObjectClass, div.divisionCode, act.fund.fundCode")
                .properties("moc", "divcode", "fund", "amount")
                .parameter("appropriation", appropriation)
                .parameter("funds", funds)
                .list();
    }

    public BigDecimal sumProjections(Activity activity) {
        return dataManager.loadValue("SELECT coalesce(sum(p.amount),0)"
                        + " FROM fis_ActivityProjection p"
                        + " WHERE p.activity=:activity", BigDecimal.class)
                .parameter("activity", activity)
                .one();
    }

    public BigDecimal sumProjections(List<Activity> activities) {
        return dataManager.loadValue("SELECT coalesce(sum(p.amount),0)"
                        + " FROM fis_ActivityProjection p"
                        + " WHERE p.activity IN :activities", BigDecimal.class)
                .parameter("activities", activities)
                .one();
    }

    public List<KeyValueEntity> sumActivityProjections(List<Division> divisions, Fund fund) {
        return dataManager.loadValues(
                        "SELECT act.fund, act.division, COALESCE(SUM(proj.amount),0)"
                                + " FROM fis_Activity act"
                                + " INNER JOIN fis_ActivityProjection proj ON act=proj.activity"
                                + " WHERE act.division IN :divisions AND act.fund=:fund"
                                + " GROUP BY act.fund, act.division")
                .parameter("divisions", divisions)
                .parameter("fund", fund)
                .properties("fund", "division", "amount")
                .list();
    }

    public List<ActivityProjectionDto> getProjectionDtosForMajorObjectClass(List<ActivityProjectionDto> projectionDtos, String majorObjectClass) {
        return projectionDtos.stream().filter(projectionDto -> projectionDto.getMajorObjectClass().equals(majorObjectClass)).toList();
    }

    public List<ActivityProjectionDto> getProjectionDtosForBudgetObjectClass(List<ActivityProjectionDto> projectionDtos, String budgetObjectClass) {
        return projectionDtos.stream().filter(projectionDto -> projectionDto.getBudgetObjectClass().equals(budgetObjectClass)).toList();
    }

    public List<ActivityProjectionDto> getProjectionDtosForDivisionCode(List<ActivityProjectionDto> projectionDtos, String divisionCode) {
        return projectionDtos.stream().filter(projectionDto -> projectionDto.getDivisionCode().equals(divisionCode)).toList();
    }

    public List<ActivityProjection> fetchBiFiscalActivityProjections(Appropriation currentYearAppropriation) {
        Appropriation priorYearAppropriation = appropriationService.getPreviousFiscalYear(currentYearAppropriation);
        Fund oneYearFund = fundService.getAppropriationOneYearFund();
        Fund twoYearFund = fundService.getAppropriationTwoYearFund();
        Date bfyStartDate = appropriationService.getFirstDayOfAppropriationBfy(currentYearAppropriation);
        Date bfyEndDate = appropriationService.getLastDayOfAppropriationBfy(currentYearAppropriation);

        return dataManager.load(ActivityProjection.class)
                .query("SELECT p FROM fis_ActivityProjection p"
                        + " INNER JOIN fis_Activity act ON act = p.activity"
                        + " INNER JOIN fis_Division dv ON dv = act.division"
                        + " INNER JOIN fis_Appropriation app ON app = dv.appropriation"
                        + " INNER JOIN fis_Fund fund ON fund = act.fund"
                        + " WHERE p.amount <> 0 AND p.activity IN "
                        + " (SELECT a FROM fis_Activity a"
                        + " INNER JOIN fis_Division dv ON dv = a.division"
                        + " INNER JOIN fis_Appropriation app ON app = dv.appropriation"
                        + " INNER JOIN fis_Fund fund ON fund = a.fund"
                        + " WHERE (app = :currentYear AND fund = :oneYearFund)"
                        + " OR (app = :priorYear AND fund = :twoYearFund AND a.endDate >= :bfyStartDate)"
                        + " OR (app = :currentYear AND fund = :twoYearFund AND (a.endDate IS NULL OR a.endDate <= :bfyEndDate)))"
                        + " ORDER BY app.budgetFiscalYear, fund.fundCode, dv.divisionCode, act.activityNumber")
                .parameter("currentYear", currentYearAppropriation)
                .parameter("priorYear", priorYearAppropriation)
                .parameter("oneYearFund", oneYearFund)
                .parameter("twoYearFund", twoYearFund)
                .parameter("bfyStartDate", bfyStartDate)
                .parameter("bfyEndDate", bfyEndDate)
                .fetchPlan("extended")
                .list();
    }

    public List<ActivityProjectionDto> getProjectionDtos(Appropriation appropriation, List<ActivityDto> activityDtos, boolean allowZeros) {
        var projections = getProjectionsNew(appropriation, activityDtos, allowZeros);
        List<ActivityProjectionDto> projectionDtos = new ArrayList<>();
        ActivityProjectionDto dto;

        for (var kvEntity : projections) {
            dto = dataManager.create(ActivityProjectionDto.class);
            dto.setId(kvEntity.getValue("id"));
            dto.setFundId(kvEntity.getValue("fundId"));
            dto.setFundCode(kvEntity.getValue("fundCode"));
            dto.setAppropriationId(kvEntity.getValue("appropriationId"));
            dto.setBudgetFiscalYear(kvEntity.getValue("budgetFiscalYear"));
            dto.setDivisionId(kvEntity.getValue("divisionId"));
            dto.setDivisionCode(kvEntity.getValue("divisionCode"));
            dto.setActivityId(kvEntity.getValue("activityId"));
            dto.setActivityNumber(kvEntity.getValue("activityNumber"));
            dto.setActivityTitle(kvEntity.getValue("activityTitle"));
            dto.setCatagoryId(kvEntity.getValue("categoryId"));
            dto.setMajorObjectClass(kvEntity.getValue("majorObjectClass"));
            dto.setObjectClassId(kvEntity.getValue("objectClassId"));
            dto.setBudgetObjectClass(kvEntity.getValue("budgetObjectClass"));
            dto.setAmount(kvEntity.getValue("amount"));
            dto.setPriorTwoYearAmount(kvEntity.getValue("prioryear"));
            dto.setCurrentOneYearAmount(kvEntity.getValue("oneyear"));
            dto.setCurrentTwoYearAmount(kvEntity.getValue("twoyear"));
            projectionDtos.add(dto);
        }
        return projectionDtos;
    }

    /**
     * sum projections for activity, either travel or non-travel
     * quick and dirty for Nancy (Mike) 6/25/2025
     *
     * @param activityDto
     * @param travel
     * @return
     */
    public BigDecimal sumProjections(ActivityDto activityDto, boolean travel) {
        return dataManager.loadValue("SELECT coalesce(sum(proj.amount),0)"
                        + " FROM fis_ActivityProjection proj"
                        + " INNER JOIN fis_Activity act ON act=proj.activity"
                        + " INNER JOIN fis_ObjectClass obj ON obj=proj.objectClass"
                        + " INNER JOIN fis_ObjectCategory cat ON cat=obj.objectCategory"
                        + " WHERE act.id = :activity"
                        + " AND ((:travel=TRUE AND cat.majorObjectClass='21')"
                        + " OR (:travel=FALSE AND cat.majorObjectClass <> '21'))", BigDecimal.class)
                .parameter("activity", activityDto.getId())
                .parameter("travel", travel)
                .one();
    }

    public ActivityProjection findOrCreateActivityProjection(Activity activity, ObjectClass objectClass) {
        return dataManager.load(ActivityProjection.class)
                .query("SELECT p FROM fis_ActivityProjection p"
                        + " WHERE p.activity = :activity AND p.objectClass = :objectClass")
                .parameter("activity", activity)
                .parameter("objectClass", objectClass)
                .optional()
                .orElseGet(() -> {
                    ActivityProjection p = dataManager.create(ActivityProjection.class);
                    p.setActivity(activity);
                    p.setObjectClass(objectClass);
                    p.setAmount(BigDecimal.ZERO);
                    return p;
                });
    }
}