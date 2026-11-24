package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.Fund;
import gov.fjc.fis.entity.dto.DivisionDto;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Sort;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides operations for retrieving, filtering, and converting FIS divisions.
 *
 * <p>This service loads divisions for appropriations and funds, identifies
 * divisions with designated division or budget organization codes, and creates
 * division selections for reporting and search operations. It also provides
 * division-specific fund selections and converts division entities to
 * {@link DivisionDto} instances.</p>
 *
 * <p>Unless otherwise specified, methods returning multiple divisions order
 * their results by division code.</p>
 */
@Component("fis_DivisionService")
public class DivisionService {

    // todo: consider rewriting these all as budgetOrgs
    private static final String ADMINISTRATION_DIVISION_CODE = "1";
    private static final String EDUCATION_DIVISION_CODE = "2";
    private static final String RESEARCH_DIVISION_CODE = "4";
    private static final String OBBBA_BUDGET_ORG = "JXXMAPP";

    private final DataManager dataManager;
    private final AppropriationService appropriationService;
    private final FundService fundService;

    /**
     * Creates a division service.
     *
     * @param dataManager the data manager used to load and create entities
     * @param fundService the service providing designated FIS funds
     * @param appropriationService the service providing appropriation information
     */
    public DivisionService(DataManager dataManager, FundService fundService, AppropriationService appropriationService) {
        this.dataManager = dataManager;
        this.fundService = fundService;
        this.appropriationService = appropriationService;
    }

    /**
     * Finds the Education division for the specified appropriation.
     *
     * @param appropriation the appropriation containing the division
     * @return the Education division, or {@code null} if it is not found
     */
    public Division findEducationDivision(Appropriation appropriation) {
        return findDivisionByCode(appropriation, EDUCATION_DIVISION_CODE);
    }

    /**
     * Finds the Research division for the specified appropriation.
     *
     * @param appropriation the appropriation containing the division
     * @return the Research division, or {@code null} if it is not found
     */
    public Division findResearchDivision(Appropriation appropriation) {
        return findDivisionByCode(appropriation, RESEARCH_DIVISION_CODE);
    }

    /**
     * Finds the Administration division for the specified appropriation.
     *
     * @param appropriation the appropriation containing the division
     * @return the Administration division, or {@code null} if it is not found
     */
    public Division findAdministrationDivision(Appropriation appropriation) {
        return findDivisionByCode(appropriation, ADMINISTRATION_DIVISION_CODE);
    }

    /**
     * Finds the OBBBA division for the specified appropriation.
     *
     * @param appropriation the appropriation containing the division
     * @return the OBBBA division, or {@code null} if it is not found
     */
    public Division findObbbaDivision(Appropriation appropriation) {
        return findDivisionByBudgetOrg(appropriation, OBBBA_BUDGET_ORG);
    }

    /**
     * Returns the Education and OBBBA divisions for the specified appropriation.
     *
     * @param appropriation the appropriation for which divisions are requested
     * @return the Education and OBBBA divisions, ordered by division code
     */
    public List<Division> fetchEducationAndObbbaDivisions(Appropriation appropriation) {
        return dataManager.load(Division.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.equal("appropriation", appropriation),
                        LogicalCondition.or(
                                PropertyCondition.equal("divisionCode", EDUCATION_DIVISION_CODE),
                                PropertyCondition.equal("budgetOrg", OBBBA_BUDGET_ORG))))
                .sort(Sort.by("divisionCode"))
                .list();
    }

    /**
     * Returns the division code assigned to the Education division.
     *
     * @return the Education division code
     * @deprecated Avoid relying on the designated division code directly.
     * ED program analysis reports will be rewritten to remove this dependency.
     */
    @Deprecated(since = "2.3", forRemoval = true)
    public String getEducationDivisionCode() {
        return EDUCATION_DIVISION_CODE;
    }

    /**
     * Returns the divisions associated with positions in the current budget
     * fiscal year.
     *
     * <p>The returned map contains divisions funded by an appropriated fund
     * whose budget organization code is referenced by at least one position.
     * Each entry maps the division's budget organization code to its combined
     * title and budget organization description. The entries retain division-code
     * order.</p>
     *
     * @return a map of budget organization codes to division titles and budget
     * organization descriptions, or an empty map if there are no appropriated
     * funds
     */
    public Map<String, String> fetchPositionDivisions() {
        var funds = fundService.getAppropriatedFundsList();
        if (funds.isEmpty()) {
            return Map.of();
        }
        var appropriation = appropriationService.getCurrentBudgetFiscalYear();

        return dataManager.load(Division.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.equal("appropriation", appropriation),
                        PropertyCondition.inList("fund", funds),
                        JpqlCondition.create(
                                "e.budgetOrg IN (SELECT p.jlCostOrgCd FROM fis_Position p)",
                                null)))
                .sort(Sort.by("divisionCode"))
                .list()
                .stream()
                .collect(Collectors.toMap(
                        Division::getBudgetOrg,
                        Division::getTitleAndBudgetOrg,
                        (first, _) -> first,
                        LinkedHashMap::new));
    }

    /**
     * Converts the specified division to a data transfer object.
     *
     * <p>The resulting DTO contains identifiers and descriptive information
     * obtained from the division and its associated appropriation and fund.</p>
     *
     * @param division the division to convert
     * @return a DTO containing the division, appropriation, and fund information
     */
    public DivisionDto toDivisionDto(Division division) {
        // todo: reconsider the other entity ids. are they necessary?
        var dto = dataManager.create(DivisionDto.class);
        dto.setAppropriationId(division.getAppropriation().getId());
        dto.setId(division.getId());
        dto.setDivisionCode(division.getDivisionCode());
        dto.setTitle(division.getTitle());
        dto.setBudgetOrg(division.getBudgetOrg());
        dto.setBudgetFiscalYear(division.getAppropriation().getBudgetFiscalYear());
        dto.setFundId(division.getFund().getId());
        dto.setFundCode(division.getFund().getFundCode());
        return dto;
    }

    /**
     * Returns the funds applicable to the specified division.
     *
     * <p>The result always includes the division's assigned fund. It also
     * includes the appropriation two-year fund for the Education division and
     * the JITF fund for the Administration division.</p>
     *
     * @param division the division for which applicable funds are requested
     * @return the applicable funds, or an empty list if {@code division} is
     * {@code null}
     * @implNote This operation belongs to {@code DivisionService} because it
     * requires division-specific lookups. Placing it in {@code FundService}
     * would create a circular dependency between the services.
     */
    public List<Fund> getDivisionFundList(Division division) {
        if (division == null) {
            return List.of();
        }

        var appropriation = division.getAppropriation();

        List<Fund> funds = new ArrayList<>();
        funds.add(division.getFund());

        if (division.equals(findEducationDivision(appropriation))) {
            funds.add(fundService.getAppropriationTwoYearFund());
        }

        if (division.equals(findAdministrationDivision(appropriation))) {
            funds.add(fundService.getJitfFund());
        }

        return funds;
    }

    /**
     * Returns the divisions associated with the specified appropriation and fund.
     *
     * @param appropriation the appropriation for which divisions are requested
     * @param fund the fund associated with the requested divisions
     * @return the matching divisions, ordered by division code
     */
    public List<Division> fetchDivisions(Appropriation appropriation, Fund fund) {
        return dataManager.load(Division.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.equal("appropriation", appropriation),
                        PropertyCondition.equal("fund", fund)))
                .sort(Sort.by("divisionCode"))
                .list();
    }

    /**
     * Returns the divisions funded by an appropriation fund for the specified
     * appropriation.
     *
     * <p>The result includes divisions assigned to either the appropriation
     * one-year fund or the appropriation two-year fund.</p>
     *
     * @param appropriation the appropriation for which divisions are requested
     * @return the appropriation-funded divisions, ordered by division code
     */
    public List<Division> fetchAppropriationDivisions(Appropriation appropriation) {
        return dataManager.load(Division.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.equal("appropriation", appropriation),
                        PropertyCondition.inList("fund", List.of(
                                fundService.getAppropriationOneYearFund(),
                                fundService.getAppropriationTwoYearFund()))))
                .sort(Sort.by("divisionCode"))
                .list();
    }

    /**
     * Returns the cost organization divisions for the specified appropriation.
     *
     * <p>The result includes divisions assigned to the appropriation one-year
     * fund and excludes the OBBBA budget organization.</p>
     *
     * @param appropriation the appropriation for which divisions are requested
     * @return the cost organization divisions, ordered by division code
     */
    public List<Division> fetchCostOrgDivisions(Appropriation appropriation) {
        return dataManager.load(Division.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.equal("appropriation", appropriation),
                        PropertyCondition.equal("fund", fundService.getAppropriationOneYearFund()),
                        PropertyCondition.notEqual("budgetOrg", OBBBA_BUDGET_ORG)))
                .sort(Sort.by("divisionCode"))
                .list();
    }

    /**
     * Returns either foundation or non-foundation divisions for the specified
     * appropriation.
     *
     * @param appropriation the appropriation for which divisions are requested
     * @param foundation {@code true} to return divisions assigned to the
     * foundation fund; {@code false} to return divisions assigned to other funds
     * @return the matching divisions, ordered by division code
     */
    public List<Division> fetchDivisions(Appropriation appropriation, boolean foundation) {
        return dataManager.load(Division.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.equal("appropriation", appropriation),
                        foundationCondition(foundation)))
                .sort(Sort.by("divisionCode"))
                .list();
    }

    /**
     * Returns the non-foundation divisions for the specified appropriation.
     *
     * @param appropriation the appropriation for which divisions are requested
     * @return the non-foundation divisions, ordered by division code
     */
    public List<Division> fetchNonFoundationDivisions(Appropriation appropriation) {
        return fetchDivisions(appropriation, false);
    }

    /**
     * Returns the foundation divisions for the specified appropriation.
     *
     * @param appropriation the appropriation for which divisions are requested
     * @return the foundation divisions, ordered by division code
     */
    public List<Division> fetchFoundationDivisions(Appropriation appropriation) {
        return fetchDivisions(appropriation, true);
    }

    /**
     * Returns divisions that have at least one associated obligation.
     *
     * @param appropriation the appropriation for which divisions are requested
     * @param foundation {@code true} to return foundation divisions;
     * {@code false} to return non-foundation divisions
     * @return the matching divisions that have an associated obligation,
     * ordered by division code
     */
    public List<Division> fetchObligationDivisions(Appropriation appropriation, boolean foundation) {
        return dataManager.load(Division.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.equal("appropriation", appropriation),
                        foundationCondition(foundation),
                        JpqlCondition.create(
                                "EXISTS (SELECT 1 FROM fis_Obligation obl WHERE obl.activity.division = e)",
                                null)))
                .sort(Sort.by("divisionCode"))
                .list();
    }

    /**
     * Creates a division search list from the specified appropriations.
     *
     * <p>The result contains at most one division for each division code. When
     * the same division code occurs in multiple appropriations, the division
     * from the most recent budget fiscal year is retained. The result is ordered
     * by division code.</p>
     *
     * @param fiscalYears the appropriations included in the search
     * @param foundation {@code true} to include foundation divisions;
     * {@code false} to include non-foundation divisions
     * @return the matching divisions, with the most recent division retained
     * for each division code, or an empty list if {@code fiscalYears} is empty
     */
    public List<Division> fetchDivisionSearchList(List<Appropriation> fiscalYears, boolean foundation) {
        if (fiscalYears.isEmpty()) {
            return List.of();
        }
        List<Division> candidates = dataManager.load(Division.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.inList("appropriation", fiscalYears),
                        foundationCondition(foundation)))
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE)
                        .add("appropriation", FetchPlan.BASE))
                .list();

        // Newest fiscal year wins for each division code
        Map<String, Division> newestByCode = candidates.stream()
                .sorted(Comparator.comparing(
                        (Division d) -> d.getAppropriation().getBudgetFiscalYear()).reversed())
                .collect(Collectors.toMap(
                        Division::getDivisionCode,
                        Function.identity(),
                        (newest, _) -> newest,
                        TreeMap::new));

        return List.copyOf(newestByCode.values());
    }

    /**
     * Finds a division by an entity property within the specified appropriation.
     *
     * @param appropriation the appropriation containing the division
     * @param property the division property to compare
     * @param value the property value for which to search
     * @return the first matching division, or {@code null} if no division is found
     */
    private Division findDivision(Appropriation appropriation, String property, String value) {
        return dataManager.load(Division.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.equal("appropriation", appropriation),
                        PropertyCondition.equal(property, value)))
                .maxResults(1)
                .optional()
                .orElse(null);
    }

    /**
     * Finds a division by its division code within the specified appropriation.
     *
     * @param appropriation the appropriation containing the division
     * @param divisionCode the division code for which to search
     * @return the matching division, or {@code null} if it is not found
     */
    private Division findDivisionByCode(Appropriation appropriation, String divisionCode) {
        return findDivision(appropriation, "divisionCode", divisionCode);
    }

    /**
     * Finds a division by its budget organization code within the specified
     * appropriation.
     *
     * @param appropriation the appropriation containing the division
     * @param budgetOrg the budget organization code for which to search
     * @return the matching division, or {@code null} if it is not found
     */
    private Division findDivisionByBudgetOrg(Appropriation appropriation, String budgetOrg) {
        return findDivision(appropriation, "budgetOrg", budgetOrg);
    }

    /**
     * Creates a query condition that selects either foundation or
     * non-foundation divisions.
     *
     * @param foundation {@code true} to select divisions assigned to the
     * foundation fund; {@code false} to select divisions assigned to other funds
     * @return the condition used to filter divisions by foundation status
     */
    private Condition foundationCondition(boolean foundation) {
        Fund foundationFund = fundService.getFoundationFund();
        return foundation
                ? PropertyCondition.equal("fund", foundationFund)
                : PropertyCondition.notEqual("fund", foundationFund);
    }
}