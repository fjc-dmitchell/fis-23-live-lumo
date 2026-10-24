package gov.fjc.fis.service.report;

import gov.fjc.fis.app.BigDecimalUtils;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.entity.dto.*;
import gov.fjc.fis.reportdata.BudgetRequestReportData;
import gov.fjc.fis.service.*;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component("fis_BudgetRequestReportService")
public class BudgetRequestReportService {

    private final DataManager dataManager;
    private final ObligationService obligationService;
    private final ActivityReimbursementService activityReimbursementService;
    private final ActivityProjectionService activityProjectionService;
    private final ActivityService activityService;
    private final DivisionService divisionService;
    private final ObjectClassService objectClassService;
    private final ObjectCategoryService objectCategoryService;
    private final AppropriationService appropriationService;
    private final CategoryFundTypeAggregator categoryFundTypeAggregator;
    private final CategoryAmountAggregator categoryAmountAggregator;

    public BudgetRequestReportService(DataManager dataManager,
                                      AppropriationService appropriationService,
                                      ObjectCategoryService objectCategoryService,
                                      ObjectClassService objectClassService,
                                      DivisionService divisionService,
                                      ActivityService activityService,
                                      ActivityProjectionService activityProjectionService,
                                      ActivityReimbursementService activityReimbursementService,
                                      ObligationService obligationService,
                                      CategoryFundTypeAggregator categoryFundTypeAggregator,
                                      CategoryAmountAggregator categoryAmountAggregator) {
        this.dataManager = dataManager;
        this.appropriationService = appropriationService;
        this.objectCategoryService = objectCategoryService;
        this.objectClassService = objectClassService;
        this.divisionService = divisionService;
        this.activityService = activityService;
        this.activityProjectionService = activityProjectionService;
        this.activityReimbursementService = activityReimbursementService;
        this.obligationService = obligationService;
        this.categoryFundTypeAggregator = categoryFundTypeAggregator;
        this.categoryAmountAggregator = categoryAmountAggregator;
    }

    public BudgetRequestReportData generateReportData(Appropriation appropriation) {

//        var activities = activityService.getBiFiscalActivityDtos(appropriation);
        var activities = activityService.getBiFiscalActivityDtos(appropriation);
        var obligations = obligationService.getObligationDtos(appropriation, activities);
        var projections = activityProjectionService.getProjectionDtos(appropriation, activities, false);
        var reimbursements = activityReimbursementService.getReimbursementDtos(appropriation, activities);
        activityService.updateActivityAmounts(activities, obligations, projections, reimbursements);

        // create the category reports
        var reportData = getCategoryDtos(appropriation, obligations, projections, reimbursements);


        var objectClasses = getObjectClassDtos(appropriation, obligations, projections, reimbursements);
        // categories could be derived from object classes
//        var categories = getCategoryDtos(appropriation, obligations, projections, reimbursements);
//        var hillPLan = getHillPlanCategoryDtos(categories);
        var divisions = getDivisionDtos(appropriation, obligations, projections, reimbursements);

        reportData.setDivisions(divisions);
        reportData.setActivities(activities);
        reportData.setObligations(obligations);
        reportData.setProjections(projections);
        reportData.setReimbursements(reimbursements);
        reportData.setObjectClasses(objectClasses);
//        reportData.setCategories(categories);
//        reportData.setHillPlanCategories(hillPLan);

        reportData.setPriorTwoYearCarriedForward(getPriorTwoYearBalance(appropriation));

        return reportData;
    }

    private BigDecimal getPriorTwoYearBalance(Appropriation appropriation) {
        Appropriation priorAppropriation = appropriationService.getPreviousFiscalYear(appropriation);

        var appropriationSpendingAuthority = appropriationService.getSpendingAuthority(priorAppropriation);
        BigDecimal authority = appropriationSpendingAuthority.getValue("two_year_total");
        var twoYearActivitiesInCurrentYear = activityService.getTwoYearActivitiesHeldInFiscalYear(priorAppropriation);
        return authority.subtract(activityProjectionService.sumProjections(twoYearActivitiesInCurrentYear))
                .subtract(obligationService.sumObligations(twoYearActivitiesInCurrentYear))
                .add(activityReimbursementService.sumReimbursements(twoYearActivitiesInCurrentYear));
    }

    public BudgetRequestReportData getCategoryDtos(
            Appropriation appropriation,
            List<ObligationDto> obligationDtos,
            List<ActivityProjectionDto> projectionDtos,
            List<ActivityReimbursementDto> reimbursementDtos) {
        Appropriation priorAppropriation = appropriationService.getPreviousFiscalYear(appropriation);
        var reportData = new BudgetRequestReportData(appropriation, priorAppropriation);

        var spendingAuthority = appropriationService.getSpendingAuthority(appropriation);
        reportData.setOneYearSpendingAuthority(spendingAuthority.getValue("one_year_total"));
        reportData.setTwoYearSpendingAuthority(spendingAuthority.getValue("two_year_total"));

        List<Appropriation> fiscalYears = List.of(appropriation, priorAppropriation);

        // Nancy’s special reporting categories
        Set<String> showCategories = Set.of(
                "11", "12", "13", "21", "22", "23", "24", "25", "26", "31", "90", "91"
        );

        List<ObjectCategory> categories =
                objectCategoryService.fetchCategorySearchList(fiscalYears);

        // --------------------------------------------------------------------
        // PRE‑GROUP INPUT DATA
        // --------------------------------------------------------------------
        Map<String, List<ObligationDto>> obligationsByMoc =
                obligationDtos.stream()
                        .collect(Collectors.groupingBy(ObligationDto::getMajorObjectClass));

        Map<String, List<ActivityProjectionDto>> projectionsByMoc =
                projectionDtos.stream()
                        .collect(Collectors.groupingBy(ActivityProjectionDto::getMajorObjectClass));

        Map<String, List<ActivityReimbursementDto>> reimbursementsByMoc =
                reimbursementDtos.stream()
                        .collect(Collectors.groupingBy(ActivityReimbursementDto::getMajorObjectClass));

        // --------------------------------------------------------------------
        // ROLLUP BUCKETS
        // --------------------------------------------------------------------
        ObjectCategoryDto uncategorized = dataManager.create(ObjectCategoryDto.class);
        uncategorized.setMajorObjectClass("00");
        uncategorized.setTitle("Uncategorized");

        ObjectCategoryDto undefined = dataManager.create(ObjectCategoryDto.class);
        undefined.setMajorObjectClass("91");
        undefined.setTitle("Undefined Disbursements");

        List<ObjectCategoryDto> categoryDtos = new ArrayList<>();

        for (ObjectCategory cat : categories) {

            String moc = cat.getMajorObjectClass();
            ObjectCategoryDto dto;

            // Consolidate 90 + 91 into "undefined"
            if ("90".equals(moc) || "91".equals(moc)) {
                dto = undefined;
            }
            // Shown category
            else if (showCategories.contains(moc)) {
                dto = dataManager.create(ObjectCategoryDto.class);
                dto.setId(cat.getId());
                dto.setMajorObjectClass(moc);
                dto.setTitle(cat.getTitle());
                categoryDtos.add(dto);
            }
            // Uncategorized
            else {
                dto = uncategorized;
            }

            // --------------------------------------------------------------------
            // LOOKUP PRE‑GROUPED DATA
            // --------------------------------------------------------------------
            List<ObligationDto> ob =
                    obligationsByMoc.getOrDefault(moc, Collections.emptyList());
            List<ActivityProjectionDto> proj =
                    projectionsByMoc.getOrDefault(moc, Collections.emptyList());
            List<ActivityReimbursementDto> reimb =
                    reimbursementsByMoc.getOrDefault(moc, Collections.emptyList());


            // === AGGREGATE AMOUNTS ===
            categoryAmountAggregator.accumulateAmounts(dto, ob, proj, reimb);

            // === DISCRETIONARY / MANDATORY ===
            categoryFundTypeAggregator.accumulateFundTypes(dto, ob, proj, reimb);

        }

        // Add rollups
        Collections.addAll(categoryDtos, undefined, uncategorized);

        reportData.setCategories(categoryDtos);
        reportData.setHillPlanCategories(getHillPlanCategoryDtos(categoryDtos));

        return reportData;
    }

    public List<ObjectClassDto> getObjectClassDtos(
            Appropriation appropriation,
            List<ObligationDto> obligationDtos,
            List<ActivityProjectionDto> projectionDtos,
            List<ActivityReimbursementDto> reimbursementDtos) {

        // Categories that appear in the report
        Set<String> showCategories = Set.of("11", "12", "13", "21", "22", "23", "24", "25", "26", "31", "91");

        // Pre-group everything once (major performance + clarity improvement)
        Map<String, List<ObligationDto>> obligationsByBoc =
                obligationDtos.stream()
                        .collect(Collectors.groupingBy(ObligationDto::getBudgetObjectClass));

        Map<String, List<ActivityProjectionDto>> projectionsByBoc =
                projectionDtos.stream()
                        .collect(Collectors.groupingBy(ActivityProjectionDto::getBudgetObjectClass));

        Map<String, List<ActivityReimbursementDto>> reimbursementsByBoc =
                reimbursementDtos.stream()
                        .collect(Collectors.groupingBy(ActivityReimbursementDto::getBudgetObjectClass));

        Appropriation prior = appropriationService.getPreviousFiscalYear(appropriation);
        List<Appropriation> fiscalYears = List.of(appropriation, prior);

        List<ObjectClass> objectClasses =
                objectClassService.fetchObjectClassSearchList(fiscalYears, null, true);

        List<ObjectClassDto> result = new ArrayList<>();

        for (ObjectClass obj : objectClasses) {

            String boc = obj.getBudgetObjectClass();

            ObjectClassDto dto = dataManager.create(ObjectClassDto.class);
            dto.setId(obj.getId());
            dto.setBudgetObjectClass(boc);
            dto.setTitle(obj.getTitle());

            // Lookups are now O(1) instead of repeatedly scanning lists
            List<ObligationDto> ob =
                    obligationsByBoc.getOrDefault(boc, Collections.emptyList());

            List<ActivityProjectionDto> proj =
                    projectionsByBoc.getOrDefault(boc, Collections.emptyList());

            List<ActivityReimbursementDto> reimb =
                    reimbursementsByBoc.getOrDefault(boc, Collections.emptyList());

            // === OBLIGATIONS ===
            dto.addPriorTwoYearObligated(BigDecimalUtils.sum(ob, ObligationDto::getPriorTwoYearObligated));
            dto.addPriorTwoYearDisbursed(BigDecimalUtils.sum(ob, ObligationDto::getPriorTwoYearDisbursed));
            dto.addCurrentOneYearObligated(BigDecimalUtils.sum(ob, ObligationDto::getCurrentOneYearObligated));
            dto.addCurrentOneYearDisbursed(BigDecimalUtils.sum(ob, ObligationDto::getCurrentOneYearDisbursed));
            dto.addCurrentTwoYearObligated(BigDecimalUtils.sum(ob, ObligationDto::getCurrentTwoYearObligated));
            dto.addCurrentTwoYearDisbursed(BigDecimalUtils.sum(ob, ObligationDto::getCurrentTwoYearDisbursed));

            // === PROJECTIONS ===
            dto.addPriorTwoYearProjected(BigDecimalUtils.sum(proj, ActivityProjectionDto::getPriorTwoYearAmount));
            dto.addCurrentOneYearProjected(BigDecimalUtils.sum(proj, ActivityProjectionDto::getCurrentOneYearAmount));
            dto.addCurrentTwoYearProjected(BigDecimalUtils.sum(proj, ActivityProjectionDto::getCurrentTwoYearAmount));

            // === REIMBURSEMENTS ===
            dto.addPriorTwoYearReimbursed(BigDecimalUtils.sum(reimb, ActivityReimbursementDto::getPriorTwoYearAmount));
            dto.addCurrentOneYearReimbursed(BigDecimalUtils.sum(reimb, ActivityReimbursementDto::getCurrentOneYearAmount));
            dto.addCurrentTwoYearReimbursed(BigDecimalUtils.sum(reimb, ActivityReimbursementDto::getCurrentTwoYearAmount));

            // Only include if flagged for reporting
            dto.setShowOnReport(showCategories.contains(boc));

            if (dto.showOnReport()) {
                result.add(dto);
            }
        }

        return result;
    }

    public List<ObjectCategoryDto> getHillPlanCategoryDtos(List<ObjectCategoryDto> categoryDtos) {
        List<ObjectCategoryDto> hillPlanDtos = new ArrayList<>();

        ObjectCategoryDto personnel = dataManager.create(ObjectCategoryDto.class);
        personnel.setTitle("Personnel Compensation & Benefits");

        ObjectCategoryDto travel = dataManager.create(ObjectCategoryDto.class);
        travel.setTitle("Travel");

        ObjectCategoryDto rent = dataManager.create(ObjectCategoryDto.class);
        rent.setTitle("Rent, Communication and Utilities");

        ObjectCategoryDto undefined = dataManager.create(ObjectCategoryDto.class);
        undefined.setTitle("Undefined Disbursements");

        ObjectCategoryDto other = dataManager.create(ObjectCategoryDto.class);
        other.setTitle("Other");

        ObjectCategoryDto hillPlanDto;

        for (ObjectCategoryDto categoryDto : categoryDtos) {
            hillPlanDto = switch (categoryDto.getMajorObjectClass()) {
                case "11", "12", "13" -> personnel;
                case "21" -> travel;
                case "23" -> rent;
                case "90", "91" -> undefined;
                default -> other;
            };

            hillPlanDto.addPriorYearProjected(categoryDto.getPriorYearProjected());
            hillPlanDto.addCurrentYearProjected(categoryDto.getOneYearProjected());
            hillPlanDto.addCurrentTwoYearProjected(categoryDto.getTwoYearProjected());

            hillPlanDto.addPriorYearReimbursed(categoryDto.getPriorYearReimbursed());
            hillPlanDto.addCurrentYearReimbursed(categoryDto.getOneYearReimbursed());
            hillPlanDto.addCurrentTwoYearReimbursed(categoryDto.getTwoYearReimbursed());

            hillPlanDto.addPriorYearObligated(categoryDto.getPriorYearObligated());
            hillPlanDto.addPriorYearDisbursed(categoryDto.getPriorYearDisbursed());
            hillPlanDto.addCurrentYearObligated(categoryDto.getOneYearObligated());
            hillPlanDto.addCurrentYearDisbursed(categoryDto.getOneYearDisbursed());
            hillPlanDto.addCurrentTwoYearObligated(categoryDto.getTwoYearObligated());
            hillPlanDto.addCurrentTwoYearDisbursed(categoryDto.getTwoYearDisbursed());
        }
        Collections.addAll(hillPlanDtos, personnel, travel, rent, undefined, other);
        return hillPlanDtos;
    }

    public List<DivisionDto> getDivisionDtos(
            Appropriation appropriation,
            List<ObligationDto> obligationDtos,
            List<ActivityProjectionDto> projectionDtos,
            List<ActivityReimbursementDto> reimbursementDtos) {

        Appropriation prior = appropriationService.getPreviousFiscalYear(appropriation);
        List<Appropriation> appropriations = List.of(appropriation, prior);
        List<Division> divisions = divisionService.fetchDivisionSearchList(appropriations, false);

        // Pre-group data by division code (removes repeated per-loop filtering)
        Map<String, List<ActivityProjectionDto>> projByDiv =
                projectionDtos.stream().collect(Collectors.groupingBy(ActivityProjectionDto::getDivisionCode));

        Map<String, List<ActivityReimbursementDto>> reimbByDiv =
                reimbursementDtos.stream().collect(Collectors.groupingBy(ActivityReimbursementDto::getDivisionCode));

        Map<String, List<ObligationDto>> obByDiv =
                obligationDtos.stream().collect(Collectors.groupingBy(ObligationDto::getDivisionCode));

        // Rollup buckets
        DivisionDto education = dataManager.create(DivisionDto.class);
        education.setTitle("Education & Training");

        DivisionDto research = dataManager.create(DivisionDto.class);
        research.setTitle("Research");

        DivisionDto programSupport = dataManager.create(DivisionDto.class);
        programSupport.setTitle("Program Support");

        DivisionDto mandatory = dataManager.create(DivisionDto.class);
        mandatory.setTitle("Mandatory Spending");

        for (Division div : divisions) {
            String code = div.getDivisionCode();

            // Determine which rollup bucket to accumulate into
            DivisionDto dto = switch (code) {
                case "2", "3", "5" -> education;
                case "4" -> research;
                case "9" -> mandatory;
                default -> programSupport;
            };

            // Lookup grouped data (O(1))
            List<ActivityProjectionDto> proj =
                    projByDiv.getOrDefault(code, Collections.emptyList());

            List<ActivityReimbursementDto> reimb =
                    reimbByDiv.getOrDefault(code, Collections.emptyList());

            List<ObligationDto> ob =
                    obByDiv.getOrDefault(code, Collections.emptyList());

            // === PROJECTIONS ===
            dto.addPriorTwoYearProjected(
                    BigDecimalUtils.sum(proj, ActivityProjectionDto::getPriorTwoYearAmount));
            dto.addCurrentOneYearProjected(
                    BigDecimalUtils.sum(proj, ActivityProjectionDto::getCurrentOneYearAmount));
            dto.addCurrentTwoYearProjected(
                    BigDecimalUtils.sum(proj, ActivityProjectionDto::getCurrentTwoYearAmount));

            // === REIMBURSEMENTS ===
            dto.addPriorTwoYearReimbursed(
                    BigDecimalUtils.sum(reimb, ActivityReimbursementDto::getPriorTwoYearAmount));
            dto.addCurrentOneYearReimbursed(
                    BigDecimalUtils.sum(reimb, ActivityReimbursementDto::getCurrentOneYearAmount));
            dto.addCurrentTwoYearReimbursed(
                    BigDecimalUtils.sum(reimb, ActivityReimbursementDto::getCurrentTwoYearAmount));

            // === OBLIGATIONS ===
            dto.addPriorTwoYearObligated(
                    BigDecimalUtils.sum(ob, ObligationDto::getPriorTwoYearAmount));
            dto.addCurrentOneYearObligated(
                    BigDecimalUtils.sum(ob, ObligationDto::getCurrentOneYearAmount));
            dto.addCurrentTwoYearObligated(
                    BigDecimalUtils.sum(ob, ObligationDto::getCurrentTwoYearAmount));
        }

        return List.of(education, research, programSupport, mandatory);
    }
}