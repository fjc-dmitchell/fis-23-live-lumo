package gov.fjc.fis.service;

import gov.fjc.fis.entity.Activity;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.ObjectCategory;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.ObjectCategoryDto;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("fis_ObjectCategoryService")
public class ObjectCategoryService {

    private final DataManager dataManager;

    final private List<String> compensationAndBenefits = Arrays.asList("11", "12", "13");

    final private String travel = "21";

    public ObjectCategoryService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public String getTravel() {
        return travel;
    }

    public List<String> getCompensationAndBenefits() {
        return compensationAndBenefits;
    }

    public List<ObjectCategory> getCompensationAndBenefits(Appropriation appropriation) {
        return getCompensationAndBenefits(Collections.singletonList(appropriation));
    }

    /**
     * Category Codes which should always appear on reports
     *
     * @return mutable set of strings representing moc codes
     */
    public Set<String> getStandardReportCategoryCodes() {
        return new HashSet<>(Set.of("11", "12", "21", "22", "23", "24", "25", "26", "31"));
    }
//            return Set.of("11", "12", "21", "22", "23", "24", "25", "26", "31");

    // probably don't need this
    public List<ObjectCategory> getStandardReportCategoryEntities(Appropriation appropriation) {
        var categories = getStandardReportCategoryCodes();
        return dataManager.load(ObjectCategory.class)
                .query("SELECT cat FROM fis_ObjectCategory cat"
                        + " WHERE cat.appropriation = :appropriation"
                        + " AND cat.majorObjectClass IN :categoryCodes")
                .parameter("appropriation", appropriation)
                .parameter("categoryCodes", getStandardReportCategoryCodes())
                .list();
    }

    // don't need this - the Dtos will not match anything else!
    public List<ObjectCategoryDto> getStandardReportCategoryDtos(Appropriation appropriation) {
        return getStandardReportCategoryEntities(appropriation).stream().map(cat -> {
            var dto = dataManager.create(ObjectCategoryDto.class);
            dto.configureCategoryDto(cat);
            return dto;
        }).toList();

    }

    public ObjectCategory getCategoryByCode(List<Appropriation> appropriations, String moc) {
        return dataManager.load(ObjectCategory.class)
                .query("SELECT c FROM fis_ObjectCategory c"
                        + " WHERE c.majorObjectClass = :moc"
                        + " AND c.appropriation.budgetFiscalYear = (SELECT MAX(e.budgetFiscalYear)"
                        + " FROM fis_Appropriation e WHERE e IN :appropriations)")
                .parameter("moc", moc)
                .parameter("appropriations", appropriations)
                .optional().orElse(null);
    }

    public List<ObjectCategory> getCompensationAndBenefits(List<Appropriation> appropriations) {
        return dataManager.load(ObjectCategory.class)
                .query("SELECT cat FROM fis_ObjectCategory cat" +
                        " INNER JOIN fis_Appropriation app ON app = cat.appropriation" +
                        " WHERE cat.majorObjectClass in :comp_benefits" +
                        " AND app in :appropriations")
                .parameter("comp_benefits", getCompensationAndBenefits())
                .parameter("appropriations", appropriations)
                .list();
    }

    /**
     * for reconciliation, exclude categories 90 and 91 which FJC has used for fund transfers
     *
     * @param appropriation
     * @return List of Category entities
     */
    public List<ObjectCategory> getFundTransferCategories(Appropriation appropriation) {
        return dataManager.load(ObjectCategory.class)
                .query("SELECT e FROM fis_ObjectCategory e"
                        + " WHERE e.appropriation=:appropriation"
                        + " AND e.majorObjectClass IN ('90','91')")
                .parameter("appropriation", appropriation)
                .list();
    }

    /**
     * Create list of categories from all fiscal years in selection. Category titles
     * are from most recent year containing category
     *
     * @param fiscalYears
     * @return list of categories
     */
    public List<ObjectCategory> fetchCategorySearchList(List<Appropriation> fiscalYears) {
        fiscalYears = fiscalYears.stream().sorted(Comparator.comparing(Appropriation::getBudgetFiscalYear).reversed()).toList();
        List<ObjectCategory> categoryList = new ArrayList<>();
        Set<String> categoryCodes = null;

        for (Appropriation year : fiscalYears) {
            List<ObjectCategory> categoriesInBfyList =
                    dataManager.load(ObjectCategory.class)
                            .query("SELECT c FROM fis_ObjectCategory c"
                                    + " WHERE c.appropriation = :year"
                                    + " AND c.majorObjectClass NOT IN :categoryCodes"
                                    + " ORDER BY c.majorObjectClass")
                            .parameter("year", year)
                            .parameter("categoryCodes", categoryCodes)
                            .list();
            categoryList.addAll(categoriesInBfyList);
            categoryCodes = categoryList.stream().map(ObjectCategory::getMajorObjectClass).collect(Collectors.toSet());
        }

        return categoryList.stream().sorted(Comparator.comparing(ObjectCategory::getMajorObjectClass)).toList();
    }

    /**
     * Create ordered list of Categories for an Appropriation
     *
     * @param appropriation entity
     * @return List of Categories
     */
    public List<ObjectCategory> fetchCategories(Appropriation appropriation) {
        return dataManager.load(ObjectCategory.class)
                .query("SELECT c FROM fis_ObjectCategory c"
                        + " WHERE c.appropriation = :appropriation"
                        + " ORDER BY c.majorObjectClass")
                .parameter("appropriation", appropriation)
                .list();
    }

    public List<ObjectCategoryDto> getCategoryDtos(Appropriation appropriation) {
        return fetchCategories(appropriation).stream().map(cat -> {
            var dto = dataManager.create(ObjectCategoryDto.class);
            dto.configureCategoryDto(cat);
            return dto;
        }).toList();
    }

    /**
     * used by Status of Funds dashboard for "unallocated" or "unspent" row
     * @return new CategoryDto entity
     */
    public ObjectCategoryDto createCategoryDto() {
        return dataManager.create(ObjectCategoryDto.class);
    }

    /**
     * used by obligation lookup screen to find categories for given appropriation year, division, and activity
     *
     * @param appropriation required
     * @param division      null allowed
     * @param activity      null allowed
     * @param foundation    boolean to indicate whether to use FJC Foundation fund
     * @return List of Categories
     */
    public List<ObjectCategory> getObligationCategoriesForDivision(
            Appropriation appropriation, Division division, Activity activity, boolean foundation) {
        return dataManager.load(ObjectCategory.class)
                .query("select distinct cat from fis_ObjectCategory cat" +
                        " inner join fis_Appropriation app on app = cat.appropriation" +
                        " inner join fis_ObjectClass obj on obj.objectCategory = cat" +
                        " inner join fis_Obligation obl on obl.objectClass = obj" +
                        " where obj.objectCategory.appropriation = :appropriation" +
                        " and (:divisionNull = true or obl.activity.division = :division)" +
                        " and (:activityNull = true or obl.activity = :activity)" +
                        " and ((:foundation = true and obl.activity.fund.fundCode = '812300') " +
                        " or (:foundation = false and obl.activity.fund.fundCode <> '812300'))" +
                        " order by cat.majorObjectClass")
                .parameter("appropriation", appropriation)
                .parameter("divisionNull", division == null)
                .parameter("division", division)
                .parameter("activityNull", activity == null)
                .parameter("activity", activity)
                .parameter("foundation", foundation)
                .list();
    }
}