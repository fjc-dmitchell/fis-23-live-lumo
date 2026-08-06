package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.Fund;
import gov.fjc.fis.entity.dto.DivisionDto;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("fis_DivisionService")
public class DivisionService {
    @Autowired
    private DataManager dataManager;
    @Lazy
    @Autowired
    private FundService fundService;
    @Autowired
    private AppropriationService appropriationService;

    final private String EDUCATION_DIVISION_CODE = "2";
    final private String RESEARCH_DIVISION_CODE = "4";
    final private String OBBBA_BUDGET_ORG = "JXXMAPP";

    // temporary service while creating views for Jmix 2.2
    public List<Division> getDivisions() {
        return dataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d INNER JOIN fis_Appropriation a ORDER BY a.budgetFiscalYear DESC, d.divisionCode ASC")
                .list();
    }

    public List<Division> getCostOrgDivisions(Appropriation appropriation) {
        var oneYearFund = fundService.getAppropriationOneYearFund();
        return dataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.appropriation=:appropriation"
                        + " AND d.fund=:oneYearFund")
                .parameter("appropriation", appropriation)
                .parameter("oneYearFund", oneYearFund)
                .list();
    }

//    public Division fetchMandatoryDivision(Appropriation appropriation) {
//        var obbbaBudgetOrg = "JXXMAPP";
//        var divisions = dataManager.load(Division.class)
//                .query("SELECT d FROM fis_Division d"
//                        + " WHERE d.appropriation=:appropriation AND d.budgetOrg=:budgetOrg")
//                .parameter("appropriation", appropriation)
//                .parameter("budgetOrg", obbbaBudgetOrg)
//                .list();
//        if (divisions.isEmpty()) {
//            return null;
//        } else {
//            return divisions.getFirst();
//        }
//    }

    public Division fetchMandatoryDivision(Appropriation appropriation) {
        return dataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.appropriation = :appropriation"
                        + " AND d.budgetOrg = :budgetOrg")
                .parameter("appropriation", appropriation)
                .parameter("budgetOrg", OBBBA_BUDGET_ORG)
                .maxResults(1)
                .optional()
                .orElse(null);
    }

    public List<Division> getAppropriationDivisions(Appropriation appropriation) {
        List<Fund> funds = new ArrayList<>();
        funds.add(fundService.getAppropriationOneYearFund());
        funds.add(fundService.getAppropriationTwoYearFund());
        return dataManager.load(Division.class)
                .query("SELECT d from fis_Division d"
                        + " WHERE d.appropriation = :appropriation"
                        + " AND d.fund in :funds"
                        + " ORDER BY d.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("funds", funds)
                .list();
    }

    public List<Division> getDivisions(Appropriation appropriation, boolean foundation) {
        return dataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.appropriation = :appropriation"
                        + " AND ((:foundation = true AND d.fund = :foundationFund) "
                        + " OR (:foundation = false AND d.fund <> :foundationFund))"
                        + " ORDER BY d.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("foundation", foundation)
                .parameter("foundationFund", fundService.getFoundationFund())
                .list();
    }

    public List<Division> fetchNonFoundationDivisions(Appropriation appropriation) {
        return getDivisions(appropriation, false);
    }

    public List<Division> getAllDivisionsWithBudgetOrgs(Appropriation appropriation) {
        return dataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.appropriation = :appropriation"
                        + " AND d.budgetOrg IS NOT NULL AND d.budgetOrg <> ''"
                        + " ORDER BY d.divisionCode")
                .parameter("appropriation", appropriation)
                .list();
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
                .query("SELECT distinct d FROM fis_Division d"
                        + " INNER JOIN fis_Obligation obl ON obl.activity.division.id = d.id"
                        + " WHERE d.appropriation = :appropriation"
                        + " AND ((:foundation = true AND d.fund = :foundationFund)"
                        + " OR (:foundation = false AND d.fund <> :foundationFund))"
                        + " ORDER BY d.divisionCode")
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
                            .query("SELECT d FROM fis_Division d"
                                    + " WHERE d.appropriation = :year"
                                    + " AND d.divisionCode NOT IN :divisionCodes"
                                    + " AND ((:foundation = true AND d.fund = :foundationFund) "
                                    + " OR (:foundation = false AND d.fund <> :foundationFund))")
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

    public Division getDivisionByCode(List<Appropriation> appropriations, String divisionCode) {
        return dataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.divisionCode = :divisionCode"
                        + " AND d.appropriation.budgetFiscalYear = (SELECT MAX(e.budgetFiscalYear)"
                        + " FROM fis_Appropriation e WHERE e IN :appropriations)")
                .parameter("divisionCode", divisionCode)
                .parameter("appropriations", appropriations)
                .optional().orElse(null);
    }

    public List<Division> fetchDivisions(Appropriation appropriation, Fund fund) {
        return dataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.appropriation = :appropriation"
                        + " AND d.fund=:fund"
                        + " ORDER BY d.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("fund", fund)
                .list();
    }

    public DivisionDto divisionToDivisionDto(Division division) {
        var dto = dataManager.create(DivisionDto.class);
        dto.configureDivisionDto(division);
        return dto;
    }

    public List<DivisionDto> convertDivisionsToDivisionDtos(List<Division> divisions) {
        List<DivisionDto> divisionDtos = new ArrayList<>();
        DivisionDto dto;

        for (var division : divisions) {
            dto = dataManager.create(DivisionDto.class);
            dto.configureDivisionDto(division);
            divisionDtos.add(dto);
        }
        return divisionDtos;
    }

//    public List<DivisionDto> getDivisionDtos(Appropriation appropriation, Fund fund) {
//        return getDivisions(appropriation, fund).stream().map(division -> {
//            var dto = dataManager.create(DivisionDto.class);
//            dto.configureDivisionDto(division);
//            return dto;
//        }).toList();
//    }

    public List<DivisionDto> getDivisionDtos(Appropriation appropriation, List<Fund> funds) {
        var divisions = dataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.appropriation = :appropriation AND d.fund IN :funds"
                        + " ORDER BY d.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("funds", funds)
                .list();
        return convertDivisionsToDivisionDtos(divisions);
    }

    /**
     * Create list of divisions used in report parameter screens.
     * Note special case for multi-year fund; both one and two year divisions included.
     *
     * @param fund         Fund entity
     * @param bfy          Appropriation entity
     * @param combinedYear for multi-year fund, return both one and two year divisions
     * @return list of division entities
     */
    public List<Division> getDivisionReportList(Fund fund, Appropriation bfy, boolean combinedYear) {
        List<Fund> funds = new ArrayList<>();
        funds.add(fund);
        if (combinedYear && fund.equals(fundService.getAppropriationTwoYearFund())) {
            funds.add(fundService.getAppropriationOneYearFund());
        }
        return dataManager.load(Division.class)
                .query("select d from fis_Division d" +
                        " where d.appropriation = :bfy" +
                        " and d.fund in :funds" +
                        " order by d.divisionCode")
                .parameter("bfy", bfy)
                .parameter("funds", funds)
                .list();
    }

    //    /**
//     * Get division allocations for Category
//     *
//     * @param category
//     * @return list of divisions with allocations
//     */
//    public List<KeyValueEntity> getCategoryAllocations(Category category) {
//        return dataManager.loadValues(
//                        "SELECT div.title, alloc.oneYearAmount, alloc.twoYearAmount" +
//                                " FROM fis_Division div" +
//                                " INNER join fis_DivisionAllocation alloc ON alloc.division = div" +
//                                " WHERE alloc.category = :category" +
//                                " ORDER BY div.divisionCode")
//                .properties("division", "oneYearAmount", "twoYearAmount")
//                .parameter("category", category)
//                .list();
//    }

    /**
     * why would I want an empty division created? this doesn't make sense!
     *
     * @param appropriation
     * @return
     */
    public Division getEducationDivisionNull(Appropriation appropriation) {
        return dataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e" +
                        " WHERE e.appropriation = :appropriation" +
                        " AND e.divisionCode = '2'")
                .parameter("appropriation", appropriation)
                .optional()
                .orElse(dataManager.create(Division.class));
    }

    /**
     * for reports, check for null!
     *
     * @param appropriation
     * @return
     */
    public Division getEducationDivision(Appropriation appropriation) {
        return getDivision(appropriation, EDUCATION_DIVISION_CODE);
//        return dataManager.load(Division.class)
//                .query("SELECT e FROM fis_Division e" +
//                        " WHERE e.appropriation = :appropriation" +
//                        " AND e.divisionCode = :educationDivisionCode")
//                .parameter("appropriation", appropriation)
//                .parameter("educationDivisionCode", EDUCATION_DIVISION_CODE)
//                .optional()
//                .orElse(null);
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

    public Division getResearchDivision(Appropriation appropriation) {
        return getDivision(appropriation, RESEARCH_DIVISION_CODE);
//        return dataManager.load(Division.class)
//                .query("SELECT e FROM fis_Division e" +
//                        " WHERE e.appropriation = :appropriation" +
//                        " AND e.divisionCode = '4'")
//                .parameter("appropriation", appropriation)
//                .parameter("appropriation", appropriation)
//                .optional()
//                .orElse(null);
    }

    /**
     * this is a bad idea but sometimes necessary. used for Nancy's ED program analysis
     *
     * @return
     */
    public String getEducationDivivisionCode() {
        return EDUCATION_DIVISION_CODE;
    }

    public Division getAdministrationDivision(Appropriation appropriation) {
        return dataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e" +
                        " WHERE e.appropriation = :appropriation" +
                        " AND e.budgetOrg = 'JXXXXXF'")
                .parameter("appropriation", appropriation)
                .optional()
                .orElse(null);
    }

//    public List<Division> getPositionDivisions() {
//        var funds = fundService.getAppropriatedFundsList();
//        var appropriation = appropriationService.getCurrentBudgetFiscalYear();
//        return dataManager.load(Division.class)
//                .query("SELECT d FROM fis_Division d"
//                        + " WHERE d.appropriation = :appropriation"
//                        + " AND d.fund IN :funds"
//                        + " AND d.budgetOrg IN (SELECT DISTINCT p.jlCostOrgCd FROM fis_Position p)"
//                        + " ORDER BY d.divisionCode")
//                .parameter("appropriation", appropriation)
//                .parameter("funds", funds)
//                .list();
//    }

    public Map<String, String> getPositionDivisions() {
        var funds = fundService.getAppropriatedFundsList();
        var appropriation = appropriationService.getCurrentBudgetFiscalYear();
        var divisions = dataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.appropriation = :appropriation"
                        + " AND d.fund IN :funds"
                        + " AND d.budgetOrg IN (SELECT DISTINCT p.jlCostOrgCd FROM fis_Position p)"
                        + " ORDER BY d.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("funds", funds)
                .list();
        return divisions.stream().collect(Collectors.toMap(Division::getBudgetOrg, Division::getTitleAndBudgetOrg));
    }

    public Division getDivisionByBudgetOrg(String budgetOrg) {
        var funds = fundService.getAppropriatedFundsList();
        var appropriation = appropriationService.getCurrentBudgetFiscalYear();
        return dataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.appropriation = :appropriation"
                        + " AND d.fund IN :funds"
                        + " AND d.budgetOrg = :budgetOrg"
                        + " ORDER BY d.divisionCode")
                .parameter("appropriation", appropriation)
                .parameter("funds", funds)
                .parameter("budgetOrg", budgetOrg)
                .optional()
                .orElse(null);
    }
}