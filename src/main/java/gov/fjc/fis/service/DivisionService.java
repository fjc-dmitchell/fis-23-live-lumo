package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.Fund;
import gov.fjc.fis.entity.dto.DivisionDto;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("fis_DivisionService")
public class DivisionService {

    private final DataManager dataManager;

    private final AppropriationService appropriationService;
    private final FundService fundService;

    public DivisionService(DataManager dataManager, FundService fundService, AppropriationService appropriationService) {
        this.dataManager = dataManager;
        this.fundService = fundService;
        this.appropriationService = appropriationService;
    }

    final private String EDUCATION_DIVISION_CODE = "2";
    final private String RESEARCH_DIVISION_CODE = "4";
    final private String OBBBA_BUDGET_ORG = "JXXMAPP";

    public List<Division> fetchCostOrgDivisions(Appropriation appropriation) {
        var oneYearFund = fundService.getAppropriationOneYearFund();
        return dataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e"
                        + " WHERE e.appropriation=:appropriation"
                        + " AND e.fund=:oneYearFund")
                .parameter("appropriation", appropriation)
                .parameter("oneYearFund", oneYearFund)
                .list();
    }

    public Division findObbbaDivision(Appropriation appropriation) {
        return dataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e"
                        + " WHERE e.appropriation = :appropriation"
                        + " AND e.budgetOrg = :budgetOrg")
                .parameter("appropriation", appropriation)
                .parameter("budgetOrg", OBBBA_BUDGET_ORG)
                .maxResults(1)
                .optional()
                .orElse(null);
    }

    public List<Division> fetchAppropriationDivisions(Appropriation appropriation) {
        List<Fund> funds = new ArrayList<>();
        funds.add(fundService.getAppropriationOneYearFund());
        funds.add(fundService.getAppropriationTwoYearFund());
        return dataManager.load(Division.class)
                .query("SELECT e from fis_Division e"
                        + " WHERE e.appropriation = :appropriation"
                        + " AND e.fund in :funds"
                        + " ORDER BY e.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("funds", funds)
                .list();
    }

    public List<Division> fetchDivisions(Appropriation appropriation, boolean foundation) {
        return dataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e"
                        + " WHERE e.appropriation = :appropriation"
                        + " AND ((:foundation = true AND e.fund = :foundationFund) "
                        + " OR (:foundation = false AND e.fund <> :foundationFund))"
                        + " ORDER BY e.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("foundation", foundation)
                .parameter("foundationFund", fundService.getFoundationFund())
                .list();
    }

    public List<Division> fetchNonFoundationDivisions(Appropriation appropriation) {
        return fetchDivisions(appropriation, false);
    }

    /**
     * used by obligation lookup screen to find divisions for given appropriation year, foundation fund
     *
     * @param appropriation required
     * @param foundation    boolean to indicate whether to use FJC Foundation fund
     * @return List of Divisions
     */
    public List<Division> getObligationDivisionsForAppropriationFoundation(Appropriation appropriation, boolean foundation) {
        return dataManager.load(Division.class)
                .query("SELECT distinct e FROM fis_Division e"
                        + " INNER JOIN fis_Obligation obl ON obl.activity.division.id = e.id"
                        + " WHERE e.appropriation = :appropriation"
                        + " AND ((:foundation = true AND e.fund = :foundationFund)"
                        + " OR (:foundation = false AND e.fund <> :foundationFund))"
                        + " ORDER BY e.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("foundation", foundation)
                .parameter("foundationFund", fundService.getFoundationFund())
                .list();
    }

//    public List<Division> getObligationDivisionsForAppropriation(Appropriation appropriation) {
//        return getObligationDivisionsForAppropriationFoundation(appropriation, false);
//    }

    /**
     * Create list of divisions from all fiscal years in selection. Division titles
     * are from most recent appropriation year provided that contains division.
     *
     * @param fiscalYears list of Appropriation entities
     * @param foundation  boolean flag used to select FJC Foundation divisions only
     * @return list of division entities
     */
    public List<Division> fetchDivisionSearchList(List<Appropriation> fiscalYears, boolean foundation) {
        fiscalYears = fiscalYears.stream().sorted(Comparator.comparing(Appropriation::getBudgetFiscalYear).reversed()).toList();
        var foundationFund = fundService.getFoundationFund();
        List<Division> divisionList = new ArrayList<>();
        Set<String> divisionCodes = null;

        for (Appropriation year : fiscalYears) {
            List<Division> divisionsInBfyList =
                    dataManager.load(Division.class)
                            .query("SELECT e FROM fis_Division e"
                                    + " WHERE e.appropriation = :year"
                                    + " AND e.divisionCode NOT IN :divisionCodes"
                                    + " AND ((:foundation = true AND e.fund = :foundationFund) "
                                    + " OR (:foundation = false AND e.fund <> :foundationFund))")
                            .parameter("year", year)
                            .parameter("divisionCodes", divisionCodes)
                            .parameter("foundation", foundation)
                            .parameter("foundationFund", foundationFund)
                            .list();
            divisionList.addAll(divisionsInBfyList);
            divisionCodes = divisionList.stream().map(Division::getDivisionCode).collect(Collectors.toSet());
        }

        return divisionList.stream().sorted(Comparator.comparing(Division::getDivisionCode)).toList();
    }

    public List<Division> fetchDivisions(Appropriation appropriation, Fund fund) {
        return dataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e"
                        + " WHERE e.appropriation = :appropriation"
                        + " AND e.fund=:fund"
                        + " ORDER BY e.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("fund", fund)
                .list();
    }

    public DivisionDto divisionToDivisionDto(Division division) {
        var dto = dataManager.create(DivisionDto.class);
        dto.configureDivisionDto(division);
        return dto;
    }

    public Division findEducationDivision(Appropriation appropriation) {
        return getDivision(appropriation, EDUCATION_DIVISION_CODE);
    }

    private Division getDivision(Appropriation appropriation, String divisionCode) {
        return dataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e" +
                        " WHERE e.appropriation = :appropriation" +
                        " AND e.divisionCode = :divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("divisionCode", divisionCode)
                .optional()
                .orElse(null);
    }

    public List<Division> fetchEducationAndObbaDivisions(Appropriation appropriation) {
        return dataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e" +
                        " WHERE e.appropriation = :appropriation" +
                        " AND (e.divisionCode = :divisionCode OR e.budgetOrg = :budgetOrg)")
                .parameter("appropriation", appropriation)
                .parameter("divisionCode", EDUCATION_DIVISION_CODE)
                .parameter("budgetOrg", OBBBA_BUDGET_ORG)
                .list();
    }

    public Division findResearchDivision(Appropriation appropriation) {
        return getDivision(appropriation, RESEARCH_DIVISION_CODE);
    }

    public String getEducationDivisionCode() {
        return EDUCATION_DIVISION_CODE;
    }

    public Division findAdministrationDivision(Appropriation appropriation) {
        return dataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e" +
                        " WHERE e.appropriation = :appropriation" +
                        " AND e.budgetOrg = 'JXXXXXF'")
                .parameter("appropriation", appropriation)
                .optional()
                .orElse(null);
    }

    public Map<String, String> getPositionDivisions() {
        var funds = fundService.getAppropriatedFundsList();
        var appropriation = appropriationService.getCurrentBudgetFiscalYear();
        var divisions = dataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e"
                        + " WHERE e.appropriation = :appropriation"
                        + " AND e.fund IN :funds"
                        + " AND e.budgetOrg IN (SELECT DISTINCT p.jlCostOrgCd FROM fis_Position p)"
                        + " ORDER BY e.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("funds", funds)
                .list();
        return divisions.stream().collect(Collectors.toMap(Division::getBudgetOrg, Division::getTitleAndBudgetOrg));
    }

    // placed here to avoid circular dependency with Fund
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
}