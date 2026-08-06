package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("fis_ObjectClassService")
public class ObjectClassService {

    private final DataManager dataManager;
    private final FundService fundService;

    public ObjectClassService(DataManager dataManager, FundService fundService) {
        this.dataManager = dataManager;
        this.fundService = fundService;
    }

    public List<ObjectClass> fetchProjectionObjectClasses(Activity activity, ObjectCategory category) {
//        Appropriation appropriation = activity == null ? null : activity.getDivision().getAppropriation();
//        List<ObjectClass> exclusions = new ArrayList<>();
//        if (activity != null && activity.getProjections() != null) {
//            exclusions = activity.getProjections().stream().map(ActivityProjection::getObjectClass).toList();
//        }
//        boolean genericProjection = activity != null && activity.getGenericProjection();
        if (activity == null) {
            return List.of();
        }

        Appropriation appropriation = Optional.ofNullable(activity)
                .map(Activity::getDivision)
                .map(Division::getAppropriation)
                .orElse(null);

        List<ObjectClass> exclusions = activity.getProjections() == null
                ? List.of()
                : activity.getProjections().stream()
                .map(ActivityProjection::getObjectClass)
                .toList();

        boolean genericProjection = activity.getGenericProjection();

        return dataManager.load(ObjectClass.class)
                .query("SELECT obj FROM fis_ObjectClass obj"
                        + " INNER JOIN fis_ObjectCategory cat ON cat=obj.objectCategory"
                        + " WHERE cat.appropriation = :appropriation"
                        + " AND (:anyCategory=true OR cat=:category)"
                        + " AND obj NOT IN :exclusions"
                        + " AND ((:genericProjection=true AND obj.budgetObjectClass like '%00')"
                        + " OR (:genericProjection=false AND obj.budgetObjectClass not like '%00'))"
                        + " ORDER BY obj.budgetObjectClass")
                .parameter("appropriation", appropriation)
                .parameter("anyCategory", category == null)
                .parameter("category", category)
                .parameter("exclusions", exclusions)
                .parameter("genericProjection", genericProjection)
                .list();
    }

    public List<ObjectClass> getReimbursementObjectClasses(Activity activity) {
        Appropriation appropriation = null;
        if (activity != null) {
            appropriation = activity.getDivision().getAppropriation();
        }
        return dataManager.load(ObjectClass.class)
                .query("SELECT o FROM fis_ObjectClass o"
                        + " INNER JOIN fis_ObjectCategory cat ON cat=o.objectCategory"
                        + " WHERE cat.appropriation = :appropriation"
                        + " AND o NOT IN (SELECT e.objectClass FROM fis_ActivityReimbursement e WHERE e.activity = :activity)"
                        + " ORDER BY o.budgetObjectClass")
                .parameter("appropriation", appropriation)
                .parameter("activity", activity)
                .list();
    }

    public ObjectClass getObjectClassByCode(List<Appropriation> appropriations, String boc) {
        return dataManager.load(ObjectClass.class)
                .query("SELECT o FROM fis_ObjectClass o"
                        + " WHERE o.budgetObjectClass = :boc"
                        + " AND o.objectCategory.appropriation.budgetFiscalYear = (SELECT MAX(e.budgetFiscalYear)"
                        + " FROM fis_Appropriation e WHERE e IN :appropriations)")
                .parameter("boc", boc)
                .parameter("appropriations", appropriations)
                .optional().orElse(null);
    }

    public List<ObjectClass> fetchObjectClasses(ObjectCategory category, boolean generic) {
        return dataManager.load(ObjectClass.class)
                .query("SELECT o FROM fis_ObjectClass o"
                        + " WHERE o.objectCategory = :category"
                        + " AND ((o.budgetObjectClass NOT LIKE '%00') "
                        + " OR (:generic = true AND o.budgetObjectClass LIKE '%00'))"
                        + " ORDER BY o.budgetObjectClass")
                .parameter("category", category)
                .parameter("generic", generic)
                .list();
    }

    /**
     * used by obligation lookup screen to find categories for given appropriation year,
     * division, activity, and category
     *
     * @param appropriation required
     * @param division      null allowed
     * @param activity      null allowed
     * @param category      null allowed
     * @param foundation    boolean to indicate whether to use FJC Foundation fund
     * @return List of ObjectClasses
     */
    public List<ObjectClass> getObligationObjectClasses(Appropriation appropriation,
                                                        Division division,
                                                        Activity activity,
                                                        ObjectCategory category,
                                                        boolean foundation) {
        Fund foundationFund = fundService.getFoundationFund();
        return dataManager.load(ObjectClass.class)
                .query("SELECT DISTINCT obj FROM fis_ObjectClass obj"
                        + " INNER JOIN fis_Appropriation app ON app = obj.objectCategory.appropriation"
                        + " INNER JOIN fis_Obligation obl ON obl.objectClass = obj"
                        + " WHERE obj.objectCategory.appropriation = :appropriation"
                        + " AND (:divisionNull = true OR obl.activity.division = :division)"
                        + " AND (:activityNull = true OR obl.activity = :activity)"
                        + " AND (:categoryNull = true OR obj.objectCategory = :category)"
                        + " AND ((:foundation = true AND obl.activity.fund = :foundationFund) "
                        + " OR (:foundation = false AND obl.activity.fund <> :foundationFund))"
                        + " ORDER BY obj.budgetObjectClass")
                .parameter("appropriation", appropriation)
                .parameter("division", division)
                .parameter("divisionNull", division == null)
                .parameter("category", category)
                .parameter("categoryNull", category == null)
                .parameter("activityNull", activity == null)
                .parameter("activity", activity)
                .parameter("foundation", foundation)
                .parameter("foundationFund", foundationFund)
                .list();
    }

    // rewrite with JOIN or fetch plan
    public List<ObjectClass> fetchObjectClassSearchList(List<Appropriation> fiscalYears, String moc, boolean includeGenerics) {
        fiscalYears = fiscalYears.stream().sorted(Comparator.comparing(Appropriation::getBudgetFiscalYear).reversed()).toList();
        List<ObjectClass> bocList = new ArrayList<>();
        Set<String> bocCodes = null;

        for (Appropriation year : fiscalYears) {
            List<ObjectClass> objectClassesInBfyList =
                    dataManager.load(ObjectClass.class)
                            .query("SELECT c FROM fis_ObjectClass c"
                                    + " WHERE c.objectCategory.appropriation = :year"
                                    + " AND c.budgetObjectClass NOT IN :bocCodes"
                                    + " AND (:generic = true OR c.budgetObjectClass NOT LIKE '%00')"
                                    + " AND (:moc IS NULL OR c.objectCategory.majorObjectClass = :moc)")
                            .parameter("year", year)
                            .parameter("bocCodes", bocCodes)
                            .parameter("moc", moc)
                            .parameter("generic", includeGenerics)
                            .list();
            bocList.addAll(objectClassesInBfyList);
            bocCodes = bocList.stream().map(ObjectClass::getBudgetObjectClass).collect(Collectors.toSet());
        }

        return bocList.stream().sorted(Comparator.comparing(ObjectClass::getBudgetObjectClass)).toList();
    }

    public List<ObjectClass> fetchObjectClassSearchList(Appropriation appropriation, String moc, boolean includeGenerics) {
       List<Appropriation> appropriationList = new ArrayList<>();
       appropriationList.add(appropriation);
       return fetchObjectClassSearchList(appropriationList, moc, includeGenerics);
    }
}